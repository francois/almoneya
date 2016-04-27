-- Deploy acctsoft:tables/user_twitter_credentials to pg
-- requires: tables/users

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE credentials.user_twitter_credentials(
      user_id int not null
    , service_id bigint not null check(service_id > 0)
    , screen_name text not null check(length(screen_name) > 0 AND trim(screen_name) = screen_name)
    , consumer_key text not null check(length(consumer_key) > 0)
    , consumer_secret text not null check(length(consumer_secret) > 0)
    , access_token text not null check(length(access_token) > 0)
    , access_token_secret text not null check(length(access_token_secret) > 0)
    , created_at timestamp with time zone not null default current_timestamp
    , updated_at timestamp with time zone not null default current_timestamp
    , primary key(user_id, service_id)
    , foreign key(user_id) references public.users on update cascade on delete cascade
    , unique(access_token, access_token_secret)
  );

  ALTER TABLE user_twitter_credentials OWNER TO acctsoft;
  GRANT INSERT, UPDATE, DELETE ON credentials.user_twitter_credentials TO webui;

  COMMENT ON TABLE credentials.user_twitter_credentials IS 'A table that allows authentication using Twitter''s Sign In button';
  COMMENT ON COLUMN credentials.user_twitter_credentials.service_id IS 'This is the actual user ID on Twitter''s platform of the user that this row represents';

  CREATE TRIGGER maintain_credentials__user_twitter_credentials_timestamps
  BEFORE INSERT OR UPDATE OF service_id, screen_name, consumer_key, consumer_secret, access_token, access_token_secret
  ON credentials.user_twitter_credentials FOR EACH ROW
  EXECUTE PROCEDURE public.maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
