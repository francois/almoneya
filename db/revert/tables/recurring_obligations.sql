-- Revert acctsoft:tables/recurring_obligations from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS public.recurring_obligations;

COMMIT;

-- vim: expandtab shiftwidth=2
