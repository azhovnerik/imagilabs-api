-- Add new fields to teacher_profiles table
ALTER TABLE teacher_profiles
    ADD COLUMN state VARCHAR(255),
    ADD COLUMN school_roles TEXT,
    ADD COLUMN grades TEXT,
    ADD COLUMN subjects TEXT,
    ADD COLUMN schools TEXT;