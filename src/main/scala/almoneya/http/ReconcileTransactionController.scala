package almoneya.http

import javax.servlet.http.HttpServletRequest

import almoneya._
import com.wix.accord._
import org.eclipse.jetty.server.Request
import org.joda.time.LocalDate

class ReconcileTransactionController(reconciliationsRepository: ReconciliationsRepository, accountsRepository: AccountsRepository) extends Controller {

    import com.wix.accord.dsl._

    case class ReconcileTransactionForm(tenantId: TenantId, transactionId: Option[String], postedOn: Option[String], accountName: Option[String]) {
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
            form.transactionId.each is matchRegexFully(TransactionId.VALID_RE)

            form.postedOn is notEmpty
            form.postedOn.each is notEmpty
            form.postedOn.each is matchRegexFully(LocalDateEx.VALID_RE)

            form.accountName is notEmpty
            form.accountName.each is notEmpty
            form.accountName.each is valid(AccountNameValidatorBuilder(form.tenantId, accountsRepository).build)
        }
    }

    override def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Either[Iterable[Violation], AnyRef] = {
        val form = ReconcileTransactionForm(tenantId,
            Option(request.getParameter("transaction_id")),
            Option(request.getParameter("posted_on")),
            Option(request.getParameter("account_name")))
        validate(form) match {
            case Success =>
                reconciliationsRepository.transaction {
                    Right(reconciliationsRepository.createEntry(tenantId, form.toReconciliationEntry))
                }

            case Failure(violations) => Left(violations)
        }
    }
}
