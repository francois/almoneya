-- Verify almoneya:tables/accounts on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT tenant_id, account_id, account_name, account_kind, virtual, created_at, updated_at
  FROM public.accounts
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
