-- Deploy almoneya:constraints/cannot_modify_reconciled_transaction to pg
-- requires: tables/reconciliation_entries

SET client_min_messages TO 'warning';

BEGIN;

  -- Check that we cannot modify transaction_entries that are reconciled
  CREATE OR REPLACE FUNCTION public.check_cannot_modify_reconciled_transaction() RETURNS TRIGGER AS $$
  DECLARE
    rec record;
  BEGIN
    FOR rec IN
      SELECT account_name, posted_on, closed_at
      FROM   public.reconciliation_entries
        JOIN public.reconciliations        USING (tenant_id, posted_on, account_name)
      WHERE tenant_id      = OLD.tenant_id
        AND account_name   = OLD.account_name
        AND transaction_id = OLD.transaction_id
        AND closed_at IS NOT NULL
    LOOP
      RAISE EXCEPTION 'Transaction entry on account "%" appears on the reconciliation statement dated %, which was closed at %',
          rec.account_name, rec.posted_on, rec.closed_at USING errcode = 'check_violation';
    END LOOP;

    RETURN OLD;
  END
  $$ LANGUAGE plpgsql;

  CREATE CONSTRAINT TRIGGER prevent_modifications_on_reconciled_transactions
  AFTER UPDATE OR DELETE
  ON public.transaction_entries
  DEFERRABLE INITIALLY IMMEDIATE
  FOR EACH ROW EXECUTE PROCEDURE public.check_cannot_modify_reconciled_transaction();

  -- Validate that we cannot change the list of transactions on a closed reconciliation
  CREATE OR REPLACE FUNCTION public.check_cannot_add_or_remove_reconciliation_entries_on_closed_reconciliation() RETURNS TRIGGER AS $$
  DECLARE
    rec record;
  BEGIN
    FOR rec IN
      SELECT account_name, closed_at
      FROM public.reconciliations
      WHERE tenant_id    = OLD.tenant_id
        AND posted_on    = OLD.posted_on
        AND account_name = OLD.account_name
        AND closed_at IS NOT NULL
    LOOP
      RAISE EXCEPTION 'Modifications on closed reconciliations disallowed; reconciliation on account "%" was closed on %',
          rec.account_name, rec.closed_at USING ERRCODE = 'check_violation';
    END LOOP;
    RETURN OLD;
  END
  $$ LANGUAGE plpgsql;

  CREATE CONSTRAINT TRIGGER prevent_modifications_on_closed_reconciliations
  AFTER INSERT OR UPDATE OR DELETE
  ON public.reconciliation_entries
  DEFERRABLE INITIALLY IMMEDIATE
  FOR EACH ROW EXECUTE PROCEDURE public.check_cannot_add_or_remove_reconciliation_entries_on_closed_reconciliation();

COMMIT;

-- vim: expandtab shiftwidth=2
