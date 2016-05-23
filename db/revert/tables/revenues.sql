-- Revert almoneya:tables/revenues from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS public.revenues;

COMMIT;

-- vim: expandtab shiftwidth=2
