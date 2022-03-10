ALTER TABLE teaching_materials
    ADD COLUMN category TEXT NOT NULL DEFAULT 'TEACHING_SLIDES';

ALTER TABLE teaching_materials
    DROP CONSTRAINT uc_teaching_materials_index;

ALTER TABLE teaching_materials
    ADD CONSTRAINT uc_teaching_materials_cat_idx UNIQUE (category, index);
