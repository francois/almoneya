-- Revert acctsoft:tables/obligations from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS public.obligations;

COMMIT;

-- vim: expandtab shiftwidth=2
