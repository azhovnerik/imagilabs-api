-- Reset CONGRATULATION_DIALOG_SHOWN to uncompleted for teachers who haven't completed COMPLETE_YOUR_ACCOUNT_INFORMATION
-- This ensures they see the congratulation popup again after completing their profile

UPDATE teacher_checklist_steps
SET completed = false,
    last_modified_at = EXTRACT(EPOCH FROM NOW()) * 1000
WHERE step = 'CONGRATULATION_DIALOG_SHOWN'
  AND completed = true
  AND teacher_id IN (
    SELECT teacher_id
    FROM teacher_checklist_steps
    WHERE step = 'COMPLETE_YOUR_ACCOUNT_INFORMATION'
      AND completed = false
);