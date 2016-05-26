package almoneya.http

import javax.servlet.http.HttpServletRequest

import almoneya._
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.jetty.server.Request
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

class CreateTransactionController(mapper: ObjectMapper, accountsRepository: AccountsRepository, transactionsRepository: TransactionsRepository) extends JsonApiController[Transaction](mapper) {
    override def process(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Try[Transaction] = {
        request.getContentType match {
            case "application/json" =>
                val transactionForm = mapper.readValue(request.getInputStream, classOf[TransactionForm])
                accountsRepository.findAll(tenantId) match {
                    case Success(accounts) =>
                        val newEntries: Set[TransactionEntry] = transactionForm.entries.flatMap(entry => accounts.find(_.name == entry.accountName).map(account => TransactionEntry(account = account, amount = entry.amount)))
                        if (newEntries.size == transactionForm.entries.size) {
                            val newTransaction = Transaction(payee = transactionForm.payee, description = transactionForm.description, postedOn = transactionForm.postedOn, entries = newEntries)

                            transactionsRepository.transaction {
                                transactionsRepository.create(tenantId, transaction = newTransaction)
                            }
                        } else {
                            val missingNames = transactionForm.entries.map(_.accountName) -- newEntries.map(_.accountName)
                            Failure(new RuntimeException("Account name(s) " + missingNames + " were not found"))
                        }

                    case Failure(ex) => Failure(ex)
                }

            case contentType =>
                log.warn("Failed to receive ")
                Failure(new BadFormatException("This endpoint only accepts application/json"))
        }
    }

    private[this] val log = LoggerFactory.getLogger(classOf[CreateTransactionController])
}
