-- Revert almoneya:constraints/cannot_modify_reconciled_transaction from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP FUNCTION IF EXISTS public.check_cannot_modify_reconciled_transaction() CASCADE;
  DROP FUNCTION IF EXISTS public.check_cannot_add_or_remove_reconciliation_entries_on_closed_reconciliation() CASCADE;

COMMIT;

-- vim: expandtab shiftwidth=2
