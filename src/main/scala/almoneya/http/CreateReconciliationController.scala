package almoneya.http

import javax.servlet.http.HttpServletRequest

import almoneya._
import com.wix.accord.{Failure, Success, Violation, validate}
import org.eclipse.jetty.server.Request
import org.joda.time.LocalDate

class CreateReconciliationController(reconciliationsRepository: ReconciliationsRepository) extends Controller {

    import com.wix.accord.dsl._

    case class ReconciliationForm(accountName: Option[String], postedOn: Option[String], openingBalance: Option[String], endingBalance: Option[String]) {
        def toReconciliation: Reconciliation =
            Reconciliation(
                accountName = accountName.map(AccountName.apply).get,
                postedOn = postedOn.map(new LocalDate(_)).get,
                openingBalance = openingBalance.map(BigDecimal.apply).map(Amount.apply).get,
                endingBalance = endingBalance.map(BigDecimal.apply).map(Amount.apply).get)
    }

    object ReconciliationForm {
        implicit val reconciliationFormValidator = validator[ReconciliationForm] { form =>
            form.accountName is notEmpty
            form.accountName.each is notEmpty
            form.postedOn is notEmpty
            form.postedOn.each is notEmpty
            form.openingBalance is notEmpty
            form.openingBalance.each is notEmpty
            form.endingBalance is notEmpty
            form.endingBalance.each is notEmpty
        }
    }

    override def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Either[Iterable[Violation], AnyRef] = {
        val form = ReconciliationForm(
            Option(request.getParameter("account_name")),
            Option(request.getParameter("posted_on")),
            Option(request.getParameter("opening_balance")),
            Option(request.getParameter("ending_balance")))
        validate(form) match {
            case Success =>
                reconciliationsRepository.transaction {
                    Right(reconciliationsRepository.createReconciliation(tenantId, form.toReconciliation))
                }

            case Failure(violations) => Left(violations)
        }
    }
}
