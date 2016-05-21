package almoneya.http

import javax.servlet.http.HttpServletRequest

import almoneya._
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.jetty.server.Request

import scala.util.Try

class ListAccountsController(private[this] val mapper: ObjectMapper, private[this] val accountsRepository: AccountsRepository) extends JsonApiController[Set[Account]](mapper) {
    override def process(baseRequest: Request, request: HttpServletRequest): Try[Set[Account]] = {
        accountsRepository.findAll(TenantId(1))
    }
}
