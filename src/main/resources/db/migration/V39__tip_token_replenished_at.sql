ALTER TABLE teacher_profiles
    ADD COLUMN tip_tokens_replenished_at BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM now()) * 1000;

ALTER TABLE student_profiles
    ADD COLUMN tip_tokens_replenished_at BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM now()) * 1000;
