-- Verify acctsoft:create_roles on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT 1/count(*) FROM pg_roles WHERE rolname = 'acctsoft' AND rolsuper = false;
  SELECT 1/count(*) FROM pg_roles WHERE rolname = 'webui' AND rolsuper = false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
