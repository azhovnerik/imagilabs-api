SELECT student_profiles.id AS student_id,
  student_profiles.name AS name,
  student_profiles.username AS username,
  TO_CHAR(
    TO_TIMESTAMP(student_profiles.created_at / 1000),
    'YYYY-MM-DD'
  ) AS created_at,
  cl.teacher_id AS teacher_id
FROM student_profiles
  LEFT JOIN classrooms AS cl ON cl.id = student_profiles.classroom_id -- change the dates below, note that 'BETWEEN' is inclusive
WHERE TO_CHAR(
    TO_TIMESTAMP(student_profiles.created_at / 1000),
    'YYYY-MM-DD'
  ) BETWEEN '2022-09-05' AND '2022-09-11'
ORDER BY created_at ASC