CREATE TABLE student_profiles (
    id               UUID NOT NULL,
    created_at       BIGINT,
    last_modified_at BIGINT,
    name             TEXT,
    username         TEXT,
    password         TEXT,
    classroom_id     UUID,
    CONSTRAINT pk_student_profiles PRIMARY KEY (id)
);
