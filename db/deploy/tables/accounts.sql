-- Deploy acctsoft:tables/accounts to pg
-- requires: tables/tenants

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.accounts(
      tenant_id int not null references credentials.tenants
    , account_code text          check(account_code IS NULL OR (trim(account_code) = account_code AND length(account_code) > 0))
    , account_name text not null check(trim(account_name) = account_name AND length(account_name) > 0)
    , account_kind text not null check(account_kind IN ('asset', 'liability', 'equity', 'revenue', 'expense', 'contra'))
    , account_id serial not null unique
    , created_at timestamp with time zone
    , updated_at timestamp with time zone
    , primary key(tenant_id, account_name)
  );

  ALTER TABLE public.accounts OWNER TO acctsoft;

  COMMENT ON TABLE public.accounts IS 'Holds the chart of accounts for each tenant';
  COMMENT ON COLUMN public.accounts.tenant_id IS 'The tenant which owns this row';
  COMMENT ON COLUMN public.accounts.account_code IS 'The tenant''s internal code to identify this account, usually some kind of number';
  COMMENT ON COLUMN public.accounts.account_name IS 'The name of this account';
  COMMENT ON COLUMN public.accounts.account_kind IS 'The kind of account. See https://en.wikipedia.org/wiki/Chart_of_accounts#Types_of_accounts';
  COMMENT ON COLUMN public.accounts.account_id IS 'An internal ID for this account, only used in CRUD queries';

  CREATE TRIGGER maintain_public__accounts_timestamps
  BEFORE INSERT OR UPDATE OF account_name, account_kind
  ON public.accounts
  FOR EACH ROW EXECUTE PROCEDURE public.maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
