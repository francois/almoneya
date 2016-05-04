-- Verify almoneya:tables/tenants on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT tenant_id, created_at, updated_at
  FROM credentials.tenants
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
