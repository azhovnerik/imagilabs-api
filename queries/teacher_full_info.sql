WITH student_draft_projects AS (
    SELECT owner_id,
        sps.name AS name,
        sps.username AS username,
        sps.classroom_id AS classroom_id,
        COUNT(DISTINCT pr.id) AS number_of_draft_projects
    FROM projects AS pr
        LEFT JOIN student_profiles AS sps ON pr.owner_id = sps.id
    WHERE pr.id NOT IN (
            SELECT DISTINCT project_id
            FROM project_classroom_share
        )
        AND owner_user_type = 'STUDENT'
    GROUP BY owner_id,
        sps.name,
        sps.username,
        sps.classroom_id
),
student_shared_projects AS (
    SELECT owner_id,
        sps.name AS name,
        sps.username AS username,
        sps.classroom_id AS classroom_id,
        COUNT(DISTINCT pcs.project_id) AS number_of_shared_projects
    FROM projects AS pr
        LEFT JOIN project_classroom_share AS pcs ON pr.id = pcs.project_id
        LEFT JOIN student_profiles AS sps ON pr.owner_id = sps.id
    WHERE owner_user_type = 'STUDENT'
    GROUP BY owner_id,
        sps.name,
        sps.username,
        sps.classroom_id
),
student_projects AS (
    SELECT COALESCE(
            student_draft_projects.owner_id,
            student_shared_projects.owner_id
        ) AS student_id,
        COALESCE(
            student_draft_projects.name,
            student_shared_projects.name
        ) AS name,
        COALESCE(
            student_draft_projects.username,
            student_shared_projects.username
        ) AS username,
        COALESCE(
            student_draft_projects.classroom_id,
            student_shared_projects.classroom_id
        ) AS classroom_id,
        number_of_draft_projects,
        number_of_shared_projects
    FROM student_draft_projects
        FULL OUTER JOIN student_shared_projects ON student_draft_projects.owner_id = student_shared_projects.owner_id
),
classroom_info AS (
    SELECT cl.id AS classroom_id,
        cl.created_at AS creation_date,
        cl.teacher_id AS teacher_id,
        COUNT(DISTINCT student_profiles.id) AS number_of_students,
        COALESCE(
            SUM(student_projects.number_of_draft_projects),
            0
        ) AS number_of_draft_projects,
        COALESCE(
            SUM(student_projects.number_of_shared_projects),
            0
        ) AS number_of_shared_projects
    FROM student_profiles
        RIGHT JOIN classrooms AS cl ON student_profiles.classroom_id = cl.id
        LEFT JOIN student_projects ON student_profiles.id = student_projects.student_id
    GROUP BY cl.id
),
teacher_profile AS (
    SELECT tps.id AS teacher_id,
        tps.email AS email,
        tps.created_at AS creation_date,
        COUNT(DISTINCT cr.id) AS number_of_classes
    FROM teacher_profiles AS tps
        LEFT JOIN classrooms AS cr ON tps.id = cr.teacher_id
    GROUP BY tps.id,
        tps.email
),
draft_projects AS (
    SELECT owner_id,
        tps.email AS email,
        tps.created_at AS creation_date,
        COUNT(DISTINCT pr.id) AS number_of_draft_projects
    FROM projects AS pr
        LEFT JOIN teacher_profiles AS tps ON pr.owner_id = tps.id
    WHERE pr.id NOT IN (
            SELECT DISTINCT project_id
            FROM project_classroom_share
        )
        AND owner_user_type = 'TEACHER'
    GROUP BY owner_id,
        tps.email,
        tps.created_at
),
shared_projects AS (
    SELECT owner_id,
        tps.email AS email,
        tps.created_at AS creation_date,
        COUNT(DISTINCT pcs.project_id) AS number_of_shared_projects
    FROM projects AS pr
        LEFT JOIN project_classroom_share AS pcs ON pr.id = pcs.project_id
        LEFT JOIN teacher_profiles AS tps ON pr.owner_id = tps.id
    WHERE owner_user_type = 'TEACHER'
    GROUP BY owner_id,
        tps.email,
        tps.created_at
),
teacher_projects AS (
    SELECT COALESCE(
            draft_projects.owner_id,
            shared_projects.owner_id
        ) AS teacher_id,
        COALESCE(draft_projects.email, shared_projects.email) AS email,
        COALESCE(
            draft_projects.creation_date,
            shared_projects.creation_date
        ) AS creation_date,
        number_of_draft_projects,
        number_of_shared_projects
    FROM draft_projects
        FULL OUTER JOIN shared_projects ON draft_projects.owner_id = shared_projects.owner_id
)
SELECT teacher_profiles.id AS teacher_id,
    teacher_profiles.email AS email,
    teacher_profiles.created_at AS creation_date,
    teacher_profile.number_of_classes AS number_of_classes,
    COALESCE(teacher_projects.number_of_draft_projects, 0) AS number_of_draft_projects,
    COALESCE(teacher_projects.number_of_shared_projects, 0) AS number_of_shared_projects,
    COALESCE(SUM(classroom_info.number_of_students), 0) AS number_of_students,
    COALESCE(SUM(classroom_info.number_of_draft_projects), 0) AS number_of_student_draft_projects,
    COALESCE(SUM(classroom_info.number_of_shared_projects), 0) AS number_of_student_shared_projects
FROM teacher_profiles
    LEFT JOIN teacher_projects ON teacher_profiles.id = teacher_projects.teacher_id
    LEFT JOIN teacher_profile ON teacher_profiles.id = teacher_profile.teacher_id
    LEFT JOIN classroom_info ON teacher_profiles.id = classroom_info.teacher_id
GROUP BY teacher_profiles.id,
    teacher_profile.number_of_classes,
    teacher_projects.number_of_draft_projects,
    teacher_projects.number_of_shared_projects
ORDER BY creation_date ASC