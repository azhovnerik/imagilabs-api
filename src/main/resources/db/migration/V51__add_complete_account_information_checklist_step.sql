-- Add COMPLETE_YOUR_ACCOUNT_INFORMATION step to all existing teachers
INSERT INTO teacher_checklist_steps (id, created_at, last_modified_at, teacher_id, step, completed)
SELECT md5(random()::text || clock_timestamp()::text)::uuid,
       CAST(EXTRACT(EPOCH FROM now()) * 1000 AS BIGINT),
       CAST(EXTRACT(EPOCH FROM now()) * 1000 AS BIGINT),
       tp.id,
       'COMPLETE_YOUR_ACCOUNT_INFORMATION',
       false
FROM teacher_profiles tp
WHERE NOT EXISTS (
    SELECT 1
    FROM teacher_checklist_steps tcs
    WHERE tcs.teacher_id = tp.id
      AND tcs.step = 'COMPLETE_YOUR_ACCOUNT_INFORMATION'
);