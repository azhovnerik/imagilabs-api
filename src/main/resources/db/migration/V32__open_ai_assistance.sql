CREATE TABLE open_ai_assistance
(
    id               UUID    NOT NULL,
    created_at       BIGINT,
    last_modified_at BIGINT,
    session_id       UUID    NOT NULL,
    user_id          UUID REFERENCES student_profiles (id),
    project_id       UUID REFERENCES projects (id),
    user_question    TEXT    NOT NULL DEFAULT '',
    ai_response      TEXT    NOT NULL DEFAULT '',
    is_helpful       BOOLEAN NOT NULL DEFAULT FALSE
);
