-- Revert almoneya:tables/reconciliation_entries from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS public.reconciliation_entries;

COMMIT;

-- vim: expandtab shiftwidth=2
