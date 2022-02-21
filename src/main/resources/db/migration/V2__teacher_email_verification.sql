ALTER TABLE teacher_profiles
    ADD COLUMN email_verification_code TEXT;

ALTER TABLE teacher_profiles
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
