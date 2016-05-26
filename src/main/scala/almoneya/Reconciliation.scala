package almoneya

import org.joda.time.{DateTime, LocalDate}

case class Reconciliation(id: Option[ReconciliationId] = None,
                          accountName: AccountName,
                          postedOn: LocalDate,
                          openingBalance: Amount,
                          endingBalance: Amount,
                          notes: Option[Notes] = None,
                          closedAt: Option[DateTime] = None,
                          entries: Set[ReconciliationEntry] = Set.empty)
