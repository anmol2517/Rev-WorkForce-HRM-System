package com.revature.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveRequest {
    private int requestId;
    private int employeeId;
    private int leaveTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalDays;
    private String reason;
    private LeaveStatus status;
    private Integer approverId;
    private String approverComments;
    private LocalDateTime appliedAt;
    private LocalDateTime actionedAt;
    private String employeeName;
    private String employeeCode;
    private String leaveTypeName;
    private String approverName;

    public enum LeaveStatus {
        PENDING, APPROVED, REJECTED, CANCELLED
    }

    public LeaveRequest() {
        this.status = LeaveStatus.PENDING;
        this.appliedAt = LocalDateTime.now();
    }

    public LeaveRequest(int employeeId, int leaveTypeId, LocalDate startDate,
                        LocalDate endDate, String reason) {
        this();
        this.employeeId = employeeId;
        this.leaveTypeId = leaveTypeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.totalDays = calculateTotalDays();
    }

    public int calculateTotalDays() {
        if (startDate == null || endDate == null) return 0;
        return (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
    }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public int getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(int leaveTypeId) { this.leaveTypeId = leaveTypeId; }

    public int getLeaveRequestId() { return requestId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        this.totalDays = calculateTotalDays();
    }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        this.totalDays = calculateTotalDays();
    }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LeaveStatus getStatus() { return status; }
    public void setStatus(LeaveStatus status) { this.status = status; }

    public Integer getApproverId() { return approverId; }
    public void setApproverId(Integer approverId) { this.approverId = approverId; }

    public String getApproverComments() { return approverComments; }
    public void setApproverComments(String approverComments) { this.approverComments = approverComments; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    public LocalDateTime getActionedAt() { return actionedAt; }
    public void setActionedAt(LocalDateTime actionedAt) { this.actionedAt = actionedAt; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getLeaveTypeName() { return leaveTypeName; }
    public void setLeaveTypeName(String leaveTypeName) { this.leaveTypeName = leaveTypeName; }

    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }

    @Override
    public String toString() {
        return String.format("LeaveRequest[%d] %s : %s to %s (%d days) - %s",
                requestId, leaveTypeName, startDate, endDate, totalDays, status);
    }

    public boolean isCancellable() {
        return this.status == LeaveStatus.PENDING;
    }
}

