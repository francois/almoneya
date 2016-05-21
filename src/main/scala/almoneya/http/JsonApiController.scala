package almoneya.http

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import almoneya.TenantId
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

abstract class JsonApiController[A](private[this] val mapper: ObjectMapper) extends AbstractHandler {
    override def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
        Option(request.getAttribute(ApiServer.TenantIdAttribute)) match {
            case Some(tenantId: TenantId) =>
                process(tenantId, baseRequest, request) match {
                    case Success(result) =>
                        response.setContentType("application/json;charset=utf-8")
                        mapper.writeValue(response.getOutputStream, Results(result))
                        baseRequest.setHandled(true)

                    case Failure(ex) =>
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage)
                        baseRequest.setHandled(true)
                }

            case other =>
                log.warn("Failed to identify the TenantId from the request's attributes, found [{}]", other)
        }
    }

    def process(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Try[A]

    val log = LoggerFactory.getLogger(classOf[JsonApiController[_]])
}

case class Results[A](data: A)
