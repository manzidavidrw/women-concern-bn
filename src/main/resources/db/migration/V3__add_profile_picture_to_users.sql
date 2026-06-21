ALTER TABLE users
    ADD COLUMN IF NOT EXISTS profile_picture_url  VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS profile_picture_id   VARCHAR(500);