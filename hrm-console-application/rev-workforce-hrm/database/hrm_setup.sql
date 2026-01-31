---------------------------------------------------------
-- HRM MAINTENANCE & YEAR SETUP
---------------------------------------------------------


USE hrm_console;

SET SQL_SAFE_UPDATES = 0;


--  Updates year for performance and goals to ensure 2026 is the primary target

SET @current_working_year = 2026;


--  Verifies and updates any existing 2025 data based on defined logic

UPDATE goals
SET goal_year = @current_working_year
WHERE goal_year < @current_working_year;

UPDATE performance_reviews
SET review_year = @current_working_year
WHERE review_year < @current_working_year;

SET SQL_SAFE_UPDATES = 1;


--  Confirmation select

SELECT 'System successfully set to Year 2026' AS Maintenance_Status;

COMMIT;


