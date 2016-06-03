-- Verify almoneya:tables/bank_accounts on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT tenant_id, bank_account_id, bank_account_hash, bank_account_last4, account_name, created_at, updated_at
  FROM public.bank_accounts
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
