---------------------------------------------------------
--  HRM MAIN STRUCTURE
---------------------------------------------------------


CREATE DATABASE IF NOT EXISTS hrm_console;
USE hrm_console;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS notifications;

DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS holidays;
DROP TABLE IF EXISTS goal_tracking;

DROP TABLE IF EXISTS goals;
DROP TABLE IF EXISTS performance_reviews;
DROP TABLE IF EXISTS leave_requests;

DROP TABLE IF EXISTS leave_balances;
DROP TABLE IF EXISTS leave_types;
DROP TABLE IF EXISTS employees;

DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS designations;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE departments (
department_id INT AUTO_INCREMENT PRIMARY KEY,
department_name VARCHAR(100) NOT NULL UNIQUE,
description TEXT,
is_active BOOLEAN DEFAULT TRUE,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE designations (
designation_id INT AUTO_INCREMENT PRIMARY KEY, designation_name VARCHAR(100) NOT NULL UNIQUE,
level INT DEFAULT 1, is_active BOOLEAN DEFAULT TRUE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employees (
employee_id INT AUTO_INCREMENT PRIMARY KEY,
employee_code VARCHAR(20) UNIQUE NOT NULL, first_name VARCHAR(50) NOT NULL, last_name VARCHAR(50) NOT NULL, email VARCHAR(100) UNIQUE NOT NULL, password VARCHAR(555) NOT NULL, 

phone VARCHAR(15), address TEXT, date_of_birth DATE, emergency_contact VARCHAR(15), department_id INT, designation_id INT, manager_id INT,

role ENUM('ADMIN', 'MANAGER', 'EMPLOYEE') DEFAULT 'EMPLOYEE', salary DECIMAL(12,2), joining_date DATE DEFAULT (CURRENT_DATE),

is_active BOOLEAN DEFAULT TRUE, is_deleted BOOLEAN DEFAULT FALSE, security_question VARCHAR(255), security_answer VARCHAR(255),

created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

FOREIGN KEY (department_id) REFERENCES departments(department_id), 
FOREIGN KEY (designation_id) REFERENCES designations(designation_id), FOREIGN KEY (manager_id) REFERENCES employees(employee_id)
);

CREATE TABLE leave_types (
leave_type_id INT AUTO_INCREMENT PRIMARY KEY, type_name VARCHAR(50) NOT NULL UNIQUE, description VARCHAR(200), max_days_per_year INT DEFAULT 12,
is_carry_forward BOOLEAN DEFAULT FALSE, is_active BOOLEAN DEFAULT TRUE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE leave_balances (
balance_id INT AUTO_INCREMENT PRIMARY KEY, employee_id INT NOT NULL, leave_type_id INT NOT NULL,
year INT NOT NULL, total_leaves INT DEFAULT 0, used_leaves INT DEFAULT 0,
remaining_leaves INT AS (total_leaves - used_leaves) STORED, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, UNIQUE(employee_id, leave_type_id, year),
FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE, FOREIGN KEY (leave_type_id) REFERENCES leave_types(leave_type_id)
);

CREATE TABLE leave_requests (
request_id INT AUTO_INCREMENT PRIMARY KEY, employee_id INT NOT NULL, leave_type_id INT NOT NULL, start_date DATE NOT NULL, end_date DATE NOT NULL,
total_days INT NOT NULL,
reason TEXT,
status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') DEFAULT 'PENDING',
approver_id INT, approver_comments TEXT, applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, actioned_at TIMESTAMP NULL,
FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
FOREIGN KEY (leave_type_id) REFERENCES leave_types(leave_type_id),
FOREIGN KEY (approver_id) REFERENCES employees(employee_id),
CONSTRAINT valid_dates CHECK (end_date >= start_date)
);

CREATE TABLE holidays (
holiday_id INT AUTO_INCREMENT PRIMARY KEY, holiday_name VARCHAR(100) NOT NULL, holiday_date DATE NOT NULL UNIQUE,
description TEXT, year INT NOT NULL,
is_optional BOOLEAN DEFAULT FALSE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS performance_reviews;

CREATE TABLE performance_reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    review_year INT NOT NULL,
    review_period ENUM('H1', 'H2') NOT NULL,
    key_deliverables TEXT,
    achievements TEXT,
    manager_comments TEXT,
    self_rating DECIMAL(3,2),
    manager_rating DECIMAL(3,2),
    status ENUM('PENDING', 'SUBMITTED', 'REVIEWED', 'COMPLETED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    UNIQUE INDEX review_cycle_idx (employee_id, review_year, review_period)
);
CREATE TABLE goals (
goal_id INT AUTO_INCREMENT PRIMARY KEY, employee_id INT NOT NULL,
goal_year INT NOT NULL, goal_description TEXT NOT NULL,
success_metrics TEXT, priority ENUM('HIGH', 'MEDIUM', 'LOW') DEFAULT 'MEDIUM',
deadline DATE, progress_percentage INT DEFAULT 0 CHECK (progress_percentage BETWEEN 0 AND 100),
status ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'ON_HOLD') DEFAULT 'NOT_STARTED', manager_guidance TEXT,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

CREATE TABLE goal_tracking (
tracking_id INT AUTO_INCREMENT PRIMARY KEY,
goal_id INT NOT NULL, update_date DATE DEFAULT (CURRENT_DATE),
progress_update TEXT, percentage_completed INT CHECK (percentage_completed BETWEEN 0 AND 100),
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (goal_id) REFERENCES goals(goal_id) ON DELETE CASCADE
);


--  Notifications Table 

DROP TABLE IF EXISTS notifications;

CREATE TABLE notifications (
notification_id INT PRIMARY KEY AUTO_INCREMENT,
employee_id INT, message VARCHAR(255) NOT NULL,
is_read BOOLEAN DEFAULT FALSE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);

DROP TABLE IF EXISTS announcements;

CREATE TABLE announcements (
announcement_id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(200) NOT NULL,
content TEXT NOT NULL, employee_id INT, is_active BOOLEAN DEFAULT TRUE,
priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM', 
valid_from DATE DEFAULT (CURRENT_DATE), valid_until DATE,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);

CREATE TABLE audit_logs (
log_id INT AUTO_INCREMENT PRIMARY KEY, employee_id INT, action_type VARCHAR(50) NOT NULL,
entity_type VARCHAR(50) NOT NULL, entity_id INT, old_value TEXT, new_value TEXT,
ip_address VARCHAR(50), action_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);


