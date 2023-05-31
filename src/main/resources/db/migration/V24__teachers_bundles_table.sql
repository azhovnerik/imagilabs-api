CREATE TABLE teachers_bundles
(
    id               UUID NOT NULL,
    created_at       BIGINT,
    last_modified_at BIGINT,
    teacher_id       UUID NOT NULL,
    bundle_id        UUID NOT NULL,
    CONSTRAINT uc_teacher_id_bundle_id UNIQUE (teacher_id, bundle_id)
);
