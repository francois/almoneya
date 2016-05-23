package almoneya

import org.joda.time.PeriodType

sealed trait Period {
    def periodType: PeriodType
}

case object Daily extends Period {
    override def periodType: PeriodType = PeriodType.days()
}

case object Weekly extends Period {
    override def periodType: PeriodType = PeriodType.weeks()
}

case object Monthly extends Period {
    override def periodType: PeriodType = PeriodType.months()
}

case object Yearly extends Period {
    override def periodType: PeriodType = PeriodType.years()
}
