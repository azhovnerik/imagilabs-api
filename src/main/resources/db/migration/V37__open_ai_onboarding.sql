ALTER TABLE teacher_profiles
    ADD COLUMN ai_chat_onboarding_completed BOOLEAN DEFAULT FALSE;

ALTER TABLE student_profiles
    ADD COLUMN ai_chat_onboarding_completed BOOLEAN DEFAULT FALSE;
