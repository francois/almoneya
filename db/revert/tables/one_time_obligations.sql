-- Revert acctsoft:tables/one_time_obligations from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS public.one_time_obligations;

COMMIT;

-- vim: expandtab shiftwidth=2
