-- Revert almoneya:tables/userpass_sign_ins from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS credentials.userpass_sign_ins;

COMMIT;

-- vim: expandtab shiftwidth=2
