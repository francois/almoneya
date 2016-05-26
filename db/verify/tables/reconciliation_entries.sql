-- Verify almoneya:tables/reconciliation_entries on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT tenant_id, transaction_id, account_name, posted_on, reconciliation_entry_id, created_at, updated_at
  FROM public.reconciliation_entries
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
