-- Verify almoneya:schemas/credentials on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT 1/sum(n) FROM (VALUES(1, pg_catalog.has_schema_privilege('webui', 'credentials', 'usage'))) t0(n, perm) WHERE perm;

ROLLBACK;

-- vim: expandtab shiftwidth=2
