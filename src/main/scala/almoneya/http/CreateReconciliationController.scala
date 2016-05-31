package almoneya.http

import javax.servlet.http.HttpServletRequest

import almoneya._
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.jetty.server.Request
import org.joda.time.LocalDate

class CreateReconciliationController(mapper: ObjectMapper, reconciliationsRepository: ReconciliationsRepository) extends JsonApiController[Reconciliation](mapper) {
    override def process(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Reconciliation = {
        val maybeReconciliation = for (accountName <- Option(request.getParameter("account_name")).map(AccountName.apply);
                                       postedOn <- Option(request.getParameter("posted_on")).map(new LocalDate(_));
                                       openingBalance <- Option(request.getParameter("opening_balance")).map(BigDecimal.apply).map(Amount.apply);
                                       endingBalance <- Option(request.getParameter("ending_balance")).map(BigDecimal.apply).map(Amount.apply)) yield
            Reconciliation(accountName = accountName, postedOn = postedOn, openingBalance = openingBalance, endingBalance = endingBalance)
        maybeReconciliation match {
            case Some(reconciliation) =>
                reconciliationsRepository.transaction {
                    reconciliationsRepository.createReconciliation(tenantId, reconciliation)
                }
            case None =>
                // TODO: implement some kind of validation library
                throw new RuntimeException("Validation error; sorry no nice message to help you out")
        }
    }
}
