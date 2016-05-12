-- Verify almoneya:tables/obligations on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT tenant_id, envelope_name, description, every, period, start_on, end_on, obligation_id, created_at, updated_at
  FROM public.obligations
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
