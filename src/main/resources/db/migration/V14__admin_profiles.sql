CREATE TABLE admin_profiles (
    id               UUID NOT NULL,
    created_at       BIGINT,
    last_modified_at BIGINT,
    email            TEXT NOT NULL,
    password_hash    TEXT NOT NULL,
    name             TEXT NOT NULL,
    CONSTRAINT pk_admin_profiles PRIMARY KEY (id),
    CONSTRAINT uc_admin_profiles_email UNIQUE (email)
);
