---------------------------------------------------------
--  HRM PATCH ANNOUNCEMENTS
---------------------------------------------------------

USE hrm_console;

DROP TABLE IF EXISTS announcements;

CREATE TABLE announcements (
    announcement_id INT AUTO_INCREMENT PRIMARY KEY, 
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL, 
    employee_id INT, 
    is_active BOOLEAN DEFAULT TRUE,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT', 'NORMAL', 'CRITICAL') DEFAULT 'MEDIUM', 
    valid_from DATE DEFAULT (CURRENT_DATE), 
    valid_until DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);

SET SQL_SAFE_UPDATES = 0;

DELETE FROM performance_reviews WHERE review_year = 2026;

UPDATE employees SET email = 'mahi@revworkforce.com' WHERE employee_id = 2;
UPDATE employees SET email = 'alice@revworkforce.com' WHERE employee_id = 3;
UPDATE employees SET email = 'bob@revworkforce.com' WHERE employee_id = 4;
UPDATE employees SET email = 'carol@revworkforce.com' WHERE employee_id = 5;

COMMIT;

SET SQL_SAFE_UPDATES = 1;

