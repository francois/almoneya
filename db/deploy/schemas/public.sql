-- Deploy acctsoft:schemas/public to pg
-- requires: create_roles

SET client_min_messages TO 'warning';

BEGIN;

  -- Truly empty the database schema
  DROP SCHEMA IF EXISTS public CASCADE;

  CREATE SCHEMA public;
  ALTER SCHEMA public OWNER TO acctsoft;
  ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON TABLES TO webui;
  ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE ON SEQUENCES TO webui;
  ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT EXECUTE ON FUNCTIONS TO webui;
  GRANT USAGE ON SCHEMA public TO webui;
  COMMENT ON SCHEMA public IS 'The default application schema';

COMMIT;

-- vim: expandtab shiftwidth=2
