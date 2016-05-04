-- Deploy almoneya:create_roles to pg

SET client_min_messages TO 'warning';

BEGIN;

  CREATE ROLE almoneya WITH nosuperuser nocreatedb noinherit nologin noreplication;
  COMMENT ON ROLE almoneya IS 'The owner of database objects: this role should never be able to login';

  CREATE ROLE webui WITH nosuperuser nologin nocreatedb nocreaterole inherit noreplication;
  COMMENT ON ROLE webui IS 'The database role with which the Web UI authenticates to the database';

COMMIT;

-- vim: expandtab shiftwidth=2
