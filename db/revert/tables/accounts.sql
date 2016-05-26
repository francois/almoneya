-- Revert almoneya:tables/accounts from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS public.accounts;

COMMIT;

-- vim: expandtab shiftwidth=2
