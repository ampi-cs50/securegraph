-- Legacy created_at values have no recorded offset. Keep them intact.
ALTER TABLE proyecto ADD COLUMN created_at_utc TIMESTAMP WITH TIME ZONE;
