-- Revert almoneya:functions/maintain_timestamps from pg

SET client_min_messages TO 'warning';

BEGIN;

  DROP FUNCTION public.maintain_timestamps();

COMMIT;

-- vim: expandtab shiftwidth=2
