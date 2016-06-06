package almoneya.http

import java.sql.Connection
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest

import almoneya.automation.{Revenue => ARevenue, _}
import almoneya.{Revenue => DRevenue, _}
import com.wix.accord.{Failure, Success, Violation, validate}
import org.eclipse.jetty.server.Request
import org.joda.time.LocalDate
import org.slf4j.LoggerFactory

class BuildAllocationController(accountsRepository: AccountsRepository, goalsRepository: GoalsRepository, obligationsRepository: ObligationsRepository, revenuesRepository: RevenuesRepository) extends Controller {

    import com.wix.accord.dsl._

    case class AllocationForm(paidOn: Option[String], amount: Option[String], autoFulfillThreshold: Option[String]) {
        def toValues(defaultAutoFulfillThreshold: Amount): (LocalDate, Amount, Amount) =
            (paidOn.map(new LocalDate(_)).get, Amount(0), Amount(0))
    }

    object AllocationForm {
        implicit val allocationFormValidator = validator[AllocationForm] { form =>
            form.paidOn is notEmpty
            form.paidOn.each is matchRegexFully(LocalDateEx.VALID_RE)
            form.amount is notEmpty
            form.amount.each is matchRegexFully(Amount.VALID_RE)
            (form.autoFulfillThreshold is empty) or ((form.autoFulfillThreshold is notEmpty) and (form.autoFulfillThreshold.each is notEmpty) and (form.autoFulfillThreshold.each is matchRegexFully(Amount.VALID_RE)))
        }
    }

    override def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest)(implicit connection: Connection): Either[Iterable[Violation], AnyRef] = {
        val form = AllocationForm(
            Option(request.getParameter("paid_on")),
            Option(request.getParameter("amount")),
            Option(request.getParameter("auto_fulfill_threshold")))

        validate(form) match {
            case Success =>
                val (paidOn, amountReceived, autoFulfillThreshold) = form.toValues(Amount(100))

                val goals = goalsRepository.findAll(tenantId)
                val obligations = obligationsRepository.findAll(tenantId)
                val envelopes = accountsRepository.findAllWithBalance(tenantId, paidOn)
                val revenues = revenuesRepository.findAll(tenantId)
                val fixedDateGoals = goals.map(goalToFundingGoal)
                val recurringGoals = obligations.map(obligationToFundingGoal(paidOn)).flatten
                val fundingGoals = fixedDateGoals ++ recurringGoals

                val allocator = RevenueAllocator(fundingGoals, revenues = revenues.map(domainRevenueToAutomationRevenue(paidOn)).flatten, autoFulfillThreshold = autoFulfillThreshold)

                val startAllocationAt = System.nanoTime()
                val plan = allocator.generatePlan(paidOn, amountReceived)
                val endAllocationAt = System.nanoTime()
                if (log.isInfoEnabled()) {
                    log.info("Allocated revenue in {} ms", "%.3f".format((endAllocationAt - startAllocationAt).toDouble / TimeUnit.MILLISECONDS.toNanos(1)))
                }

                Right(plan)

            case Failure(violations) => Left(violations)
        }
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
