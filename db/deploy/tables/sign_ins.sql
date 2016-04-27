-- Deploy acctsoft:tables/sign_ins to pg
-- requires: schemas/credentials
-- requires: functions/maintain_timestamps

SET client_min_messages TO 'warning';

BEGIN;

  CREATE TABLE credentials.sign_ins(
      sign_in_id serial not null primary key
    , source_ip inet not null
    , user_agent text not null
    , method text not null check(method in ('userpass', 'twitter', 'facebook'))
    , created_at timestamp with time zone not null default current_timestamp
    , updated_at timestamp with time zone not null default current_timestamp
  );

  ALTER TABLE credentials.sign_ins OWNER TO acctsoft;
  GRANT INSERT ON credentials.sign_ins TO webui; -- append only table for webui user

  COMMENT ON TABLE credentials.sign_ins IS 'Records sign in attempts, in order to track potential threaths to the application';
  COMMENT ON COLUMN credentials.sign_ins.source_ip IS 'From which IP address did the sign in attempt originate; be careful as reverse proxies will influence this IP address';
  COMMENT ON COLUMN credentials.sign_ins.user_agent IS 'The value stored in the User-Agent HTTP header, which may or may not have been provided intially';
  COMMENT ON COLUMN credentials.sign_ins.method IS 'Which authentication method was used to attempt this sign in attempt?';

  CREATE TRIGGER maintain_credentials__sign_ins_timestamps
  BEFORE INSERT ON credentials.sign_ins FOR EACH ROW
  EXECUTE PROCEDURE public.maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
