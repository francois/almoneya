-- Verify almoneya:tables/goals on pg

SET client_min_messages TO 'warning';

BEGIN;

  SELECT tenant_id, envelope_name, description, amount, due_on, goal_id, created_at, updated_at
  FROM public.goals
  WHERE false;

ROLLBACK;

-- vim: expandtab shiftwidth=2
