package almoneya

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler

class WebApiHandler extends AbstractHandler {
    override def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
        response.setHeader("Content-Type", "text/html; charset=utf-8")
        response.setStatus(HttpServletResponse.SC_OK)
        response.getWriter.println("<h1>Hello World</h1>")
        baseRequest.setHandled(true)
    }
}
