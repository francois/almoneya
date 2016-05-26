-- Revert almoneya:tables/envelopes from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP TABLE public.envelopes;

COMMIT;

-- vim: expandtab shiftwidth=2
