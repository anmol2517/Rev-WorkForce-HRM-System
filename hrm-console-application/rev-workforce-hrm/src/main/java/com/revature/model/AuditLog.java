package com.revature.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


//  AuditLog Entity - Tracks all system changes for accountability


public class AuditLog {
    private int logId;
    private Integer employeeId;
    private ActionType actionType;
    private EntityType entityType;
    private Integer entityId;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private LocalDateTime actionTimestamp;
    
    private String employeeName;

    public enum ActionType {
        CREATE, UPDATE, DELETE, LOGIN, LOGOUT, APPROVE, REJECT, CANCEL, INSERT
    }

    public enum EntityType {
        EMPLOYEE, LEAVE, PERFORMANCE, GOAL, NOTIFICATION, ANNOUNCEMENT, SYSTEM
    }

    public AuditLog() {
        this.actionTimestamp = LocalDateTime.now();
    }

    public AuditLog(Integer employeeId, ActionType actionType, EntityType entityType,
                   Integer entityId, String oldValue, String newValue) {
        this();
        this.employeeId = employeeId;
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }

    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }

    public EntityType getEntityType() { return entityType; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }

    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getActionTimestamp() { return actionTimestamp; }
    public void setActionTimestamp(LocalDateTime actionTimestamp) { this.actionTimestamp = actionTimestamp; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public LocalDateTime getChangedAt() { return actionTimestamp; }
    public String getTableName() { return entityType != null ? entityType.name() : "N/A"; }
    public String getAction() { return actionType != null ? actionType.name() : "N/A"; }

    public String getFormattedTimestamp() {
        if (actionTimestamp == null) return "";
        return actionTimestamp.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %s on %s (ID: %d) by Employee %d",
            getFormattedTimestamp(), actionType, entityType, entityId,
            entityId != null ? entityId : 0, employeeId != null ? employeeId : 0);
    }
}


