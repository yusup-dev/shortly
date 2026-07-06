-- Backfill plan_id for seeded users (was never set by earlier migrations)
UPDATE users
SET plan_id = (SELECT id FROM plans WHERE name = 'FREE')
WHERE email = 'free@gmail.com' AND plan_id IS NULL;

UPDATE users
SET plan_id = (SELECT id FROM plans WHERE name = 'PRO')
WHERE email = 'pro@gmail.com' AND plan_id IS NULL;

UPDATE users
SET plan_id = (SELECT id FROM plans WHERE name = 'PRO')
WHERE email = 'admin@gmail.com' AND plan_id IS NULL;

-- Fix audit log seed target_type to match the TargetType enum value
UPDATE audit_logs
SET target_type = 'SHORT_URL'
WHERE target_type = 'URL';
