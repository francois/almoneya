package almoneya.http

import javax.servlet.http.HttpServletRequest

import almoneya._
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.jetty.server.Request
import org.joda.time.LocalDate

import scala.collection.JavaConversions._
import scala.util.{Failure, Try}

class ReconcileTransactionController(mapper: ObjectMapper, reconciliationsRepository: ReconciliationsRepository) extends JsonApiController[ReconciliationEntry](mapper) {
    override def process(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Try[ReconciliationEntry] = {
        val maybeEntry = for (transactionId <- Option(request.getParameter("transaction_id")).map(_.toInt);
                              postedOn <- Option(request.getParameter("posted_on"));
                              accountName <- Option(request.getParameter("account_name"))) yield
            ReconciliationEntry(transactionId = TransactionId(transactionId), postedOn = new LocalDate(postedOn), accountName = AccountName(accountName))
        maybeEntry match {
            case Some(entry) => reconciliationsRepository.createEntry(tenantId, entry)
            case None =>
                val missingParams = Set("transaction_id", "posted_on", "account_name") -- request.getParameterNames.toSet
                if (missingParams.isEmpty) {
                    // all parameteres are accounted for, thus the problem is transaction_id that couldn't be parsed as an Int
                    Failure(new RuntimeException("Parameter transaction_id could not be parsed as an Int, found [" + request.getParameter("transaction_id") + "]"))
                } else {
                    Failure(new RuntimeException("Missing parameters: " + missingParams.mkString(", ")))
                }
        }
    }
}
