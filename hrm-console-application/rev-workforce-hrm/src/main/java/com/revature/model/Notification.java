package com.revature.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


//  Notification Entity - In-app notifications for employees

public class Notification {
    private int notificationId;
    private int employeeId;
    private String title;
    private String message;
    private NotificationType notificationType;
    private boolean isRead;
    private Integer relatedId;
    private LocalDateTime createdAt;

    public enum NotificationType {
        LEAVE, PERFORMANCE, BIRTHDAY, ANNIVERSARY, ANNOUNCEMENT, SYSTEM
    }

    public Notification() {
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(int employeeId, String title, String message, NotificationType type) {
        this();
        this.employeeId = employeeId;
        this.title = title;
        this.message = message;
        this.notificationType = type;
    }

    public Notification(int employeeId, String title, String message,
                       NotificationType type, Integer relatedId) {
        this(employeeId, title, message, type);
        this.relatedId = relatedId;
    }

    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Integer getRelatedId() { return relatedId; }
    public void setRelatedId(Integer relatedId) { this.relatedId = relatedId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFormattedTime() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy | HH:mm"));
    }

    public String getTypeIcon() {
        return switch (notificationType) {
            case LEAVE -> "[LEAVE]";
            case PERFORMANCE -> "[PERF]";
            case BIRTHDAY -> "[BDAY]";
            case ANNIVERSARY -> "[ANNI]";
            case ANNOUNCEMENT -> "[NEWS]";
            case SYSTEM -> "[SYS]";
        };
    }

    @Override
    public String toString() {
        String readStatus = isRead ? "" : "[NEW] ";
        return String.format("%s%s %s - %s (%s)", 
            readStatus, getTypeIcon(), title, message.substring(0, Math.min(50, message.length())),
            getFormattedTime());
    }
}

