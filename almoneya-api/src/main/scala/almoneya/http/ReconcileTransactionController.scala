package almoneya.http

import java.sql.Connection
import javax.servlet.http.HttpServletRequest

import almoneya._
import com.wix.accord._
import org.eclipse.jetty.server.Request
import org.joda.time.LocalDate

class ReconcileTransactionController(reconciliationsRepository: ReconciliationsRepository, accountsRepository: AccountsRepository, transactionsRepository: TransactionsRepository) extends Controller {

    import com.wix.accord.dsl._

    case class ReconcileTransactionForm(tenantId: TenantId,
                                        transactionId: Option[String],
                                        postedOn: Option[String],
                                        accountName: Option[String],
                                        validAccounts: Set[Account] = Set.empty,
                                        validTransactionIds: Set[TransactionId] = Set.empty) {
        def toReconciliationEntry =
            ReconciliationEntry(
                transactionId = TransactionId(transactionId.get.toInt),
                postedOn = new LocalDate(postedOn.get),
                accountName = AccountName(accountName.get))
    }

    object ReconcileTransactionForm {
        implicit val reconcileTransactionFormValidator = validator[ReconcileTransactionForm] { form =>
            form.transactionId is notEmpty
            form.transactionId.each is notEmpty
            form.transactionId.each is valid(TransactionIdValidatorBuilder(form.validTransactionIds).build)

            form.postedOn is notEmpty
            form.postedOn.each is notEmpty
            form.postedOn.each is matchRegexFully(LocalDateEx.VALID_RE)

            form.accountName is notEmpty
            form.accountName.each is notEmpty
            form.accountName.each is valid(AccountNameValidator(form.validAccounts).build)
        }
    }

    override def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest)(implicit connection: Connection): Either[Iterable[Violation], AnyRef] = {
        val form = ReconcileTransactionForm(tenantId,
            Option(request.getParameter("transaction_id")),
            Option(request.getParameter("posted_on")),
            Option(request.getParameter("account_name")))
        validate(form.copy(validAccounts = accountsRepository.findAll(tenantId), validTransactionIds = transactionsRepository.findAllIds(tenantId))) match {
            case Success =>
                Right(reconciliationsRepository.createEntry(tenantId, form.toReconciliationEntry))

            case Failure(violations) => Left(violations)
        }
    }
}
