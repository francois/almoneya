package almoneya.http

import java.sql.Connection
import javax.servlet.http.HttpServletRequest

import almoneya.TenantId
import com.wix.accord.Violation
import org.eclipse.jetty.server.Request
import org.slf4j.LoggerFactory

trait Controller {
    def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest)(implicit connection: Connection): Either[Iterable[Violation], AnyRef]

    val log = LoggerFactory.getLogger(getClass)
}
