-- ------------------------------------------------
-- Version: V1.11
-- Description: Update t_project table to add description column
-- ------------------------------------------------
ALTER TABLE t_Project
ADD COLUMN description VARCHAR(1024)