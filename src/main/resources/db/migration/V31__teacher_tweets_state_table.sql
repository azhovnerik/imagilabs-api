CREATE TABLE teacher_tweets_state
(
    id                        UUID    NOT NULL,
    created_at                BIGINT,
    last_modified_at          BIGINT,

    teacher_id                UUID    NOT NULL,
    is_hidden                 BOOLEAN NOT NULL,
    is_showed_feedback_dialog BOOLEAN NOT NULL,

    CONSTRAINT pk_teacher_tweets_state PRIMARY KEY (id),
    CONSTRAINT fk_teacher_id FOREIGN KEY (teacher_id) REFERENCES teacher_profiles(id),
    CONSTRAINT uc_teacher_tweets_state_teacher_id UNIQUE (teacher_id)
)
