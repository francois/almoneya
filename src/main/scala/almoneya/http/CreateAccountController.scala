package almoneya.http

import javax.servlet.http.HttpServletRequest

import almoneya.{AccountsRepository, TenantId}
import com.wix.accord.{Failure => AccordFailure, Success => AccordSuccess, Violation, validate}
import org.eclipse.jetty.server.Request

import scala.util.Try

class CreateAccountController(accountsRepository: AccountsRepository) extends Controller {
    override def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Either[Iterable[Violation], AnyRef] = {
        val form = AccountForm(code = Option(request.getParameter("code")),
            name = Option(request.getParameter("name")),
            kind = Option(request.getParameter("kind")),
            virtual = Option(request.getParameter("virtual")))

        validate(form) match {
            case AccordSuccess =>
                Right(accountsRepository.create(tenantId, form.toAccount))

            case AccordFailure(violations) => Left(violations)
        }
    }
}
