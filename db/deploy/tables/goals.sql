-- Deploy almoneya:tables/goals to pg
-- requires: tables/accounts

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.goals(
      tenant_id int not null
    , account_name text not null
    , description text check(description is null or trim(description) = description)
    , due_on date not null
    , amount numeric not null check(amount > 0)
    , goal_id serial not null unique
    , created_at timestamp with time zone not null
    , updated_at timestamp with time zone not null
    , primary key(tenant_id, account_name)
    , foreign key(tenant_id, account_name) references public.accounts on update cascade on delete cascade
  );

  ALTER TABLE public.goals OWNER TO almoneya;

  COMMENT ON TABLE public.goals IS 'Records a company''s goals: saving up for a new fridge or a new delivery truck, for example';

  CREATE TRIGGER maintain_public__goals
  BEFORE INSERT OR UPDATE OF account_name, description, due_on, amount
  ON public.goals
  FOR EACH ROW EXECUTE PROCEDURE maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
