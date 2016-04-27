-- Revert acctsoft:tables/sign_ins from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS credentials.sign_ins;

COMMIT;

-- vim: expandtab shiftwidth=2
