CREATE TABLE co_teachers
(
    id               UUID   NOT NULL,
    created_at       BIGINT,
    last_modified_at BIGINT,
    classroom_id     UUID NOT NULL,
    teacher_email    TEXT   NOT NULL,
    teacher_id       UUID,
    CONSTRAINT pk_co_teachers PRIMARY KEY (id)
);
