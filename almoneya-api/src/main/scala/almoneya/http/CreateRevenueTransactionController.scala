package almoneya.http

import java.sql.Connection
import javax.servlet.http.HttpServletRequest

import almoneya._
import com.fasterxml.jackson.core.{JsonParseException, JsonParser, JsonToken}
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.{DeserializationContext, ObjectMapper}
import com.wix.accord.{Failure, Success, Violation, validate}
import org.eclipse.jetty.server.Request
import org.joda.time.LocalDate

class CreateRevenueTransactionController(accountsRepository: AccountsRepository, transactionsRepository: TransactionsRepository, mapper: ObjectMapper) extends Controller {

    import com.wix.accord.dsl._

    override def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest)(implicit connection: Connection): Either[Iterable[Violation], AnyRef] = {
        val form = mapper.readValue(request.getInputStream, classOf[CreateRevenueTransactionForm])
        validate(form) match {
            case Success =>
                Right(transactionsRepository.create(tenantId, transaction = form.toTransaction(tenantId, accountsRepository)))

            case Failure(violations) => Left(violations)
        }
    }
}
