-- Deploy acctsoft:tables/user_facebook_credentials to pg
-- requires: tables/users

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE credentials.user_facebook_credentials(
      user_id int not null
    , service_id text not null unique check(length(service_id) > 0 AND trim(service_id) = service_id AND lower(service_id) = service_id)
    , name text not null check(length(name) > 0)
    , access_token text not null unique check(length(access_token) > 0)
    , expires_at timestamp with time zone not null
    , created_at timestamp with time zone not null default current_timestamp
    , updated_at timestamp with time zone not null default current_timestamp
    , primary key(user_id, service_id)
    , foreign key(user_id) references public.users on update cascade on delete cascade
  );

  ALTER TABLE credentials.user_facebook_credentials OWNER TO acctsoft;
  GRANT INSERT, UPDATE, DELETE ON credentials.user_facebook_credentials TO webui;

  COMMENT ON TABLE credentials.user_facebook_credentials IS 'Credentials that users can use to authenticate with this platform using Facebook''s "Sign In with Facebook" oauth flow';
  COMMENT ON COLUMN credentials.user_facebook_credentials.service_id IS 'The person''s ID on the Facebook platform';
  COMMENT ON COLUMN credentials.user_facebook_credentials.name IS 'The person''s name on the Facebook platform';
  COMMENT ON COLUMN credentials.user_facebook_credentials.expires_at IS 'The moment in time where this access token will expire';

  CREATE TRIGGER maintain_credentials__user_facebook_credentials_timestamps
  BEFORE INSERT OR UPDATE OF service_id, name, access_token, expires_at
  ON credentials.user_facebook_credentials FOR EACH ROW
  EXECUTE PROCEDURE public.maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
