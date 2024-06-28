ALTER TABLE open_ai_assistance
    DROP CONSTRAINT open_ai_assistance_user_id_fkey,
    ADD COLUMN user_type TEXT;

UPDATE open_ai_assistance oai
SET user_type = 'STUDENT';

ALTER TABLE open_ai_assistance
    ALTER COLUMN user_type SET NOT NULL;
