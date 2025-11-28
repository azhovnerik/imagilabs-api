ALTER TABLE classrooms
    RENAME COLUMN ed_link_id TO ed_link_class_id;
ALTER TABLE classrooms
    ADD COLUMN ed_link_integration_id UUID;
