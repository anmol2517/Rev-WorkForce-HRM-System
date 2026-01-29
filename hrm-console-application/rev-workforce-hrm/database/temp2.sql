use hrm_console;

INSERT IGNORE INTO leave_balances (employee_id, leave_type_id, year, total_leaves, used_leaves)
SELECT employee_id, leave_type_id, 2026, max_days_per_year, 0
FROM employees CROSS JOIN leave_types 
WHERE email = 'satyam@revworkforce.com';

SELECT * FROM leave_balances 
WHERE employee_id = (SELECT employee_id FROM employees WHERE email = 'satyam@revworkforce.com')
AND year = 2026 AND leave_type_id = 2;

UPDATE leave_types SET max_days_per_year = 20 WHERE type_name = 'Casual Leave';
