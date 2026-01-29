package com.revature.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


//  Announcement Entity - Company-wide announcements

public class Announcement {
    private int announcementId;
    private String title;
    private String content;
    private int employeeId;
    private boolean isActive;
    private Priority priority;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private LocalDateTime createdAt;
    private String createdByName;

    public enum Priority {
        HIGH, NORMAL, LOW
    }

    public Announcement() {
        this.isActive = true;
        this.priority = Priority.NORMAL;
        this.validFrom = LocalDate.now();
    }

    public Announcement(String title, String content, int empId) {
        this();
        this.title = title;
        this.content = content;
        this.employeeId = empId;
    }

    public int getAnnouncementId() { return announcementId; }
    public void setAnnouncementId(int announcementId) { this.announcementId = announcementId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public Integer getCreatedBy() { return employeeId; }
    public void setCreatedBy(Integer createdBy) {
        this.employeeId = (createdBy != null) ? createdBy : 0;
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }

    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public boolean isValid() {
        LocalDate today = LocalDate.now();
        boolean afterStart = validFrom == null || !today.isBefore(validFrom);
        boolean beforeEnd = validUntil == null || !today.isAfter(validUntil);
        return isActive && afterStart && beforeEnd;
    }

    public String getPriorityTag() {
        return switch (priority) {
            case HIGH -> "[!!! HIGH !!!]";
            case NORMAL -> "[NOTICE]";
            case LOW -> "[INFO]";
        };
    }

    public String getFormattedDate() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
    }

    @Override
    public String toString() {
        return String.format("%s %s - %s (%s)",
                getPriorityTag(), title,
                (content != null && content.length() > 50) ? content.substring(0, 50) : content,
                getFormattedDate());
    }
}


