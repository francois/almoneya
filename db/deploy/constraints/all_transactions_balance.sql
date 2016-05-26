-- Deploy almoneya:constraints/all_transactions_balance to pg
-- requires: tables/transaction_entries

SET client_min_messages TO 'warning';

BEGIN;

  CREATE OR REPLACE FUNCTION public.check_all_transaction_entries_balance() RETURNS TRIGGER AS $$
  DECLARE
    rec RECORD;
  BEGIN
    FOR rec IN
      SELECT transaction_id, sum(amount) AS balance
      FROM public.transaction_entries
      GROUP BY transaction_id
      HAVING sum(amount) <> 0
    LOOP
      RAISE EXCEPTION 'Transaction % is unbalanced; balance is %', rec.transaction_id, rec.balance;
    END LOOP;

    RETURN NEW;
  END
  $$ LANGUAGE plpgsql;

  DROP TRIGGER IF EXISTS check_all_transaction_entries_balance ON public.transaction_entries;
  CREATE TRIGGER check_all_transaction_entries_balance
  AFTER INSERT OR UPDATE OF amount OR DELETE
  ON public.transaction_entries
  FOR EACH STATEMENT EXECUTE PROCEDURE public.check_all_transaction_entries_balance();

COMMIT;

-- vim: expandtab shiftwidth=2
