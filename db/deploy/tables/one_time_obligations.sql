-- Deploy almoneya:tables/one_time_obligations to pg
-- requires: tables/obligations

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.one_time_obligations(
      tenant_id int not null
    , envelope_name text not null
    , due_on date not null
    , amount numeric not null check(amount > 0)
    , one_time_obligation_id serial not null unique
    , created_at timestamp with time zone not null
    , updated_at timestamp with time zone not null
    , primary key(tenant_id, envelope_name)
    , foreign key(tenant_id, envelope_name) references public.obligations on update cascade on delete cascade
  );

  ALTER TABLE public.one_time_obligations OWNER TO almoneya;

  COMMENT ON TABLE public.one_time_obligations IS 'Records a company''s one time obligations: saving up for a new fridge or a new delivery truck, for example';

  CREATE TRIGGER maintain_public__one_time_obligations
  BEFORE INSERT OR UPDATE OF due_on, amount
  ON public.one_time_obligations
  FOR EACH ROW EXECUTE PROCEDURE maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
