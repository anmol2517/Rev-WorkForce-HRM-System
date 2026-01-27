use hrm_console;
DESC leave_balances;

USE hrm_console;

ALTER TABLE leave_balances 
ADD COLUMN total_leaves INT DEFAULT 10 AFTER year,
ADD COLUMN remaining_leaves INT AS (total_leaves - used_leaves) STORED AFTER used_leaves;

SELECT * FROM leave_balances WHERE employee_id = 7 AND year = 2026;
INSERT IGNORE INTO leave_balances (employee_id, leave_type_id, year, total_leaves, used_leaves)
SELECT e.employee_id, lt.leave_type_id, 2026, lt.max_days_per_year, 0
FROM employees e CROSS JOIN leave_types lt WHERE e.is_active = TRUE;

COMMIT;

