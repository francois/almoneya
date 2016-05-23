package almoneya

import org.joda.time.LocalDate

case class Goal(id: Option[GoalId] = None,
                envelope: Envelope,
                description: Option[Description],
                dueOn: LocalDate,
                amount: Amount)
