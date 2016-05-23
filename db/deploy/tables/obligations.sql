-- Deploy almoneya:tables/obligations to pg
-- requires: tables/accounts

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.obligations(
      tenant_id int not null
    , account_name text not null
    , description text check(description is null or trim(description) = description)
    , every int not null check(every >= 1)
    , period text not null check(period in ('day', 'week', 'month', 'quarter', 'semester', 'year'))
    , start_on date not null default current_date
    , end_on date check(end_on is null or end_on > start_on)
    , amount numeric not null check(amount > 0)
    , obligation_id serial not null unique
    , created_at timestamp with time zone not null
    , updated_at timestamp with time zone not null
    , primary key(tenant_id, account_name)
    , foreign key(tenant_id, account_name) references public.accounts on update cascade on delete cascade
    , constraint end_on_is_unknown_or_after_start_on check(end_on is null or end_on > start_on)
  );

  ALTER TABLE public.obligations OWNER TO almoneya;

  COMMENT ON TABLE public.obligations IS 'Records a company''s obligations; obligations that must be paid monthly or weekly, such as the electricity bill, the delivery truck''s payment or the mortgate';
  COMMENT ON COLUMN public.obligations.every IS 'Records the frequency of the period: every 2 months, every 3 years, every 1 week.';
  COMMENT ON COLUMN public.obligations.period IS 'Records the "distance" between each instance of the obligation.';
  COMMENT ON COLUMN public.obligations.end_on IS 'Records a possible end date for this obligation: the date after which this obligation expires. Imagine the delivery truck''s payment, after 4 years, we won''t have to pay it anymore.';

  CREATE TRIGGER maintain_public__obligations
  BEFORE INSERT OR UPDATE OF every, period, start_on, end_on
  ON public.obligations
  FOR EACH ROW EXECUTE PROCEDURE maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
