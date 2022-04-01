ALTER TABLE student_profiles
    ADD COLUMN classroom_id UUID;

UPDATE student_profiles sp
SET classroom_id = (
    SELECT cs.classroom_id FROM classrooms_students cs WHERE cs.student_id = sp.id
);

ALTER TABLE student_profiles
    ALTER COLUMN classroom_id SET NOT NULL;

ALTER TABLE student_profiles
    ADD CONSTRAINT fk_student_profiles_classrooms FOREIGN KEY (classroom_id) REFERENCES classrooms(id);

DROP TABLE classrooms_students;
