WITH teacher_profile AS (
    SELECT tps.id AS teacher_id,
        tps.email AS email,
        tps.created_at AS creation_date,
        COUNT(DISTINCT cr.id) AS number_of_classes
    FROM teacher_profiles AS tps
        LEFT JOIN classrooms AS cr ON tps.id = cr.teacher_id
    GROUP BY tps.id,
        tps.email
)
SELECT tp.teacher_id,
    tp.email,
    tp.creation_date AS creation_date,
    number_of_classes
FROM teacher_profile AS tp
ORDER BY tp.creation_date ASC