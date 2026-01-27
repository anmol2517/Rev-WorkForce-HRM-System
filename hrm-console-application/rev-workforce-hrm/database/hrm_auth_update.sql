---------------------------------------------------------
--  HRM AUTH & PASSWORDS
---------------------------------------------------------


USE hrm_console;


--  Updates all employee password hashes using SHA-256 with "password123" as the default base

UPDATE employees
SET password = SHA2('password123', 256)
WHERE employee_code IN ('EMP001', 'EMP002', 'EMP003', 'EMP004', 'EMP005');

UPDATE employees SET role = 'ADMIN' WHERE employee_code = 'EMP001';
UPDATE employees SET role = 'MANAGER' WHERE employee_code = 'EMP002';
SELECT employee_id, first_name, role FROM employees WHERE role = 'MANAGER';

--  Specific Security Question Updates

UPDATE employees
SET security_question = 'What is your favorite color?',
security_answer = 'Blue'
WHERE employee_code = 'EMP001';


--  Password Verification Check 

SELECT employee_code, first_name, email, role,
LEFT(password, 20) as password_hash_preview
FROM employees;

COMMIT;


