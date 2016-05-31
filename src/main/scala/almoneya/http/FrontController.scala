package almoneya.http

import javax.servlet.MultipartConfigElement
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import almoneya.TenantId
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import org.slf4j.MDC

import scala.util.{Failure, Success}

class FrontController(val router: Router,
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
        val tenantId = Option(request.getAttribute(ApiServer.TenantIdAttribute)).map(id => TenantId(id.asInstanceOf[Int]))
        maybeRoute match {
            case Some(route) if tenantId.isDefined =>
                val (status, results) = route.execute(tenantId.get, baseRequest, request) match {
                    case Success(Right(theResults)) =>
                        (HttpServletResponse.SC_OK, Results(theResults))

                    case Success(Left(violations)) =>
                        val messages = violations.map(violation => violation.description.map(_ + " ").getOrElse("") + violation.constraint)
                        (HttpServletResponse.SC_BAD_REQUEST, Errors(messages))

                    case Failure(ex) =>
                        (HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Errors(Set(ex.getMessage)))
                }

                response.setStatus(status)
                response.setContentType("application/json")
                response.setCharacterEncoding("UTF-8")

                mapper.writeValue(response.getWriter, results)

                baseRequest.setHandled(true)

            case _ => () // Something went wrong, so we let someone else handle the eventual 404
        }
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
}

object FrontController {
    val multiPartConfig = new MultipartConfigElement(System.getProperty("java.io.tmpdir"))

    case class Results[A](data: A)

    case class Errors(errors: Iterable[String])

}
