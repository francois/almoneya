-- Deploy almoneya:functions/maintain_timestamps to pg

SET client_min_messages TO 'warning';

BEGIN;

  CREATE OR REPLACE FUNCTION public.maintain_timestamps() RETURNS TRIGGER AS $$
  BEGIN
    IF TG_OP = 'INSERT' THEN
      NEW.created_at := current_timestamp;
    END IF;

    NEW.updated_at := current_timestamp;

    RETURN NEW;
  END
  $$ LANGUAGE plpgsql;

COMMIT;

-- vim: expandtab shiftwidth=2
