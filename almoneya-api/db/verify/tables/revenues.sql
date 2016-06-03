-- Verify almoneya:tables/revenues on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT tenant_id, revenue_id, revenue_name, start_on, end_on, every, period, amount, created_at, updated_at
  FROM public.revenues
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
