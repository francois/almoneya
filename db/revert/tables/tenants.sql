-- Revert almoneya:tables/tenants from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE IF EXISTS credentials.tenants;

COMMIT;

-- vim: expandtab shiftwidth=2
