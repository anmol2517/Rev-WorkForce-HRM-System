USE hrm_console;

UPDATE employees
SET password = SHA2('password123', 256)
WHERE employee_code IN ('EMP001', 'EMP002', 'EMP003', 'EMP004', 'EMP005');

INSERT IGNORE INTO employees (employee_code, first_name, last_name, email, password, phone, role, department_id, designation_id, joining_date, is_active) 
VALUES ('EMP006', 'Komal', 'Kumari', 'komal@revworkforce.com', SHA2('Password@123', 256), '9988776655', 'EMPLOYEE', 2, 4, '2024-05-01', true);

UPDATE employees SET role = 'ADMIN' WHERE employee_code = 'EMP001';
UPDATE employees SET role = 'MANAGER' WHERE employee_code = 'EMP002';

UPDATE employees
SET security_question = 'What is your favorite color?',
security_answer = 'Blue'
WHERE employee_code = 'EMP001';

COMMIT;