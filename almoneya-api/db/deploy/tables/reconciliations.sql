-- Deploy almoneya:tables/reconciliations to pg
-- requires: tables/accounts

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.reconciliations(
      tenant_id int not null
    , account_name text not null
    , posted_on date not null
    , opening_balance numeric not null
    , ending_balance numeric not null
    , notes text check(notes is null or trim(notes) = notes)
    , closed_at timestamp with time zone
    , reconciliation_id serial not null unique
    , created_at timestamp with time zone not null
    , updated_at timestamp with time zone not null

    , primary key(tenant_id, posted_on, account_name)
    , foreign key(tenant_id, account_name) references public.accounts on update cascade on delete cascade
  );

  ALTER TABLE public.reconciliations OWNER TO almoneya;

  COMMENT ON TABLE public.reconciliations IS '';
  COMMENT ON COLUMN public.reconciliations.posted_on IS 'The date on the bank''s statement';
  COMMENT ON COLUMN public.reconciliations.opening_balance IS 'The opening balance on the bank''s statement';
  COMMENT ON COLUMN public.reconciliations.ending_balance IS 'The ending balance on the bank''s statement';
  COMMENT ON COLUMN public.reconciliations.notes IS 'A pure notebook where people can record what they want about the statement';
  COMMENT ON COLUMN public.reconciliations.closed_at IS 'When this is NOT NULL, indicates we want to prevent transactions and transaction_entries from being added, modified or deleted';

  CREATE TRIGGER maintain_public__reconciliations_timestamps
  BEFORE INSERT OR UPDATE OF account_name, posted_on, opening_balance, ending_balance, notes, closed_at
  ON public.reconciliations
  FOR EACH ROW EXECUTE PROCEDURE public.maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
