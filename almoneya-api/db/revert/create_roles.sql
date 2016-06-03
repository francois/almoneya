-- Revert almoneya:create_roles from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP ROLE IF EXISTS almoneya;
  DROP ROLE IF EXISTS webui;

COMMIT;

-- vim: expandtab shiftwidth=2
