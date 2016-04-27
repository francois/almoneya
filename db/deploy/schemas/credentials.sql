-- Deploy acctsoft:schemas/credentials to pg
-- requires: create_roles

SET client_min_messages TO 'warning';

BEGIN;

  CREATE SCHEMA credentials;
  ALTER SCHEMA credentials OWNER TO acctsoft;
  GRANT USAGE ON SCHEMA credentials TO webui;
  ALTER DEFAULT PRIVILEGES IN SCHEMA credentials GRANT SELECT ON TABLES TO webui;
  ALTER DEFAULT PRIVILEGES IN SCHEMA credentials GRANT USAGE ON SEQUENCES TO webui;
  COMMENT ON SCHEMA credentials IS 'The schema for anything that is security-sensitive. This schema is access restricted to SELECT only for the web tier.';

COMMIT;

-- vim: expandtab shiftwidth=2
