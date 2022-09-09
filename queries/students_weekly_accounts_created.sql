WITH draft_projects as (
SELECT 
    owner_id,
    sps.name as name,
    sps.username as username,
    COUNT(DISTINCT pr.id) as number_of_draft_projects
from projects as pr
LEFT JOIN student_profiles as sps
ON pr.owner_id = sps.id  
WHERE pr.id NOT in (SELECT DISTINCT project_id from project_classroom_share)
AND owner_user_type = 'STUDENT'
GROUP BY owner_id, sps.name, sps.username
),
shared_projects as (
SELECT 
    owner_id,
    sps.name as name,
    sps.username as username,
    COUNT(DISTINCT pcs.project_id) as number_of_shared_projects
from projects as pr
LEFT JOIN project_classroom_share as pcs
ON pr.id = pcs.project_id  
LEFT JOIN student_profiles as sps
ON pr.owner_id = sps.id  
WHERE owner_user_type = 'STUDENT'
GROUP BY owner_id, sps.name, sps.username
),
Final_output as
(
SELECT 
COALESCE(draft_projects.owner_id, shared_projects.owner_id) as student_id,
COALESCE(draft_projects.name, shared_projects.name) as name,
COALESCE(draft_projects.username, shared_projects.username) as username,
number_of_draft_projects, 
number_of_shared_projects
from draft_projects FULL OUTER JOIN shared_projects ON draft_projects.owner_id = shared_projects.owner_id
)

SELECT 
student_profiles.id as student_id,
student_profiles.name as name,
student_profiles.username as username,
student_profiles.created_at as created_at,
cl.teacher_id as teacher_id,
COALESCE(Final_output.number_of_draft_projects, 0) as number_of_draft_projects,
COALESCE(Final_output.number_of_shared_projects, 0) as number_of_shared_projects
from student_profiles
LEFT JOIN classrooms as cl 
ON cl.id = student_profiles.classroom_id
LEFT JOIN Final_output
ON student_profiles.id = Final_output.student_id
WHERE TO_CHAR(TO_TIMESTAMP(student_profiles.created_at/1000), 'YYYY-MM-DD') between '2022-09-01' and '2022-09-09'

ORDER BY created_at asc