CREATE TABLE teacher_tweets_state
(
    teacher_id                UUID    NOT NULL,
    is_hidden                 BOOLEAN NOT NULL,
    is_showed_feedback_dialog BOOLEAN NOT NULL,

    CONSTRAINT pk_teacher_tweets_state PRIMARY KEY (teacher_id),
    CONSTRAINT fk_teacher_id FOREIGN KEY (teacher_id) REFERENCES teacher_profiles(id)
)
