WITH draft_projects as (
SELECT 
    owner_id,
    tps.email as email,
    tps.created_at as creation_date,
    COUNT(DISTINCT pr.id) as number_of_draft_projects
from projects as pr
LEFT JOIN teacher_profiles as tps
ON pr.owner_id = tps.id  
WHERE pr.id NOT in (SELECT DISTINCT project_id from project_classroom_share)
AND owner_user_type = 'TEACHER'
GROUP BY owner_id, tps.email, tps.created_at
),
shared_projects as (
SELECT 
    owner_id,
    tps.email as email,
    tps.created_at as creation_date,
    COUNT(DISTINCT pcs.project_id) as number_of_shared_projects
from projects as pr
LEFT JOIN project_classroom_share as pcs
ON pr.id = pcs.project_id  
LEFT JOIN teacher_profiles as tps
ON pr.owner_id = tps.id  
WHERE owner_user_type = 'TEACHER'
GROUP BY owner_id, tps.email, tps.created_at
),
Final_output as
(SELECT 
COALESCE(draft_projects.owner_id, shared_projects.owner_id) as teacher_id,
COALESCE(draft_projects.email, shared_projects.email) as email,
COALESCE(draft_projects.creation_date, shared_projects.creation_date) as creation_date,
number_of_draft_projects, 
number_of_shared_projects
from draft_projects FULL OUTER JOIN shared_projects ON draft_projects.owner_id = shared_projects.owner_id)

SELECT 
teacher_profiles.id as teacher_id,
teacher_profiles.email as email,
teacher_profiles.created_at as creation_date,
COALESCE(Final_output.number_of_draft_projects, 0) as number_of_draft_projects,
COALESCE(Final_output.number_of_shared_projects, 0) as number_of_shared_projects
from teacher_profiles
LEFT JOIN Final_output
ON teacher_profiles.id = Final_output.teacher_id

ORDER BY creation_date asc