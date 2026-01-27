package com.revature.model;

import java.time.LocalDateTime;


//  Department Entity - Company departments

public class Department {
    private int departmentId;
    private String departmentName;
    private String description;
    private boolean isActive;
    private LocalDateTime createdAt;

    public Department() {
        this.isActive = true;
    }

    public Department(String departmentName, String description) {
        this();
        this.departmentName = departmentName;
        this.description = description;
    }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("%s - %s", departmentName, description);
    }
}



