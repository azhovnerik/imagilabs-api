SELECT 
    cr.id as class_id,
    cr.created_at as creation_date,
    cr.name as class_name, 
    cr.teacher_id as teacher_id,
    COUNT(DISTINCT sp.id) as number_of_students,
    COUNT(DISTINCT pcs.project_id) as number_of_shared_projects
from classrooms as cr
LEFT JOIN student_profiles as sp
ON sp.classroom_id = cr.id
LEFT JOIN project_classroom_share as pcs
ON pcs.classroom_id = cr.id
GROUP BY cr.id, cr.name, cr.teacher_id, cr.created_at
ORDER BY creation_date asc;