-- Verify acctsoft:tables/userpass_sign_ins on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT sign_in_id, username, created_at
  FROM credentials.userpass_sign_ins
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
