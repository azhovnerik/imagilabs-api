-- Add new fields to teacher_profiles table
ALTER TABLE teacher_profiles
    ADD COLUMN state VARCHAR(255),
    ADD COLUMN school_roles TEXT,
    ADD COLUMN grades TEXT,
    ADD COLUMN subjects TEXT;

-- Create schools table
CREATE TABLE schools
(
    id               UUID PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    created_at       BIGINT,
    last_modified_at BIGINT
);

-- Create join table for teacher-school many-to-many relationship
CREATE TABLE teacher_schools
(
    teacher_id UUID NOT NULL REFERENCES teacher_profiles (id) ON DELETE CASCADE,
    school_id  UUID NOT NULL REFERENCES schools (id) ON DELETE CASCADE,
    PRIMARY KEY (teacher_id, school_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_teacher_schools_teacher_id ON teacher_schools (teacher_id);
CREATE INDEX idx_teacher_schools_school_id ON teacher_schools (school_id);