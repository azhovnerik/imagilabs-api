CREATE TABLE project_classroom_share (
    project_id   UUID NOT NULL,
    classroom_id UUID NOT NULL,
    CONSTRAINT pk_project_classroom_share PRIMARY KEY (project_id),
    CONSTRAINT fk_project_classroom_share_pr FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_project_classroom_share_cr FOREIGN KEY (classroom_id) REFERENCES classrooms(id)
);
