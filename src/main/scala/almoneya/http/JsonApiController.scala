package almoneya.http

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler

import scala.util.{Failure, Success, Try}

abstract class JsonApiController[A](private[this] val mapper: ObjectMapper) extends AbstractHandler {

    override def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
        process(baseRequest, request) match {
            case Success(result) =>
                response.setContentType("application/json;charset=utf-8")
                mapper.writeValue(response.getOutputStream, Results(result))
                baseRequest.setHandled(true)

            case Failure(ex) =>
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage)
                baseRequest.setHandled(true)
        }
    }

    def process(baseRequest: Request, request: HttpServletRequest): Try[A]
}

case class Results[A](data:A)
