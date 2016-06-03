-- Deploy almoneya:tables/transactions to pg
-- requires: tables/tenants

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.transactions(
      tenant_id int not null
    , payee text not null check(trim(payee) = payee and length(payee) > 1)
    , description text check(description is null or (trim(description) = description))
    , posted_on date not null
    , booked_at timestamp with time zone not null default current_timestamp
    , transaction_id serial not null unique
    , created_at timestamp with time zone not null
    , updated_at timestamp with time zone not null

    , primary key(tenant_id, transaction_id)
    , foreign key(tenant_id) references credentials.tenants on update cascade on delete cascade
  );

  ALTER TABLE public.transactions OWNER TO almoneya;

  CREATE INDEX transactions_by_posted_on ON public.transactions(posted_on, booked_at);

  COMMENT ON TABLE public.transactions IS 'The General Ledger: describes each monetary transaction that occurs in this company';
  COMMENT ON COLUMN public.transactions.payee IS 'Indicates to whom the money moved from/to';
  COMMENT ON COLUMN public.transactions.description IS 'Describes the transaction in more details';
  COMMENT ON COLUMN public.transactions.posted_on IS 'The date at which the transaction occurred, which may not be the time when we learned about the transaction';
  COMMENT ON COLUMN public.transactions.booked_at IS 'The moment in time when this transaction was made known to the system';

  CREATE TRIGGER maintain_public__transactions_timestamps
  BEFORE INSERT OR UPDATE OF payee, description, posted_on, booked_at
  ON public.transactions
  FOR EACH ROW EXECUTE PROCEDURE public.maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
