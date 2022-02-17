CREATE TABLE teacher_profiles (
    id UUID NOT NULL,
    created_at BIGINT,
    last_modified_at BIGINT,
    email TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    country TEXT NOT NULL,
    organization TEXT NOT NULL,
    how_did_you_hear_about_us TEXT NOT NULL,
    CONSTRAINT pk_teacher_profiles PRIMARY KEY (id)
);

ALTER TABLE teacher_profiles ADD CONSTRAINT uc_teacher_profiles_email UNIQUE (email);
