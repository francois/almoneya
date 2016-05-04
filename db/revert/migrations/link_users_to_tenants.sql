-- Revert almoneya:migrations/link_users_to_tenants from pg

SET client_min_messages TO 'warning';

BEGIN;

  ALTER TABLE public.users DROP COLUMN tenant_id;

COMMIT;

-- vim: expandtab shiftwidth=2
