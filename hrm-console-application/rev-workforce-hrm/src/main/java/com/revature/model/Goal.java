package com.revature.model;

import java.time.LocalDate;
import java.time.LocalDateTime;


//  Goal Entity - Employee goals and targets

public class Goal {
    private int goalId;
    private int employeeId;
    private int goalYear;
    private String goalDescription;
    private String successMetrics;
    private Priority priority;
    private LocalDate deadline;
    private int progressPercentage;
    private GoalStatus status;
    private String managerGuidance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String employeeName;
    private String employeeCode;

    public enum Priority {
        HIGH, MEDIUM, LOW
    }

    public enum GoalStatus {
        NOT_STARTED, IN_PROGRESS, COMPLETED, ON_HOLD
    }

    public Goal() {
        this.priority = Priority.MEDIUM;
        this.status = GoalStatus.NOT_STARTED;
        this.progressPercentage = 0;
        this.goalYear = java.time.Year.now().getValue();
    }

    public Goal(int employeeId, String goalDescription, Priority priority, LocalDate deadline) {
        this();
        this.employeeId = employeeId;
        this.goalDescription = goalDescription;
        this.priority = priority;
        this.deadline = deadline;
    }

    public int getGoalId() { return goalId; }
    public void setGoalId(int goalId) { this.goalId = goalId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public int getGoalYear() { return goalYear; }
    public void setGoalYear(int goalYear) { this.goalYear = goalYear; }

    public String getGoalDescription() { return goalDescription; }
    public void setGoalDescription(String goalDescription) { this.goalDescription = goalDescription; }

    public String getSuccessMetrics() { return successMetrics; }
    public void setSuccessMetrics(String successMetrics) { this.successMetrics = successMetrics; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }


    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getTitle() { return goalDescription; }
    public LocalDate getTargetDate() { return deadline; }

    public void setTitle(String title) { this.goalDescription = title; }
    public void setDescription(String desc) { this.goalDescription = desc; }
    public String getDescription() { return goalDescription; }
    public void setTargetDate(LocalDate date) { this.deadline = date; }



    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) {
        if (progressPercentage < 0 || progressPercentage > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }
        this.progressPercentage = progressPercentage;
        if (progressPercentage == 100) {
            this.status = GoalStatus.COMPLETED;
        } else if (progressPercentage > 0) {
            this.status = GoalStatus.IN_PROGRESS;
        }
    }

    public GoalStatus getStatus() { return status; }
    public void setStatus(GoalStatus status) { this.status = status; }

    public String getManagerGuidance() { return managerGuidance; }
    public void setManagerGuidance(String managerGuidance) { this.managerGuidance = managerGuidance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }


    public boolean isOverdue() {
        return deadline != null && LocalDate.now().isAfter(deadline)
               && status != GoalStatus.COMPLETED;
    }

    public String getProgressBar() {
        int filled = progressPercentage / 10;
        int empty = 10 - filled;
        return "[" + "=".repeat(filled) + " ".repeat(empty) + "] " + progressPercentage + "%";
    }

    @Override
    public String toString() {
        return String.format("Goal[%d] %s - %s (%s) %s",
            goalId, goalDescription.substring(0, Math.min(30, goalDescription.length())),
            priority, status, getProgressBar());
    }
}

