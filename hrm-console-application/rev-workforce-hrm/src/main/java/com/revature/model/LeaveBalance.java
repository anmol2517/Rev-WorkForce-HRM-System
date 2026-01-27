package com.revature.model;

import java.time.LocalDateTime;

public class LeaveBalance {
    private int balanceId;
    private int employeeId;
    private int leaveTypeId;
    private int year;
    private int totalLeaves;
    private int usedLeaves;
    private int remainingLeaves;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String leaveTypeName;
    private String employeeName;

    public LeaveBalance() {
        this.year = java.time.Year.now().getValue();
        this.totalLeaves = 0;
        this.usedLeaves = 0;
    }

    public LeaveBalance(int employeeId, int leaveTypeId, int year, int totalLeaves) {
        this();
        this.employeeId = employeeId;
        this.leaveTypeId = leaveTypeId;
        this.year = year;
        this.totalLeaves = totalLeaves;
        this.usedLeaves = 0;
        this.remainingLeaves = totalLeaves;
    }

    public int getBalanceId() { return balanceId; }
    public void setBalanceId(int balanceId) { this.balanceId = balanceId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public int getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(int leaveTypeId) { this.leaveTypeId = leaveTypeId; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getTotalDays() { return totalLeaves; }
    public int getUsedDays() { return usedLeaves; }
    public int getRemainingDays() { return remainingLeaves; }

    public void setTotalDays(int days) {
        this.totalLeaves = days;
    }

    public void setUsedDays(int days) {
        this.usedLeaves = days;
    }

    public void setRemainingDays(int days) {
        this.remainingLeaves = days;
    }

    public int getTotalLeaves() { return totalLeaves; }
    public void setTotalLeaves(int totalLeaves) {
        this.totalLeaves = totalLeaves;
    }

    public int getUsedLeaves() { return usedLeaves; }
    public void setUsedLeaves(int usedLeaves) {
        this.usedLeaves = usedLeaves;
    }

    public int getRemainingLeaves() { return remainingLeaves; }
    public void setRemainingLeaves(int remainingLeaves) {
        this.remainingLeaves = remainingLeaves;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getLeaveTypeName() { return leaveTypeName; }
    public void setLeaveTypeName(String leaveTypeName) { this.leaveTypeName = leaveTypeName; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public void calculateRemaining() {
        this.remainingLeaves = this.totalLeaves - this.usedLeaves;
    }

    public boolean hasSufficientBalance(int daysRequested) {
        return (this.totalLeaves - this.usedLeaves) >= daysRequested;
    }

    public void deductLeaves(int days) {
        this.usedLeaves += days;
        calculateRemaining();
    }

    public void creditLeaves(int days) {
        this.usedLeaves = Math.max(0, this.usedLeaves - days);
        calculateRemaining();
    }

    @Override
    public String toString() {
        int currentRemaining = this.totalLeaves - this.usedLeaves;
        return String.format("%s : %d/%d (Used : %d)",
                leaveTypeName, currentRemaining, totalLeaves, usedLeaves);
    }
}

