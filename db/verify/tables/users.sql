-- Verify almoneya:tables/users on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT user_id, surname, rest_of_name, created_at, updated_at
  FROM public.users
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
