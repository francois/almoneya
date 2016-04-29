-- Revert acctsoft:tables/bank_account_transactions from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS public.bank_account_transactions;

COMMIT;

-- vim: expandtab shiftwidth=2
