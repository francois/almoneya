-- Revert acctsoft:schemas/credentials from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP SCHEMA IF EXISTS credentials CASCADE;

COMMIT;

-- vim: expandtab shiftwidth=2
