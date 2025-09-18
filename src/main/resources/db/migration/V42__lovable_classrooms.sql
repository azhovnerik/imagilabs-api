CREATE TABLE lovable_classrooms
(
    id                          UUID PRIMARY KEY,
    created_at                  BIGINT,
    last_modified_at            BIGINT,
    classroom_id                UUID,
    lovable_integration_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    lovable_integration_paused  BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_lovable_classrooms_classrooms FOREIGN KEY (classroom_id) REFERENCES classrooms (id)
);
