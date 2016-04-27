-- Revert acctsoft:tables/user_userpass_credentials from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS credentials.user_userpass_credentials;

COMMIT;

-- vim: expandtab shiftwidth=2
