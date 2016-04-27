-- Deploy acctsoft:tables/user_userpass_credentials to pg
-- requires: tables/users

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE credentials.user_userpass_credentials(
      user_id int not null
    , username text not null check(length(username) > 4 AND trim(username) = username AND lower(username) = username)
    , password_hash text not null
    , created_at timestamp with time zone not null default current_timestamp
    , updated_at timestamp with time zone not null default current_timestamp
    , primary key(user_id, username)
    , foreign key(user_id) references public.users on update cascade on delete cascade
    , unique(username)
  );

  ALTER TABLE credentials.user_userpass_credentials OWNER TO acctsoft;

  COMMENT ON TABLE credentials.user_userpass_credentials IS 'Records usernames (or emails) and passwords';
  COMMENT ON COLUMN credentials.user_userpass_credentials.username IS 'Represents the identity of a person, usually as an email address or as a plain username. If the application requires the use of an email address, this column''s check must be updated.';
  COMMENT ON COLUMN credentials.user_userpass_credentials.password_hash IS 'Should be a BCrypt password hash, but there are no real restrictions on this';

  CREATE TRIGGER maintain_credentials__user_userpass_credentials_timestamps
  BEFORE INSERT OR UPDATE OF username, password_hash
  ON credentials.user_userpass_credentials FOR EACH ROW
  EXECUTE PROCEDURE public.maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
