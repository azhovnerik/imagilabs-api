ALTER TABLE classrooms
    ALTER COLUMN name SET NOT NULL;

ALTER TABLE classrooms
    ALTER COLUMN teacher_id SET NOT NULL;

ALTER TABLE student_profiles
    ALTER COLUMN name SET NOT NULL;

ALTER TABLE student_profiles
    ALTER COLUMN username SET NOT NULL;

ALTER TABLE student_profiles
    ALTER COLUMN password SET NOT NULL;
