-- Deploy almoneya:migrations/link_users_to_tenants to pg
-- requires: tables/users
-- requires: tables/tenants

SET client_min_messages TO 'warning';

BEGIN;

  ALTER TABLE public.users ADD COLUMN tenant_id int NOT NULL REFERENCES tenants;

COMMIT;

-- vim: expandtab shiftwidth=2
