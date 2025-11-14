CREATE TABLE student_classrooms
(
    student_id   UUID NOT NULL REFERENCES student_profiles,
    classroom_id UUID NOT NULL REFERENCES classrooms,
    PRIMARY KEY (student_id, classroom_id)
);

INSERT INTO student_classrooms (student_id, classroom_id)
SELECT id, classroom_id
FROM student_profiles;
