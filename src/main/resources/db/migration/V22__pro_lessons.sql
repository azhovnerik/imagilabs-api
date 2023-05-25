ALTER TABLE bundle_lessons
    RENAME COLUMN locked TO pro_lesson;

ALTER TABLE teacher_lessons
    RENAME COLUMN locked to pro_lesson;
