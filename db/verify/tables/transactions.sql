-- Verify almoneya:tables/transactions on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT tenant_id, transaction_id, payee, description, posted_on, booked_at, created_at, updated_at
  FROM public.transactions
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
