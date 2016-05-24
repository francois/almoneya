package almoneya.http

import java.io.IOException
import java.sql.SQLException
import javax.servlet.MultipartConfigElement
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import almoneya.TenantId
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import org.slf4j.{LoggerFactory, MDC}

import scala.util.{Failure, Success, Try}

abstract class JsonApiController[A](private[this] val mapper: ObjectMapper) extends AbstractHandler {
    override def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
        try {
            if (Option(request.getContentType).isDefined && request.getContentType.startsWith("multipart/form-data")) {
                baseRequest.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, multiPartConfig)
            }

            Option(request.getAttribute(ApiServer.TenantIdAttribute)) match {
                case Some(tenantId: TenantId) =>
                    process(tenantId, baseRequest, request) match {
                        case Success(result) =>
                            response.setContentType("application/json;charset=utf-8")
                            mapper.writeValue(response.getOutputStream, Results(result))
                            baseRequest.setHandled(true)

                        case Failure(ex: SQLException) =>
                            sendErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex, baseRequest, response)

                        case Failure(ex: IOException) =>
                            sendErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex, baseRequest, response)

                        case Failure(ex) =>
                            sendErrorResponse(HttpServletResponse.SC_BAD_REQUEST, ex, baseRequest, response)
                    }

                case other =>
                    log.warn("Failed to identify the TenantId from the request's attributes, found [{}]", other)
            }
        } finally {
            MDC.remove(ApiServer.RequestIdAttribute)
            MDC.remove(ApiServer.TenantIdAttribute)
            MDC.remove(ApiServer.UserIdAttribute)
        }
    }

    private[this] def sendErrorResponse(statusCode: Int, ex: Throwable, baseRequest: Request, response: HttpServletResponse): Unit = {
        response.setStatus(statusCode)
        response.setContentType("application/json;charset=utf-8")
        mapper.writeValue(response.getOutputStream, Errors(Seq(ex.getMessage)))
        baseRequest.setHandled(true)

        // Immediately tell the client there was an error, then log
        // This way, clients don't wait for logging to finish before continuing
        response.flushBuffer()
        log.error("Failed to process request", ex)
    }

    def process(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Try[A]

    private[this] val multiPartConfig = new MultipartConfigElement(System.getProperty("java.io.tmpdir"))
    private[this] val log = LoggerFactory.getLogger(classOf[JsonApiController[_]])
}

case class Results[A](data: A)

case class Errors(errors: Seq[String])
