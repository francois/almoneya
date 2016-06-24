package almoneya.http

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.sql.Connection
import java.util.concurrent.TimeUnit
import javax.servlet.MultipartConfigElement
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.sql.DataSource

import almoneya.TenantId
import almoneya.utils.Instrumented
import com.fasterxml.jackson.databind.ObjectMapper
import com.wix.accord.{GroupViolation, RuleViolation, Violation}
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import org.slf4j.{LoggerFactory, MDC}

import scala.util.{Failure, Success, Try}

class FrontController(val router: Router,
                      val dataSource: DataSource,
                      val mapper: ObjectMapper) extends AbstractHandler with Instrumented {

    import FrontController.{Errors, Results, multiPartConfig}

    override def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
        val startRouting = System.nanoTime
        try {
            activeMultipartHandlingIfRequestIsMultipart(baseRequest)

            val route = time("routing") {
                for (method <- Route.methodToHttpMethod(request.getMethod);
                     route <- router.route(request.getPathInfo, method)) yield route
            }

            sendResponse(route, baseRequest, request, response)
        } finally {
            val endWriteResponse = System.nanoTime
            log.info("{} \"{}\" {} ({} ms)",
                request.getMethod,
                request.getContextPath + request.getPathInfo,
                response.getStatus.asInstanceOf[AnyRef],
                TimeUnit.NANOSECONDS.toMillis(endWriteResponse - startRouting).asInstanceOf[AnyRef])
            // These are not the FrontController's concerns, but due to the API design of
            // the security subsystem in the HttpServlet spec, we have to do it here. The
            // security subsystem does not inform the LoginService that a request is
            // finished, thus we have to clean up after ourselves here.
            cleanupLoggingContext()
        }
    }

    private[this] def sendResponse(maybeRoute: Option[Route], baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
        val tenantId = Option(request.getAttribute(ApiServer.TenantIdAttribute)).map(id => id.asInstanceOf[TenantId])
        maybeRoute match {
            case Some(route) if tenantId.isDefined && route.transactionalBehaviour == NoTransactionNeeded =>
                runWithNoTransaction(tenantId, route, baseRequest, request, response)

            case Some(route) if tenantId.isDefined && route.transactionalBehaviour == TransactionRequired =>
                runWithTransaction(tenantId, route, baseRequest, request, response)

            case _ => () // Something went wrong, so we let someone else handle the eventual 404
        }
    }

    private[this] def runWithTransaction(tenantId: Option[TenantId], route: Route, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
        implicit val connection = acquireConnection

        try {
            connection.setAutoCommit(false)

            val (status, results) = Try(route.execute(tenantId.get, baseRequest, request)) match {
                case Success(Right(theResults)) =>
                    connection.commit()
                    (HttpServletResponse.SC_OK, Results(theResults))

                case Success(Left(violations)) =>
                    connection.rollback()
                    val messages = violations.foldLeft(Set.empty[String])((memo, violation) => violationToErrorMessage(violation, memo))
                    (HttpServletResponse.SC_BAD_REQUEST, Errors(messages))

                case Failure(ex) =>
                    connection.rollback()
                    log.warn("Internal error while processing " + route, ex)
                    (HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Errors(Set(ex.getMessage)))
            }

            time("writeResponse")(writeResponse(response, status, results, baseRequest))
        } finally {
            returnConnection(connection)
        }
    }

    private[this] def runWithNoTransaction(tenantId: Option[TenantId], route: Route, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
        implicit val connection = acquireConnection

        try {
            val (status, results) = Try(time("execute")(route.execute(tenantId.get, baseRequest, request))) match {
                case Success(Right(theResults)) =>
                    (HttpServletResponse.SC_OK, Results(theResults))

                case Success(Left(violations)) =>
                    val messages = violations.foldLeft(Set.empty[String])((memo, violation) => violationToErrorMessage(violation, memo))
                    log.debug("{}: {}", route, violations, null)
                    (HttpServletResponse.SC_BAD_REQUEST, Errors(messages))

                case Failure(ex) =>
                    (HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Errors(Set(ex.getMessage)))
            }

            time("writeResponse")(writeResponse(response, status, results, baseRequest))
        } finally {
            returnConnection(connection)
        }
    }

    private[this] def writeResponse(response: HttpServletResponse, status: Int, results: AnyRef, baseRequest: Request): Unit = {
        if (HttpServletResponse.SC_OK <= status && status < 300) {
            if (isPostWithRedirectToParameter(baseRequest)) {
                redirectToLocation(Option(baseRequest.getParameter("redirect_to")).map(new URI(_)).get, baseRequest, response)
            } else if (isPostWithMultipartAndRedirectToPart(baseRequest)) {
                val value = Option(baseRequest.getPart("redirect_to")).map(part => new BufferedReader(new InputStreamReader(part.getInputStream)).readLine())
                redirectToLocation(value.map(new URI(_)).get, baseRequest, response)
            } // else, proceed with regular response handling
        }

        if (!baseRequest.isHandled) {
            response.setStatus(status)
            response.setContentType("application/json")
            response.setCharacterEncoding("UTF-8")

            mapper.writeValue(response.getWriter, results)

            baseRequest.setHandled(true)
        }
    }

    private[this] def isPostWithRedirectToParameter(baseRequest: Request): Boolean = {
        Option(baseRequest.getMethod).exists(_.toLowerCase() == "post") && Option(baseRequest.getParameter("redirect_to")).isDefined
    }

    private[this] def isPostWithMultipartAndRedirectToPart(baseRequest: Request): Boolean = {
        Option(baseRequest.getMethod).exists(_.toLowerCase() == "post") && Option(baseRequest.getContentType).exists(_.startsWith("multipart/form-data")) && Option(baseRequest.getPart("redirect_to")).isDefined
    }

    private[this] def redirectToLocation(redirectTo: URI, baseRequest: Request, response: HttpServletResponse): Unit = {
        response.setStatus(HttpServletResponse.SC_FOUND)
        response.setContentType("text/html")
        response.setCharacterEncoding("UTF-8")

        val scheme = baseRequest.getScheme
        val host = baseRequest.getServerName
        val port = baseRequest.getServerPort
        val path = redirectTo.getPath // path includes the initial slash; hence we don't need it below
        val location = if (port < 0) {
                "%s://%s%s".format(scheme, host, path)
            } else {
                "%s://%s:%d%s".format(scheme, host, port, path)
            }

        response.setHeader("Location", location)
        response.getWriter.printf("You are being <a href=\"%s\">redirected</a>.\n", location)
        baseRequest.setHandled(true)
    }

    //////////////////// Database Connection Handling

    private[this] def acquireConnection: Connection = time("acquireConnection") {
        val connection = dataSource.getConnection
        log.debug("Acquired database connection {}", connection.hashCode())
        connection
    }

    private[this] def returnConnection(connection: Connection): Unit = time("returnConnection") {
        log.debug("Closing database connection {}", connection.hashCode())
        connection.close()
    }

    //////////////////// Helper Methods

    private[this] def violationToErrorMessage(violation: Violation, accumulator: Set[String]): Set[String] = violation match {
        case RuleViolation(value, constraint, Some(description)) =>
            accumulator + "%s %s".format(description, constraint)

        case RuleViolation(value, constraint, None) =>
            accumulator + constraint

        case GroupViolation(value, constraint, Some(description), children) =>
            children.foldLeft(accumulator)((memo, violation) => violationToErrorMessage(violation, memo))

        case GroupViolation(value, constraint, None, children) =>
            children.foldLeft(accumulator)((memo, violation) => violationToErrorMessage(violation, memo))
    }

    private[this] def cleanupLoggingContext(): Unit = {
        MDC.remove(ApiServer.RequestIdAttribute)
        MDC.remove(ApiServer.TenantIdAttribute)
        MDC.remove(ApiServer.UserIdAttribute)
    }

    private[this] def activeMultipartHandlingIfRequestIsMultipart(baseRequest: Request): Unit = {
        Option(baseRequest.getContentType)
                .filter(_.startsWith("multipart/form-data"))
                .foreach(_ => baseRequest.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, multiPartConfig))
    }

    private[this] val log = LoggerFactory.getLogger(this.getClass)
    val timingLog = LoggerFactory.getLogger(this.getClass.getName + ".timing")

    log.info("Front Controller Ready: {} routes defined", router.numberOfRoutes)
    router.routes.foreach { route =>
        route.methods.foreach(method => log.debug("{}", "%-8s %s".format(method.name, route.path)))
    }
}

object FrontController {
    val multiPartConfig = new MultipartConfigElement(System.getProperty("java.io.tmpdir"))

    case class Results[A](data: A)

    case class Errors(errors: Iterable[String])

}
