package almoneya.http

import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest

import almoneya.automation.{Revenue => ARevenue, _}
import almoneya.{Revenue => DRevenue, _}
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.jetty.server.Request
import org.joda.time.LocalDate
import org.slf4j.LoggerFactory

import scala.util.{Failure, Try}

class AllocatorController(private[this] val mapper: ObjectMapper,
                          private[this] val accountsRepository: AccountsRepository,
                          private[this] val goalsRepository: GoalsRepository,
                          private[this] val obligationsRepository: ObligationsRepository,
                          private[this] val revenuesRepository: RevenuesRepository) extends JsonApiController[Seq[Allocation]](mapper) {
    override def process(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest): Try[Seq[Allocation]] = {
        val maybePaidOn = Option(request.getParameter("paid_on")).map(new LocalDate(_))
        val maybeRevenueAmount = Option(request.getParameter("amount")).map(BigDecimal.apply).map(Amount.apply)
        val autofulfillThreshold = Option(request.getParameter("auto_fulfill_threshold")).map(BigDecimal.apply).map(Amount.apply).getOrElse(Amount(100))

        val results = for (paidOn <- maybePaidOn; amountReceived <- maybeRevenueAmount) yield {
            val maybeGoals = goalsRepository.findAll(tenantId)
            val maybeObligations = obligationsRepository.findAll(tenantId)
            val maybeEnvelopes = accountsRepository.findAllWithBalance(tenantId)
            val maybeRevenues = revenuesRepository.findAll(tenantId)
            for (goals <- maybeGoals; obligations <- maybeObligations; envelopes <- maybeEnvelopes; revenues <- maybeRevenues) yield {
                val fixedDateGoals = goals.map(goalToFundingGoal)
                val recurringGoals = obligations.flatMap(obligationToFundingGoal(paidOn))
                val fundingGoals = fixedDateGoals ++ recurringGoals

                val allocator = RevenueAllocator(fundingGoals, revenues = revenues.flatMap(domainRevenueToAutomationRevenue(paidOn)), autoFulfillThreshold = autofulfillThreshold)

                val startAllocationAt = System.nanoTime()
                val plan = allocator.generatePlan(paidOn, amountReceived)
                val endAllocationAt = System.nanoTime()
                if (log.isInfoEnabled()) {
                    log.info("Allocated revenue in {} ms", "%.3f".format((endAllocationAt - startAllocationAt).toDouble / TimeUnit.MILLISECONDS.toNanos(1)))
                }

                plan
            }
        }

        results match {
            case None if maybePaidOn.isEmpty && maybeRevenueAmount.isEmpty => Failure(new RuntimeException("Missing paid_on and amount parameters"))
            case None if maybePaidOn.isEmpty => Failure(new RuntimeException("Missing paid_on parameter"))
            case None if maybeRevenueAmount.isEmpty => Failure(new RuntimeException("Missing amount parameter"))
            case Some(result) => result
        }
    }

    private[this] def domainRevenueToAutomationRevenue(paidOn: LocalDate)(revenue: DRevenue): Option[ARevenue] =
        revenue.dueOnAfter(paidOn).map(dueOn => ARevenue(name = revenue.name, dueOn = dueOn, period = revenue.period, every = revenue.every))

    private[this] def obligationToFundingGoal(paidOn: LocalDate)(obligation: Obligation): Option[RecurringObligation] = {
        obligation.dueOnAfter(paidOn).map(nextDueOn =>
            RecurringObligation(priority = Priority(50),
                name = ObligationName.fromAccountName(obligation.account.name),
                target = obligation.amount,
                balance = Amount(0),
                dueOn = nextDueOn,
                period = obligation.period,
                every = obligation.every,
                endOn = obligation.endOn))
    }

    private[this] def goalToFundingGoal(goal: Goal): FundingGoal = {
        FixedDateObligation(priority = Priority(100),
            name = ObligationName.fromAccountName(goal.account.name),
            target = goal.amount,
            balance = Amount(0),
            dueOn = goal.dueOn)
    }

    private[this] val log = LoggerFactory.getLogger(classOf[AllocatorController])
}
