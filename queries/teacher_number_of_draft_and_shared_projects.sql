WITH draft_projects AS (
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
final_output AS (
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
    COALESCE(final_output.number_of_draft_projects, 0) AS number_of_draft_projects,
    COALESCE(final_output.number_of_shared_projects, 0) AS number_of_shared_projects
FROM teacher_profiles
    LEFT JOIN final_output ON teacher_profiles.id = final_output.teacher_id
ORDER BY creation_date ASC