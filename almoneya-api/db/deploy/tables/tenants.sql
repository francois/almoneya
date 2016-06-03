-- Deploy almoneya:tables/tenants to pg
-- requires: schemas/credentials

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE credentials.tenants(
      tenant_id serial not null primary key
    , created_at timestamp with time zone not null
    , updated_at timestamp with time zone not null
  );

  ALTER TABLE credentials.tenants OWNER TO almoneya;
  GRANT SELECT, INSERT ON credentials.tenants TO webui;

  COMMENT ON TABLE credentials.tenants IS '';
  COMMENT ON COLUMN credentials.tenants.tenant_id IS '';

  CREATE TRIGGER maintain_credentials__tenants_timestamps
  BEFORE INSERT OR UPDATE OF tenant_id
  ON credentials.tenants
  FOR EACH ROW EXECUTE PROCEDURE public.maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
