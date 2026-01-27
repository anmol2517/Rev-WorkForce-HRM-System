USE hrm_console;
UPDATE employees SET password = 'password123' WHERE email = 'admin@revworkforce.com';
COMMIT;
UPDATE employees SET password = 'password123' WHERE email = 'admin@revworkforce.com';
COMMIT;

UPDATE employees 
SET first_name = 'Mahi', 
    last_name = 'Singh', 
    email = 'mahi@revworkforce.com'
WHERE employee_id = 2;

UPDATE employees 
SET first_name = 'Mahi', 
    last_name = 'Singh', 
    email = 'mahi@revworkforce.com' 
WHERE email = 'john@revworkforce.com';
UPDATE employees SET role = 'ADMIN' WHERE employee_id = 1;
UPDATE employees SET role = 'MANAGER' WHERE employee_id = 2;
DESC employees;
ALTER TABLE notifications ADD COLUMN title VARCHAR(255) AFTER employee_id;
UPDATE employees SET password = 'Vikas@123' WHERE email = 'vikas@revworkforce.com';
UPDATE employees SET password = 'Myra@123' WHERE email = 'myra@revworkforce.com';