package almoneya

import org.joda.time.LocalDate

case class Goal(id: Option[GoalId] = None,
                account: Account,
                description: Option[Description],
                dueOn: LocalDate,
                amount: Amount)
