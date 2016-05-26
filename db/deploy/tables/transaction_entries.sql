-- Deploy almoneya:tables/transaction_entries to pg
-- requires: tables/transactions
-- requires: tables/accounts

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.transaction_entries(
      tenant_id int not null
    , transaction_id int not null
    , account_name text not null
    , amount numeric not null

    , transaction_entry_id serial not null unique
    , created_at timestamp with time zone not null
    , updated_at timestamp with time zone not null

    , primary key(tenant_id, transaction_id, account_name)
    , foreign key(tenant_id, transaction_id) references public.transactions on update cascade on delete cascade
    , foreign key(tenant_id, account_name) references public.accounts on update cascade on delete cascade
  );

  ALTER TABLE public.transaction_entries OWNER TO almoneya;

  COMMENT ON TABLE public.transaction_entries IS '';
  COMMENT ON COLUMN public.transaction_entries.transaction_id IS 'The transaction to which this account refers';
  COMMENT ON COLUMN public.transaction_entries.account_name IS 'The account from/to which money is moving';
  COMMENT ON COLUMN public.transaction_entries.amount IS 'The actual dollar amount of money moving';

  CREATE TRIGGER maintain_public__transaction_entries_timestamps
  BEFORE INSERT OR UPDATE OF transaction_id, account_name, amount
  ON public.transaction_entries
  FOR EACH ROW EXECUTE PROCEDURE public.maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
