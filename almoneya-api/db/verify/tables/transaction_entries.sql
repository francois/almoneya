-- Verify almoneya:tables/transaction_entries on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT tenant_id, transaction_id, account_name, amount, created_at, updated_at
  FROM public.transaction_entries
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
