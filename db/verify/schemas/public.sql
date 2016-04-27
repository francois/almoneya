-- Verify acctsoft:schemas/public on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT pg_catalog.has_schema_privilege('webui', 'public', 'usage');

ROLLBACK;

-- vim: expandtab shiftwidth=2
