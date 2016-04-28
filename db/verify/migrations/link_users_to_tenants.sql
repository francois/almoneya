-- Verify acctsoft:migrations/link_users_to_tenants on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT tenant_id
  FROM public.users
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
