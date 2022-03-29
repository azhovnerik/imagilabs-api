CREATE TABLE lesson_bundles (
    id               UUID NOT NULL,
    created_at       BIGINT,
    last_modified_at BIGINT,
    name             TEXT,
    default_bundle   BOOLEAN,
    CONSTRAINT pk_lesson_bundles PRIMARY KEY (id)
);

CREATE TABLE bundle_lessons (
    id               UUID NOT NULL,
    name             TEXT,
    worksheet_uri    TEXT,
    slides_uri       TEXT,
    created_at       BIGINT,
    last_modified_at BIGINT,
    bundle_id        UUID,
    lesson_index     INTEGER,
    CONSTRAINT pk_bundle_lessons PRIMARY KEY (id),
    CONSTRAINT uc_bundle_lessons_bundle_id_lesson_index UNIQUE (bundle_id, lesson_index),
    CONSTRAINT fk_bundle_lessons_lesson_bundles FOREIGN KEY (bundle_id) REFERENCES lesson_bundles(id)
);

CREATE TABLE teacher_lessons (
    id               UUID NOT NULL,
    name             TEXT,
    worksheet_uri    TEXT,
    slides_uri       TEXT,
    created_at       BIGINT,
    last_modified_at BIGINT,
    teacher_id       UUID,
    lesson_index     INTEGER,
    CONSTRAINT pk_teacher_lessons PRIMARY KEY (id),
    CONSTRAINT uc_teacher_lessons_teacher_id_lesson_index UNIQUE (teacher_id, lesson_index),
    CONSTRAINT fk_teacher_lessons_teacher_profiles FOREIGN KEY (teacher_id) REFERENCES teacher_profiles(id)
);
