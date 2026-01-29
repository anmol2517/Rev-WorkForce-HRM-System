USE hrm_console;

UPDATE employees SET first_name = 'Mahi', last_name = 'Singh', email = 'mahi@revworkforce.com', role = 'MANAGER' WHERE employee_id = 2;
UPDATE employees SET role = 'ADMIN' WHERE employee_id = 1;

-- ALTER TABLE notifications ADD COLUMN title VARCHAR(255) AFTER employee_id;

UPDATE employees SET password = SHA2('password123', 256) WHERE email = 'admin@revworkforce.com';
UPDATE employees SET password = SHA2('Password@123', 256) WHERE email = 'vikas@revworkforce.com';
UPDATE employees SET password = SHA2('Password@123', 256) WHERE email = 'myra@revworkforce.com';

COMMIT;