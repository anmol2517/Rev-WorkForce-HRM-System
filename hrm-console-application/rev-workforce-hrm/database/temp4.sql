USE hrm_console;
UPDATE employees SET password = SHA2('password123', 256) WHERE email = 'admin@revworkforce.com';
SELECT email, password FROM employees WHERE email = 'myra@revworkforce.com';
COMMIT;
ALTER TABLE notifications ADD COLUMN notification_type VARCHAR(50) AFTER title;
ALTER TABLE notifications ADD COLUMN related_id INT;
