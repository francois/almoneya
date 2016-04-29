-- Verify acctsoft:tables/bank_account_transactions on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT tenant_id, bank_account_hash, posted_on, check_number, description1, description2, amount
  FROM public.bank_account_transactions
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
