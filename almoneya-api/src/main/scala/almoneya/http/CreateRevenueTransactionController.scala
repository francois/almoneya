package almoneya.http

import java.sql.Connection
import javax.servlet.http.HttpServletRequest

import almoneya._
import com.fasterxml.jackson.databind.ObjectMapper
import com.wix.accord.{Failure, Success, Violation, validate}
import org.eclipse.jetty.server.Request

class CreateRevenueTransactionController(accountsRepository: AccountsRepository, transactionsRepository: TransactionsRepository, mapper: ObjectMapper) extends Controller {
    override def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest)(implicit connection: Connection): Either[Iterable[Violation], AnyRef] = {
        val form = mapper.readValue(request.getInputStream, classOf[CreateRevenueTransactionForm])
        val validAccounts = accountsRepository.findAll(tenantId)
        validate(form.copy(validAccounts = validAccounts)) match {
            case Success =>
                Right(transactionsRepository.create(tenantId, transaction = form.toTransaction))

            case Failure(violations) => Left(violations)
        }
    }
}
