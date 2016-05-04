-- Verify acctsoft:tables/one_time_obligations on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT tenant_id, envelope_name, amount, due_on, one_time_obligation_id, created_at, updated_at
  FROM public.one_time_obligations
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
