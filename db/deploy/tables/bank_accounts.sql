-- Deploy almoneya:tables/bank_accounts to pg
-- requires: tables/accounts

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.bank_accounts(
      tenant_id int not null
    , bank_account_hash text not null
    , bank_account_last4 char(4) not null check(trim(bank_account_last4) = bank_account_last4)
    , account_name text
    , bank_account_id serial not null unique
    , created_at timestamp with time zone not null
    , updated_at timestamp with time zone not null
    , primary key(tenant_id, bank_account_hash)
    , foreign key(tenant_id) references credentials.tenants on update cascade on delete cascade
    , foreign key(tenant_id, account_name) references public.accounts on update cascade on delete cascade
    , unique(tenant_id, bank_account_hash, account_name)
  );

  ALTER TABLE public.bank_accounts OWNER TO almoneya;
  GRANT INSERT, SELECT, UPDATE ON public.bank_accounts TO webui;

  COMMENT ON TABLE public.bank_accounts IS 'Records which bank accounts map to which accounts. Used during importing to determine where to post transactions.';
  COMMENT ON COLUMN public.bank_accounts.bank_account_hash IS 'The SHA256 hash of the bank account''s number';
  COMMENT ON COLUMN public.bank_accounts.bank_account_last4 IS 'The last 4 digits of the bank account';

  CREATE TRIGGER maintain_public__bank_accounts_timestamps
  BEFORE INSERT OR UPDATE OF bank_account_hash, bank_account_last4, account_name
  ON public.bank_accounts
  FOR EACH ROW EXECUTE PROCEDURE maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
