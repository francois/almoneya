-- Deploy almoneya:tables/userpass_sign_ins to pg
-- requires: tables/sign_ins
-- requires: tables/user_userpass_credentials

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE credentials.userpass_sign_ins(
      sign_in_id int not null
    , username text not null
    , created_at timestamp with time zone not null
    , updated_at timestamp with time zone not null
    , primary key(sign_in_id)
    , foreign key(sign_in_id) references credentials.sign_ins
  );

  ALTER TABLE credentials.userpass_sign_ins OWNER TO almoneya;
  GRANT INSERT ON credentials.userpass_sign_ins TO webui;

  COMMENT ON TABLE credentials.userpass_sign_ins IS 'Records each authentication attempt against each username separately, in order to track attacks against specific users';
  COMMENT ON COLUMN credentials.userpass_sign_ins.username IS 'Records the username that was used on the form. May or may not refer to a row in credentials.user_userpass_credentials';

  CREATE TRIGGER maintain_credentials__userpass_sign_ins_timestamps
  BEFORE INSERT OR UPDATE OF username
  ON credentials.userpass_sign_ins FOR EACH ROW
  EXECUTE PROCEDURE public.maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
