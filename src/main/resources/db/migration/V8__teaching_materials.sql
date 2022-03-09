CREATE TABLE teaching_materials (
    id               UUID NOT NULL,
    created_at       BIGINT,
    last_modified_at BIGINT,
    name             TEXT,
    index            INTEGER,
    path             TEXT,
    CONSTRAINT pk_teaching_materials PRIMARY KEY (id)
);

ALTER TABLE teaching_materials
    ADD CONSTRAINT uc_teaching_materials_index UNIQUE (index);
