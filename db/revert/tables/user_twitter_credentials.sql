-- Revert acctsoft:tables/user_twitter_credentials from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS credentials.user_twitter_credentials;

COMMIT;

-- vim: expandtab shiftwidth=2
