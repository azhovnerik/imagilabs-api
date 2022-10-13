SELECT cr.id AS class_id,
    cr.created_at AS creation_date,
    cr.name AS class_name,
    cr.teacher_id AS teacher_id,
    COUNT(DISTINCT sp.id) AS number_of_students,
    COUNT(DISTINCT pcs.project_id) AS number_of_shared_projects
FROM classrooms AS cr
    LEFT JOIN student_profiles AS sp ON sp.classroom_id = cr.id
    LEFT JOIN project_classroom_share AS pcs ON pcs.classroom_id = cr.id
GROUP BY cr.id,
    cr.name,
    cr.teacher_id,
    cr.created_at
ORDER BY creation_date ASC