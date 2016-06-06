package almoneya.http

import java.sql.Connection
import javax.servlet.http.HttpServletRequest

import almoneya._
import com.fasterxml.jackson.databind.ObjectMapper
import com.wix.accord._
import org.eclipse.jetty.server.Request

class CreateRevenueTransactionController(accountsRepository: AccountsRepository, transactionsRepository: TransactionsRepository, mapper: ObjectMapper) extends Controller {
    override def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest)(implicit connection: Connection): Either[Iterable[Violation], AnyRef] = {
        Option(request.getContentType).map(_.split(";")).map(_.head) match {
            case Some("application/json") =>
                val pendingForm = mapper.readValue(request.getInputStream, classOf[CreateRevenueTransactionForm])
                log.debug("received {}", pendingForm)
                val form = pendingForm.copy(validAccounts = accountsRepository.findAll(tenantId))
                log.debug("validAccounts: {} entries", form.validAccounts.size)
                validate(form) match {
                    case Success =>
                        Right(transactionsRepository.create(tenantId, transaction = form.toTransaction))

                    case Failure(violations) => Left(violations)
                }

            case Some(other) =>
                log.warn("Received non-application/json request: {}", other)
                Left(Set(RuleViolation(other, "must be application/json, found \"" + other + "\"", Some("Content-Type"))))

            case None => Left(Set(RuleViolation(null, "must be application/json, fond none", Some("Content-Type"))))
        }
    }
}
