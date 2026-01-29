USE hrm_console;
DROP TABLE performance_reviews;

CREATE TABLE performance_reviews (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_id INT NOT NULL,
    manager_id INT,
    review_year INT NOT NULL,
    review_period VARCHAR(10) NOT NULL,
    key_deliverables TEXT,
    major_accomplishments TEXT,
    areas_of_improvement TEXT,
    self_rating INT,
    manager_rating INT,
    manager_feedback TEXT,
    status ENUM('PENDING', 'SUBMITTED', 'REVIEWED', 'COMPLETED') DEFAULT 'PENDING',
    submitted_at TIMESTAMP NULL,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    FOREIGN KEY (manager_id) REFERENCES employees(employee_id)
);

