---------------------------------------------------------
--  HRM SEED DATA
---------------------------------------------------------

USE hrm_console;

INSERT IGNORE INTO departments (department_name, description) VALUES
('Human Resources', 'HR Department - Manages employee relations and policies'), 
('Engineering', 'Software Development and Engineering'),
('Finance', 'Financial planning and accounting'), 
('Marketing', 'Marketing and brand management'), 
('Operations', 'Business operations and logistics');

INSERT IGNORE INTO designations (designation_name, level) VALUES
('HR Manager', 5), ('HR Executive', 2), ('Senior Software Engineer', 3), 
('Software Engineer', 2), ('Junior Software Engineer', 1),
('Finance Manager', 5), ('Accountant', 2), ('Marketing Manager', 5), 
('Marketing Executive', 2), ('Operations Manager', 5);

INSERT IGNORE INTO leave_types (type_name, description, max_days_per_year, is_carry_forward) VALUES
('Annual Leave', 'Yearly vacation leave', 20, true), 
('Sick Leave', 'Medical/Health related leave', 10, false), 
('Casual Leave', 'Personal/Emergency leave', 20, false), 
('Maternity Leave', 'Maternity/Pregnancy leave', 90, false), 
('Paternity Leave', 'Paternity leave for new fathers', 15, false), 
('Unpaid Leave', 'Leave without pay', 365, false);

INSERT IGNORE INTO employees (employee_code, first_name, last_name, email, password, phone, role, department_id, designation_id, joining_date, is_active) 
VALUES ('EMP001', 'System', 'Administrator', 'admin@revworkforce.com', '240be518fabd2724ddb6f04eeb9d5b4f8d23cd6b0c0b3c4da9f5f4d7a9b8a3c7', '1234567890', 'ADMIN', 1, 1, '2024-01-01', true);

INSERT IGNORE INTO employees (employee_code, first_name, last_name, email, password, phone, role, department_id, designation_id, manager_id, joining_date, is_active) 
VALUES ('EMP002', 'Mahi', 'Singh', 'mahi@revworkforce.com', '5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5', '9876543210', 'MANAGER', 2, 3, 1, '2024-01-15', true);

INSERT IGNORE INTO employees (employee_code, first_name, last_name, email, password, phone, role, department_id, designation_id, manager_id, joining_date, is_active) 
VALUES
('EMP003', 'Alice', 'Johnson', 'alice@revworkforce.com', '5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5', '5551234567', 'EMPLOYEE', 2, 4, 2, '2024-02-01', true),
('EMP004', 'Bob', 'Williams', 'bob@revworkforce.com', '5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5', '5559876543', 'EMPLOYEE', 2, 5, 2, '2024-02-15', true),
('EMP005', 'Carol', 'Davis', 'carol@revworkforce.com', '5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5', '5554567890', 'EMPLOYEE', 3, 7, 1, '2024-03-01', true);

INSERT IGNORE INTO goals (employee_id, goal_year, goal_description, success_metrics, priority, deadline, status)
VALUES (3, 2026, 'Complete Java Backend Module', 'All unit tests passed', 'HIGH', '2026-03-31', 'IN_PROGRESS'),
(4, 2026, 'Fix Frontend UI bugs', '0 critical bugs remaining', 'MEDIUM', '2026-02-15', 'NOT_STARTED');

INSERT IGNORE INTO performance_reviews (employee_id, review_year, review_period, key_deliverables, status)
VALUES (3, 2026, 'H1', 'Q1 Backend Performance', 'PENDING');

INSERT IGNORE INTO holidays (holiday_name, holiday_date, year, description) VALUES
('New Year Day', '2026-01-01', 2026, 'New Year Celebration'),
('Republic Day', '2026-01-26', 2026, 'Republic Day of India'),
('Independence Day', '2026-08-15', 2026, 'Independence Day of India');

INSERT IGNORE INTO announcements (title, content, employee_id, priority, valid_from, valid_until) 
VALUES ('Welcome to Rev Workforce HRM', 'Welcome to the new system.', 1, 'HIGH', '2026-01-01', '2026-12-31');

INSERT IGNORE INTO leave_balances (employee_id, leave_type_id, year, total_leaves, used_leaves)
SELECT e.employee_id, lt.leave_type_id, 2026, lt.max_days_per_year, 0
FROM employees e CROSS JOIN leave_types lt WHERE e.is_active = TRUE;

COMMIT;
