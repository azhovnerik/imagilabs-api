CREATE TABLE classrooms (
    id UUID NOT NULL,
    created_at BIGINT,
    last_modified_at BIGINT,
    name TEXT,
    access_code TEXT,
    CONSTRAINT uc_access_code UNIQUE (access_code),
    CONSTRAINT pk_classrooms PRIMARY KEY (id)
);

CREATE TABLE classroom_students(
    student_id UUID NOT NULL,
    classroom_id UUID NOT NULL,
    CONSTRAINT fk_classroom_students_sp FOREIGN KEY (student_id) REFERENCES student_profiles(id),
    CONSTRAINT fk_classroom_students_c FOREIGN KEY (classroom_id) REFERENCES classrooms(id),
    CONSTRAINT pk_classroom_students PRIMARY KEY (student_id, classroom_id)
);

ALTER TABLE student_profiles
    DROP COLUMN classroom_id;
