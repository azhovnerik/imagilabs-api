SELECT
student_profiles.id as student_id,
student_profiles.name as name,
student_profiles.username as username,
TO_CHAR(TO_TIMESTAMP(student_profiles.created_at/1000), 'YYYY-MM-DD') as created_at,
cl.teacher_id as teacher_id
from student_profiles
LEFT JOIN classrooms as cl
ON cl.id = student_profiles.classroom_id
-- change the dates below, note that 'between' is inclusive
WHERE TO_CHAR(TO_TIMESTAMP(student_profiles.created_at/1000), 'YYYY-MM-DD') between '2022-09-05' and '2022-09-11'
ORDER BY created_at asc