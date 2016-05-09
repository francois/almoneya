-- Revert almoneya:constraints/all_transactions_balance from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP FUNCTION IF EXISTS public.check_all_transaction_entries_balance() CASCADE;

COMMIT;

-- vim: expandtab shiftwidth=2
