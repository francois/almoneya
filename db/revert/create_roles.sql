-- Revert acctsoft:create_roles from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP ROLE IF EXISTS acctsoft;
  DROP ROLE IF EXISTS webui;

COMMIT;

-- vim: expandtab shiftwidth=2
