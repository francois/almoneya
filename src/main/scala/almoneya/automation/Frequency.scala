package almoneya.automation

case class Frequency(value: Int) {
    assert(value > 0)

    def toPeriod(period: Period): org.joda.time.Period = period match {
        case Daily => new org.joda.time.Period(0, 0, 0, value, 0, 0, 0, 0, period.periodType)
        case Weekly => new org.joda.time.Period(0, 0, value, 0, 0, 0, 0, 0, period.periodType)
        case Monthly => new org.joda.time.Period(0, value, 0, 0, 0, 0, 0, 0, period.periodType)
        case Yearly => new org.joda.time.Period(value, 0, 0, 0, 0, 0, 0, 0, period.periodType)
    }
}
