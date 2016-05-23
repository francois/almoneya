-- Deploy almoneya:tables/revenues to pg
-- requires: tables/tenants

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.revenues(
      tenant_id int not null
    , revenue_name text not null check(revenue_name = trim(revenue_name))
    , start_on date not null
    , end_on date check(end_on is null or end_on > start_on)
    , every int not null check(every >= 1)
    , period text not null check(period in ('day', 'week', 'month', 'quarter', 'semester', 'year'))
    , amount numeric not null check(amount > 0)
    , revenue_id serial not null unique
    , created_at timestamp with time zone not null
    , updated_at timestamp with time zone not null

    , primary key(tenant_id, revenue_name)
    , foreign key(tenant_id) references tenants
  );

  ALTER TABLE public.revenues OWNER TO almoneya;

  CREATE TRIGGER maintain_public__revenues_timestamps
  BEFORE INSERT OR UPDATE OF revenue_name, start_on, end_on, every, period, amount
  ON public.revenues
  FOR EACH ROW EXECUTE PROCEDURE public.maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
