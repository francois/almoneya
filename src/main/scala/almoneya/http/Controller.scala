package almoneya.http

import javax.servlet.http.HttpServletRequest

import almoneya.TenantId
import com.wix.accord.Violation
import org.eclipse.jetty.server.Request

import scala.util.Try

trait Controller {
    def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Try[Either[Iterable[Violation], AnyRef]]
}
