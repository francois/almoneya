-- Deploy acctsoft:tables/envelopes to pg
-- requires: tables/tenants

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.envelopes(
      tenant_id int not null
    , envelope_name text not null check(trim(envelope_name) = envelope_name)
    , envelope_id serial not null unique
    , created_at timestamp with time zone not null
    , updated_at timestamp with time zone not null
    , primary key(tenant_id, envelope_name)
    , foreign key(tenant_id) references tenants on update cascade on delete cascade
  );

  ALTER TABLE public.envelopes OWNER TO acctsoft;

  COMMENT ON TABLE public.envelopes IS 'Records the different envelopes the system knows about. Envelopes are used to stash money away in bank accounts, in order to meet obligations or goals.';

  CREATE TRIGGER maintain_public__envelopes_timestamps
  BEFORE INSERT OR UPDATE OF envelope_name
  ON public.envelopes
  FOR EACH ROW EXECUTE PROCEDURE public.maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
