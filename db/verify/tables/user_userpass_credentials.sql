-- Verify almoneya:tables/user_userpass_credentials on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT user_id, username, password_hash, created_at, updated_at
  FROM credentials.user_userpass_credentials
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
