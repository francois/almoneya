package almoneya.http

import javax.servlet.http.HttpServletRequest

import almoneya.{AccountsRepository, TenantId}
import com.wix.accord.{Failure, Success, Violation, validate}
import org.eclipse.jetty.server.Request
import org.joda.time.LocalDate

class SearchAccountsController(accountsRepository: AccountsRepository) extends Controller {

    import com.wix.accord.dsl._

    case class SearchForm(q: Option[String])

    object SearchForm {
        implicit val searchFormValidator = validator[SearchForm] { form =>
            form.q is notEmpty
            form.q.each is notEmpty
        }
    }

    override def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Either[Iterable[Violation], AnyRef] = {
        val form = SearchForm(Option(request.getParameter("q")))
        validate(form) match {
            case Success =>
                val accounts = accountsRepository.findAllWithBalance(tenantId, new LocalDate())
                Right(accounts.filter(_.name.caseInsensitiveContains(form.q.get)))

            case Failure(violations) => Left(violations)
        }
    }
}
