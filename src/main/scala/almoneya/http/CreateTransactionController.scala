package almoneya.http

import javax.servlet.http.HttpServletRequest

import almoneya._
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.jetty.server.Request

class CreateTransactionController(mapper: ObjectMapper, accountsRepository: AccountsRepository, transactionsRepository: TransactionsRepository, bankAccountTransactionsRepository: BankAccountTransactionsRepository) extends JsonApiController[Transaction](mapper) {
    override def process(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Transaction = {
        request.getContentType match {
            case "application/json" =>
                val transactionForm = mapper.readValue(request.getInputStream, classOf[TransactionForm])
                val accounts = accountsRepository.findAll(tenantId)
                val newEntries: Set[TransactionEntry] = transactionForm.entries.map(entry => accounts.find(_.name == entry.accountName).map(account => TransactionEntry(account = account, amount = entry.amount))).flatten
                if (newEntries.size == transactionForm.entries.size) {
                    val newTransaction = Transaction(payee = transactionForm.payee, description = transactionForm.description, postedOn = transactionForm.postedOn, entries = newEntries)

                    transactionsRepository.transaction {
                        val txn = transactionsRepository.create(tenantId, transaction = newTransaction)
                        transactionForm.bankAccountTransactionId.foreach { id =>
                            bankAccountTransactionsRepository.linkBankAccountTransactionToTransactionEntry(tenantId, id, txn.transactionId.get)
                        }

                        txn
                    }
                } else {
                    val missingNames = transactionForm.entries.map(_.accountName) -- newEntries.map(_.accountName)
                    throw new RuntimeException("Account name(s) " + missingNames + " were not found")
                }

            case contentType =>
                log.warn("Failed to receive ")
                throw new BadFormatException("This endpoint only accepts application/json")
        }
    }
}
