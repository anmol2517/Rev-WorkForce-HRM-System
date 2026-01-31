package com.revature.model;

import java.time.LocalDateTime;


//  LeaveType Entity - Defines types of leaves available

public class LeaveType {
    private int leaveTypeId;
    private String typeName;
    private String description;
    private int maxDaysPerYear;
    private boolean isCarryForward;
    private boolean isActive;
    private LocalDateTime createdAt;

    public LeaveType() {
        this.maxDaysPerYear = 12;
        this.isCarryForward = false;
        this.isActive = true;
    }

    public LeaveType(String typeName, String description, int maxDaysPerYear) {
        this();
        this.typeName = typeName;
        this.description = description;
        this.maxDaysPerYear = maxDaysPerYear;
    }

    public int getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(int leaveTypeId) { this.leaveTypeId = leaveTypeId; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getMaxDaysPerYear() { return maxDaysPerYear; }
    public void setMaxDaysPerYear(int maxDaysPerYear) { this.maxDaysPerYear = maxDaysPerYear; }

    public boolean isCarryForward() { return isCarryForward; }
    public void setCarryForward(boolean carryForward) { isCarryForward = carryForward; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getDefaultDays() {
        return 10;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Max : %d days/year",
            typeName, description, maxDaysPerYear);
    }
}


