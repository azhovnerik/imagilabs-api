WITH draft_projects AS (
    SELECT owner_id,
        sps.name AS name,
        sps.username AS username,
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
        sps.username
),
shared_projects AS (
    SELECT owner_id,
        sps.name AS name,
        sps.username AS username,
        COUNT(DISTINCT pcs.project_id) AS number_of_shared_projects
    FROM projects AS pr
        LEFT JOIN project_classroom_share AS pcs ON pr.id = pcs.project_id
        LEFT JOIN student_profiles AS sps ON pr.owner_id = sps.id
    WHERE owner_user_type = 'STUDENT'
    GROUP BY owner_id,
        sps.name,
        sps.username
),
final_output AS (
    SELECT COALESCE(
            draft_projects.owner_id,
            shared_projects.owner_id
        ) AS student_id,
        COALESCE(draft_projects.name, shared_projects.name) AS name,
        COALESCE(
            draft_projects.username,
            shared_projects.username
        ) AS username,
        number_of_draft_projects,
        number_of_shared_projects
    FROM draft_projects
        FULL OUTER JOIN shared_projects ON draft_projects.owner_id = shared_projects.owner_id
)
SELECT student_profiles.id AS student_id,
    student_profiles.name AS name,
    student_profiles.username AS username,
    student_profiles.created_at AS creation_date,
    cl.teacher_id AS teacher_id,
    COALESCE(final_output.number_of_draft_projects, 0) AS number_of_draft_projects,
    COALESCE(final_output.number_of_shared_projects, 0) AS number_of_shared_projects
FROM student_profiles
    LEFT JOIN classrooms AS cl ON cl.id = student_profiles.classroom_id
    LEFT JOIN final_output ON student_profiles.id = final_output.student_id
ORDER BY creation_date ASC