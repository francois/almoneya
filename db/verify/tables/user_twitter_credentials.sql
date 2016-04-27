-- Verify acctsoft:tables/user_twitter_credentials on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT user_id, consumer_key, consumer_secret, access_token, access_token_secret, created_at, updated_at
  FROM credentials.user_twitter_credentials
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
