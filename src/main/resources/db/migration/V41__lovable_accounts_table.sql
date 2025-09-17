CREATE TABLE lovable_accounts
(
    id               UUID    NOT NULL,
    created_at       BIGINT,
    last_modified_at BIGINT,
    email            TEXT    NOT NULL,
    password         TEXT    NOT NULL,
    connected_user   UUID,
    connected_at     BIGINT,
    active           BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_lovable_accounts PRIMARY KEY (id)
);
