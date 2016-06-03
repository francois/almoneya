-- Deploy almoneya:tables/reconciliation_entries to pg
-- requires: tables/reconciliations
-- requires: tables/transaction_entries

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.reconciliation_entries(
      tenant_id int not null
    , transaction_id int not null
    , account_name text not null
    , posted_on date not null
    , reconciliation_entry_id serial not null unique
    , created_at timestamp with time zone not null
    , updated_at timestamp with time zone not null

    , primary key(tenant_id, transaction_id, account_name)
    , foreign key(tenant_id, transaction_id, account_name) references public.transaction_entries on update cascade on delete restrict
    , foreign key(tenant_id, posted_on, account_name) references public.reconciliations on update cascade on delete cascade
  );

  ALTER TABLE public.reconciliation_entries OWNER TO almoneya;

  COMMENT ON TABLE public.reconciliation_entries IS 'Records which transactions have been reconciled with a bank statement';
  COMMENT ON COLUMN public.reconciliation_entries.transaction_id IS 'The foreign key reference to a single transaction entry';
  COMMENT ON COLUMN public.reconciliation_entries.account_name IS 'The foreign key reference to a single transaction entry';
  COMMENT ON COLUMN public.reconciliation_entries.posted_on IS 'The date on the bank''s statement';

  CREATE TRIGGER maintain_public__reconciliation_entries_timestamps
  BEFORE INSERT OR UPDATE OF transaction_id, account_name, posted_on
  ON public.reconciliation_entries
  FOR EACH ROW EXECUTE PROCEDURE public.maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
