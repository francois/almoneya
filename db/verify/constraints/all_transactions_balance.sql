-- Verify almoneya:constraints/all_transactions_balance on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT has_function_privilege('public.check_all_transaction_entries_balance()', 'execute');

ROLLBACK;

-- vim: expandtab shiftwidth=2
