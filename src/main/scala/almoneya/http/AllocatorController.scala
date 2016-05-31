package almoneya.http

import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest

import almoneya.automation.{Revenue => ARevenue, _}
import almoneya.{Revenue => DRevenue, _}
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.jetty.server.Request
import org.joda.time.LocalDate

class AllocatorController(private[this] val mapper: ObjectMapper,
                          private[this] val accountsRepository: AccountsRepository,
                          private[this] val goalsRepository: GoalsRepository,
                          private[this] val obligationsRepository: ObligationsRepository,
                          private[this] val revenuesRepository: RevenuesRepository) extends JsonApiController[Seq[Allocation]](mapper) {
    override def process(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Seq[Allocation] = {
        val maybePaidOn = Option(request.getParameter("paid_on")).map(new LocalDate(_))
        val maybeRevenueAmount = Option(request.getParameter("amount")).map(BigDecimal.apply).map(Amount.apply)
        val autofulfillThreshold = Option(request.getParameter("auto_fulfill_threshold")).map(BigDecimal.apply).map(Amount.apply).getOrElse(Amount(100))

        // TODO: Replace excpetions with form validation
        if (maybePaidOn.isEmpty && maybeRevenueAmount.isEmpty) throw new RuntimeException("Missing paid_on and amount parameters")
        if (maybePaidOn.isEmpty) throw new RuntimeException("Missing paid_on parameter")
        if (maybeRevenueAmount.isEmpty) throw new RuntimeException("Missing amount parameter")

        val results = for (paidOn <- maybePaidOn; amountReceived <- maybeRevenueAmount) yield {
            val goals = goalsRepository.findAll(tenantId)
            val obligations = obligationsRepository.findAll(tenantId)
            val envelopes = accountsRepository.findAllWithBalance(tenantId, paidOn)
            val revenues = revenuesRepository.findAll(tenantId)
            val fixedDateGoals = goals.map(goalToFundingGoal)
            val recurringGoals = obligations.map(obligationToFundingGoal(paidOn)).flatten
            val fundingGoals = fixedDateGoals ++ recurringGoals

            val allocator = RevenueAllocator(fundingGoals, revenues = revenues.map(domainRevenueToAutomationRevenue(paidOn)).flatten, autoFulfillThreshold = autofulfillThreshold)

            val startAllocationAt = System.nanoTime()
            val plan = allocator.generatePlan(paidOn, amountReceived)
            val endAllocationAt = System.nanoTime()
            if (log.isInfoEnabled()) {
                log.info("Allocated revenue in {} ms", "%.3f".format((endAllocationAt - startAllocationAt).toDouble / TimeUnit.MILLISECONDS.toNanos(1)))
            }

            plan
        }

        results.get
    }

    private[this] def domainRevenueToAutomationRevenue(paidOn: LocalDate)(revenue: DRevenue): Option[ARevenue] =
        revenue.dueOnAfter(paidOn).map(dueOn => ARevenue(name = revenue.name, dueOn = dueOn, period = revenue.period, every = revenue.every))

    private[this] def obligationToFundingGoal(paidOn: LocalDate)(obligation: Obligation): Option[RecurringObligation] = {
        obligation.dueOnAfter(paidOn).map(nextDueOn =>
            RecurringObligation(priority = obligation.priority,
                name = ObligationName.fromAccountName(obligation.account.name),
                target = obligation.amount,
                balance = Amount(0),
                dueOn = nextDueOn,
                period = obligation.period,
                every = obligation.every,
                endOn = obligation.endOn))
    }

    private[this] def goalToFundingGoal(goal: Goal): FundingGoal = {
        FixedDateObligation(priority = goal.priority,
            name = ObligationName.fromAccountName(goal.account.name),
            target = goal.amount,
            balance = Amount(0),
            dueOn = goal.dueOn)
    }
}
