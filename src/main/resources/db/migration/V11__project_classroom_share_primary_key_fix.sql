ALTER TABLE project_classroom_share
    DROP CONSTRAINT pk_project_classroom_share;

ALTER TABLE project_classroom_share
    ADD CONSTRAINT pk_project_classroom_share PRIMARY KEY (project_id, classroom_id);
