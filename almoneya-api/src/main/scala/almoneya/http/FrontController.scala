package almoneya.http

import java.sql.Connection
import javax.servlet.MultipartConfigElement
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.sql.DataSource

import almoneya.TenantId
import com.fasterxml.jackson.databind.ObjectMapper
import com.wix.accord.{GroupViolation, RuleViolation, Violation}
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import org.slf4j.{LoggerFactory, MDC}

import scala.util.{Failure, Success, Try}

class FrontController(val router: Router,
                      val dataSource: DataSource,
                      val mapper: ObjectMapper) extends AbstractHandler {

    import FrontController.{Errors, Results, multiPartConfig}

    override def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
        try {
            activeMultipartHandlingIfRequestIsMultipart(baseRequest)

            val route = for (method <- Route.methodToHttpMethod(request.getMethod);
                             route <- router.route(request.getPathInfo, method)) yield route

            sendResponse(route, baseRequest, request, response)
        } finally {
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

            writeResponse(response, status, results, baseRequest)
        } finally {
            returnConnection(connection)
        }
    }

    private[this] def runWithNoTransaction(tenantId: Option[TenantId], route: Route, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
        implicit val connection = acquireConnection

        try {
            val (status, results) = Try(route.execute(tenantId.get, baseRequest, request)) match {
                case Success(Right(theResults)) =>
                    (HttpServletResponse.SC_OK, Results(theResults))

                case Success(Left(violations)) =>
                    val messages = violations.foldLeft(Set.empty[String])((memo, violation) => violationToErrorMessage(violation, memo))
                    (HttpServletResponse.SC_BAD_REQUEST, Errors(messages))

                case Failure(ex) =>
                    (HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Errors(Set(ex.getMessage)))
            }

            writeResponse(response, status, results, baseRequest)
        } finally {
            returnConnection(connection)
        }
    }

    private[this] def writeResponse(response: HttpServletResponse, status: Int, results: Product with Serializable, baseRequest: Request): Unit = {
        response.setStatus(status)
        response.setContentType("application/json")
        response.setCharacterEncoding("UTF-8")

        mapper.writeValue(response.getWriter, results)

        baseRequest.setHandled(true)
    }

    //////////////////// Database Connection Handling

    private[this] def acquireConnection: Connection = {
        val connection = dataSource.getConnection
        log.debug("Acquired database connection {}", connection.hashCode())
        connection
    }

    private[this] def returnConnection(connection: Connection): Unit = {
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
