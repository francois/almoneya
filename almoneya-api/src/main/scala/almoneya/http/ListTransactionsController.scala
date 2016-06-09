package almoneya.http

import java.sql.Connection
import javax.servlet.http.HttpServletRequest

import almoneya.{TenantId, TransactionsRepository}
import com.wix.accord.Violation
import org.eclipse.jetty.server.Request

class ListTransactionsController(transactionsRepository: TransactionsRepository) extends Controller {
    override def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest)(implicit connection: Connection): Either[Iterable[Violation], AnyRef] = {
        Right(transactionsRepository.findAllWithBalance(tenantId))
    }
}
