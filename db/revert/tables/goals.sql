-- Revert almoneya:tables/goals from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS public.goals;

COMMIT;

-- vim: expandtab shiftwidth=2
