-- Revert almoneya:tables/reconciliations from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS public.reconciliations;

COMMIT;

-- vim: expandtab shiftwidth=2
