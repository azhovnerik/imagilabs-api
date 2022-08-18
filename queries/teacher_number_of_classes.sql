WITH teacher_profile as 
(SELECT 
    tps.id as teacher_id,
    tps.email as email,
    tps.created_at as creation_date,
    COUNT(DISTINCT cr.id) as number_of_classes
from teacher_profiles as tps
LEFT JOIN classrooms as cr
ON tps.id = cr.teacher_id
GROUP BY tps.id, 
tps.email)
SELECT 
tp.teacher_id,
tp.email,
TO_CHAR(TO_TIMESTAMP(tp.creation_date/1000), 'yyyy/mm/dd') as creation_date,
number_of_classes
from teacher_profile as tp
ORDER BY tp.creation_date asc;