CREATE TABLE teacher_checklist_steps
(
    id               UUID    NOT NULL,
    created_at       BIGINT,
    last_modified_at BIGINT,

    teacher_id       UUID    NOT NULL,
    step             TEXT    NOT NULL,
    completed        BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_teacher_checklist_steps PRIMARY KEY (id)
);

CREATE INDEX idx_teacher_id ON teacher_checklist_steps(teacher_id);
