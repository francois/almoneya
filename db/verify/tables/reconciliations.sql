-- Verify almoneya:tables/reconciliations on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT
      tenant_id
    , account_name
    , posted_on
    , opening_balance
    , ending_balance
    , notes
    , closed_at
    , reconciliation_id
    , created_at
    , updated_at
  FROM public.reconciliations
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
