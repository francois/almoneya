-- Deploy almoneya:tables/obligations to pg
-- requires: tables/accounts

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.obligations(
      tenant_id int not null
    , envelope_name text not null
    , description text check(description is null or trim(description) = description)
    , obligation_id serial not null unique
    , created_at timestamp with time zone not null
    , updated_at timestamp with time zone not null
    , primary key(tenant_id, envelope_name)
    , foreign key(tenant_id, envelope_name) references public.envelopes on update cascade on delete cascade
  );

  ALTER TABLE public.obligations OWNER TO almoneya;

  COMMENT ON TABLE public.obligations IS 'Records a company''s obligations: money that we''re putting aside now, in order to pay something at a future date';

  CREATE TRIGGER maintain_public__obligations_timestamps
  BEFORE INSERT OR UPDATE OF envelope_name, description
  ON public.obligations
  FOR EACH ROW EXECUTE PROCEDURE maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
