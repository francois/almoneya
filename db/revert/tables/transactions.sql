-- Revert almoneya:tables/transactions from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS public.transactions;

COMMIT;

-- vim: expandtab shiftwidth=2
