-- Revert almoneya:schemas/public from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP SCHEMA IF EXISTS public CASCADE;

COMMIT;

-- vim: expandtab shiftwidth=2
