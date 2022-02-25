CREATE TABLE projects (
    id               UUID NOT NULL,
    created_at       BIGINT,
    last_modified_at BIGINT,
    name             TEXT NOT NULL,
    owner_id         UUID NOT NULL,
    owner_user_type  TEXT NOT NULL,
    source_code      TEXT NOT NULL,
    run_result       TEXT,
    CONSTRAINT pk_projects PRIMARY KEY (id)
);
