-- Deploy acctsoft:tables/recurring_obligations to pg
-- requires: tables/obligations

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.recurring_obligations(
      tenant_id int not null
    , envelope_name text not null
    , every int not null check(every >= 1)
    , period text not null check(period in ('day', 'week', 'month', 'quarter', 'semester', 'year'))
    , start_on date not null default current_date
    , end_on date
    , amount numeric not null check(amount > 0)
    , recurring_obligation_id serial not null unique
    , created_at timestamp with time zone not null
    , updated_at timestamp with time zone not null
    , primary key(tenant_id, envelope_name)
    , foreign key(tenant_id, envelope_name) references public.obligations on update cascade on delete cascade
    , constraint end_on_is_unknown_or_after_start_on check(end_on is null or end_on > start_on)
  );

  ALTER TABLE public.recurring_obligations OWNER TO acctsoft;

  COMMENT ON TABLE public.recurring_obligations IS 'Records a company''s recurring obligations; obligations that are present monthly or weekly, such as the electricity bill, or the delivery truck''s payment or the mortgate';
  COMMENT ON COLUMN public.recurring_obligations.every IS 'Records the frequency of the period: every 2 months, every 3 years, every 1 week.';
  COMMENT ON COLUMN public.recurring_obligations.period IS 'Records the "distance" between each instance of the obligation.';
  COMMENT ON COLUMN public.recurring_obligations.end_on IS 'Records a possible end date for this recurring obligation: the date after which this obligation expires. Imagine the delivery truck''s payment, after 4 years, we won''t have to pay it anymore.';

  CREATE TRIGGER maintain_public__recurring_obligations
  BEFORE INSERT OR UPDATE OF every, period, start_on, end_on
  ON public.recurring_obligations
  FOR EACH ROW EXECUTE PROCEDURE maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
