-- Verify almoneya:constraints/cannot_modify_reconciled_transaction on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT has_function_privilege('public.check_cannot_modify_reconciled_transaction()', 'execute');
  SELECT has_function_privilege('public.check_cannot_add_or_remove_reconciliation_entries_on_closed_reconciliation()', 'execute');

ROLLBACK;

-- vim: expandtab shiftwidth=2
