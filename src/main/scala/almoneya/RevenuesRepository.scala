package almoneya

import org.joda.time.LocalDate

import scala.util.Try

class RevenuesRepository(executor: QueryExecutor) {

    import RevenuesRepository.FIND_ALL_QUERY

    def findAll(tenantId: TenantId): Try[Set[Revenue]] =
        executor.findAll(FIND_ALL_QUERY, tenantId) { rs =>
            Revenue(id = Some(RevenueId(rs.getInt("revenue_id"))),
                name = RevenueName(rs.getString("revenue_name")),
                startOn = new LocalDate(rs.getDate("start_on")),
                endOn = Option(rs.getDate("end_on")).map(new LocalDate(_)),
                amount = Amount(rs.getBigDecimal("amount")),
                every = Every(rs.getInt("every")),
                period = rs.getString("period") match {
                    case "day" => Daily
                    case "week" => Weekly
                    case "month" => Monthly
                    case "year" => Yearly
                })
        }.map(_.toSet)
}

object RevenuesRepository {
    val FIND_ALL_QUERY = Query("SELECT revenue_id, revenue_name, start_on, end_on, every, period, amount FROM public.revenues WHERE tenant_id = ?")
}
