USE hrm_db; 
UPDATE employees SET password = 'Password@123' WHERE role != 'ADMIN';