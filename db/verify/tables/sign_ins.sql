-- Verify acctsoft:tables/sign_ins on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT sign_in_id, source_ip, user_agent, method, successful, created_at, updated_at
  FROM credentials.sign_ins
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
