CREATE TABLE ed_link_oauth_state
(
    id               UUID PRIMARY KEY,
    created_at       BIGINT,
    last_modified_at BIGINT
);

ALTER TABLE student_profiles
    ADD COLUMN ed_link_integration_id UUID,
    ADD COLUMN ed_link_person_id      UUID;

ALTER TABLE teacher_profiles
    ADD COLUMN ed_link_integration_id UUID,
    ADD COLUMN ed_link_person_id      UUID;
