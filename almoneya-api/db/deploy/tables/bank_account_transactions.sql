-- Deploy almoneya:tables/bank_account_transactions to pg
-- requires: tables/bank_accounts

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.bank_account_transactions(
      tenant_id int not null
    , bank_account_hash text not null
    , posted_on date not null
    , description1 text not null check(trim(description1) = description1 AND length(description1) > 0)
    , description2 text
    , check_number text
    , amount numeric not null
    , transaction_id int
    , account_name text
    , bank_account_transaction_id serial not null
    , created_at timestamp with time zone not null
    , updated_at timestamp with time zone not null
    , primary key(tenant_id, bank_account_transaction_id)
    , foreign key(tenant_id, bank_account_hash, account_name) references public.bank_accounts(tenant_id, bank_account_hash, account_name) on update cascade on delete cascade
    , foreign key(tenant_id, transaction_id, account_name) references public.transaction_entries on update cascade on delete cascade
  );

  CREATE INDEX index_bank_account_transactions_on_tenant_bank_account_hash ON public.bank_account_transactions(tenant_id, bank_account_hash, posted_on);
  CREATE INDEX index_bank_account_transactions_on_tenant_posted_on ON public.bank_account_transactions(tenant_id, posted_on, bank_account_hash);

  ALTER TABLE public.bank_account_transactions OWNER TO almoneya;
  GRANT INSERT, SELECT, UPDATE ON public.bank_account_transactions TO webui;

  COMMENT ON TABLE public.bank_account_transactions IS 'Records bank transactions imported from remote systems into our own system';

  CREATE TRIGGER maintain_public__bank_account_transactions
  BEFORE INSERT OR UPDATE OF posted_on, description1, description2
  ON public.bank_account_transactions
  FOR EACH ROW EXECUTE PROCEDURE maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
