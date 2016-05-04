-- Verify acctsoft:tables/recurring_obligations on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT tenant_id, envelope_name, every, period, start_on, end_on, recurring_obligation_id, created_at, updated_at
  FROM public.recurring_obligations
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
