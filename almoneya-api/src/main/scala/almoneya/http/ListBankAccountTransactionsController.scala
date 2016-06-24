package almoneya.http

import java.sql.Connection
import javax.servlet.http.HttpServletRequest

import almoneya.{BankAccountTransactionsRepository, TenantId}
import com.wix.accord.Violation
import org.eclipse.jetty.server.Request

class ListBankAccountTransactionsController(bankAccountTransactionsRepository: BankAccountTransactionsRepository) extends Controller {
    override def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest)(implicit connection: Connection): Either[Iterable[Violation], AnyRef] = {
        Right(bankAccountTransactionsRepository.listBankAccountTransactions(tenantId))
    }
}
