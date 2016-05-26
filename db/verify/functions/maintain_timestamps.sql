-- Verify almoneya:functions/maintain_timestamps on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT pg_catalog.has_function_privilege('webui', 'public.maintain_timestamps()', 'execute');

ROLLBACK;

-- vim: expandtab shiftwidth=2
