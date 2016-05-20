package almoneya.automation

sealed trait Period

case object Daily extends Period

case object Weekly extends Period

case object Monthly extends Period

case object Yearly extends Period
