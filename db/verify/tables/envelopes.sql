-- Verify almoneya:tables/envelopes on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT envelope_id, envelope_name, created_at, updated_at
  FROM public.envelopes
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
