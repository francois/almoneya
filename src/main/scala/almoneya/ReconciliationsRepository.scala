package almoneya

import scala.util.Try

class ReconciliationsRepository(executor: QueryExecutor) {

    import ReconciliationsRepository.{INSERT_ENTRY_QUERY, INSERT_RECONCILIATION_QUERY}

    def createReconciliation(tenantId: TenantId, reconciliation: Reconciliation): Try[Reconciliation] = {
        executor.insertOne(INSERT_RECONCILIATION_QUERY, tenantId, reconciliation.accountName, reconciliation.postedOn, reconciliation.openingBalance, reconciliation.endingBalance, reconciliation.notes) { rs =>
            reconciliation.copy(id = Some(ReconciliationId(rs.getInt("reconciliation_id"))))
        }
    }

    def createEntry(tenantId: TenantId, entry: ReconciliationEntry): Try[ReconciliationEntry] = {
        executor.insertOne(INSERT_ENTRY_QUERY, tenantId, entry.transactionId, entry.accountName, entry.postedOn) { rs =>
            entry.copy(id = Some(ReconciliationEntryId(rs.getInt("reconciliation_entry_id"))))
        }
    }
}

object ReconciliationsRepository {
    val INSERT_RECONCILIATION_QUERY = Query("INSERT INTO public.reconciliations(tenant_id, account_name, posted_on, opening_balance, ending_balance, notes) VALUES (?, ?, ?, ?, ?, ?)", Seq(Column("reconciliation_id")))
    val INSERT_ENTRY_QUERY = Query("INSERT INTO public.reconciliation_entries(tenant_id, transaction_id, account_name, posted_on) VALUES (?, ?, ?, ?)", Seq(Column("reconciliation_entry_id")))
}
