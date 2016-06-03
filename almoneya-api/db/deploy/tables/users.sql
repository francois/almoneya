-- Deploy almoneya:tables/users to pg
-- requires: schemas/public
-- requires: create_roles

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE public.users(
      tenant_id int not null
    , user_id serial not null
    , surname text not null check(length(surname) > 0)
    , rest_of_name text
    , created_at timestamp with time zone not null default current_timestamp
    , updated_at timestamp with time zone not null default current_timestamp
    , primary key(user_id)
    , foreign key(tenant_id) references credentials.tenants on update cascade on delete cascade
  );

  ALTER TABLE public.users OWNER TO almoneya;
  GRANT INSERT, UPDATE, DELETE ON public.users TO webui;

  COMMENT ON TABLE public.users IS 'A place to store people who have the potential to access the system. Inspired by http://tdan.com/a-universal-person-and-organization-data-model/5014';

  CREATE TRIGGER maintain_public__users_timestamps
  BEFORE INSERT OR UPDATE OF surname, rest_of_name
  ON public.users FOR EACH ROW
  EXECUTE PROCEDURE public.maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
