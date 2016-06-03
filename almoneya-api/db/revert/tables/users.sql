-- Revert almoneya:tables/users from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS public.users;

COMMIT;

-- vim: expandtab shiftwidth=2
