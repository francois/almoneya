package almoneya.http

import javax.servlet.http.HttpServletRequest

import almoneya._
import com.fasterxml.jackson.databind.ObjectMapper
import com.wix.accord._
import org.eclipse.jetty.server.Request

class CreateTransactionController(val mapper: ObjectMapper,
                                  val accountsRepository: AccountsRepository,
                                  val transactionsRepository: TransactionsRepository,
                                  val bankAccountTransactionsRepository: BankAccountTransactionsRepository) extends Controller {
    override def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Either[Iterable[Violation], AnyRef] = {
        Option(request.getContentType) match {
            case Some("application/json") =>
                val transactionForm = mapper.readValue(request.getInputStream, classOf[TransactionForm])
                validate(transactionForm) match {
                    case Success =>
                        val accounts = accountsRepository.findAll(tenantId)
                        val newTransaction = transactionForm.toTransaction(accounts)
                        if (newTransaction.entries.size == transactionForm.entries.size) {
                            transactionsRepository.transaction {
                                val txn = transactionsRepository.create(tenantId, newTransaction)
                                transactionForm.bankAccountTransactionId.foreach { id =>
                                    bankAccountTransactionsRepository.linkBankAccountTransactionToTransactionEntry(tenantId, BankAccountTransactionId(id.toInt), txn.transactionId.get)
                                }

                                Right(txn)
                            }
                        } else {
                            val missingNames = transactionForm.entries.flatMap(_.accountName) -- newTransaction.entries.map(_.accountName.name)
                            Left(missingNames.map(name => RuleViolation(name, "could not identify account", Some("account_name"))))
                        }

                    case Failure(violations) => Left(violations)
                }

            case contentType =>
                Left(Set(RuleViolation(contentType, "must be application/json", Some("Content-Type"))))
        }
    }
}
