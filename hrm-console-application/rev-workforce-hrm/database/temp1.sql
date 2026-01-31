USE hrm_console;

INSERT IGNORE INTO leave_balances (employee_id, leave_type_id, year, total_leaves, used_leaves)

SELECT e.employee_id, lt.leave_type_id, 2026, lt.max_days_per_year, 0
FROM employees e CROSS JOIN leave_types lt WHERE e.is_active = TRUE;

SELECT * FROM leave_balances WHERE year = 2026;

COMMIT;