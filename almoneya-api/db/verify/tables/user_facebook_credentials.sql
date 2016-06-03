-- Verify almoneya:tables/user_facebook_credentials on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT user_id, service_id, name, access_token, expires_at, created_at, updated_at
  FROM credentials.user_facebook_credentials
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
