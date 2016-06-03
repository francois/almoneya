package almoneya.http

import java.sql.Connection
import javax.servlet.http.HttpServletRequest

import almoneya.{LocalDateEx, AccountsRepository, TenantId}
import com.wix.accord.{Failure => AccordFailure, Success => AccordSuccess, Violation, validate}
import org.eclipse.jetty.server.Request
import org.joda.time.LocalDate

import scala.util.{Success => ScalaSuccess, Try}

case class ListAccountsController(accountsRepository: AccountsRepository) extends Controller {

    import com.wix.accord.dsl._

    case class ListAccountsForm(balanceOn: Option[String])

    object ListAccountsForm {
        implicit val listAccountsFormValidator = validator[ListAccountsForm] { form =>
            (form.balanceOn is empty) or (form.balanceOn.each is matchRegexFully(LocalDateEx.VALID_RE))
        }
    }

    override def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest)(implicit connection: Connection): Either[Set[Violation], AnyRef] = {
        val form = ListAccountsForm(Option(request.getParameter("balance_an")))
        validate(form) match {
            case AccordSuccess =>
                val balanceOn = form.balanceOn.map(new LocalDate(_)).getOrElse(new LocalDate())
                Right(accountsRepository.findAllWithBalance(tenantId, balanceOn))

            case AccordFailure(violations) => Left(violations)
        }
    }
}
