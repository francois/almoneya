-- Revert almoneya:tables/transaction_entries from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS public.transaction_entries;

COMMIT;

-- vim: expandtab shiftwidth=2
