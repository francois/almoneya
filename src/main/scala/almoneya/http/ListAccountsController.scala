package almoneya.http

import javax.servlet.http.HttpServletRequest

import almoneya.{AccountsRepository, TenantId}
import com.wix.accord.{Failure => AccordFailure, Success => AccordSuccess, Violation, validate}
import org.eclipse.jetty.server.Request
import org.joda.time.LocalDate

import scala.util.{Success => ScalaSuccess, Try}

case class ListAccountsController(accountsRepository: AccountsRepository) extends Controller {

    import com.wix.accord.dsl._

    case class ListAccountsForm(balanceOn: Option[String])

    object ListAccountsForm {
        implicit val listAccountsFormValidator = validator[ListAccountsForm] { form =>
            (form.balanceOn is empty) or (form.balanceOn.each is matchRegexFully("""\d{4}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[12][0-9]|3[0-1])"""))
        }
    }

    override def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Try[Either[Set[Violation], AnyRef]] = Try {
        val form = ListAccountsForm(Option(request.getParameter("balance_an")))
        validate(form) match {
            case AccordSuccess =>
                val balanceOn = form.balanceOn.map(new LocalDate(_)).getOrElse(new LocalDate())
                accountsRepository.findAllWithBalance(tenantId, balanceOn).map(Right.apply)

            case AccordFailure(violations) => ScalaSuccess(Left(violations))
        }
    }.flatten
}
