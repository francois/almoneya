-- Revert almoneya:tables/bank_accounts from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS public.bank_accounts;

COMMIT;

-- vim: expandtab shiftwidth=2
