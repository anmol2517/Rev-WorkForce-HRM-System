package com.revature.service;

import com.revature.dao.*;
import com.revature.exception.AppException;
import com.revature.model.Notification;
import com.revature.model.Announcement;
import com.revature.model.AuditLog;
import com.revature.dao.AuditLogDAOImpl;
import com.revature.util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;


public class NotificationService {

    private final NotificationDAO notificationDAO;
    private final AnnouncementDAO announcementDAO;
    private final AuditLogDAO auditLogDAO;
    private AuthService authService;

    public NotificationService() {
        this.notificationDAO = new NotificationDAOImpl();
        this.announcementDAO = new AnnouncementDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }


    // ==================== NOTIFICATIONS ====================


    public Notification sendNotification(int employeeId, String title, String message,
                                         String type) throws AppException {
        if (ValidationUtil.isNullOrEmpty(title)) {
            throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Notification title is required");
        }
        if (ValidationUtil.isNullOrEmpty(message)) {
            throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Notification message is required");
        }

        try {
            Notification notification = new Notification();
            notification.setEmployeeId(employeeId);
            notification.setTitle(title);
            notification.setMessage(message);
            Notification.NotificationType finalType = Notification.NotificationType.SYSTEM;

            if (type != null) {
                try {
                    finalType = Notification.NotificationType.valueOf(type.toUpperCase());
                } catch (IllegalArgumentException e) {
                    finalType = Notification.NotificationType.SYSTEM;
                }
            }
            notification.setNotificationType(finalType);

            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());

            int generatedId = notificationDAO.createNotification(notification);
            notification.setNotificationId(generatedId);

            return notification;

        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to send notification : " + e.getMessage());
        }
    }

    public List<Notification> getMyNotifications() throws AppException {
        if (authService == null || authService.getLoggedInUser() == null) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "User not authenticated");
        }

        try {
            return notificationDAO.findByEmployee(
                    authService.getLoggedInUser().getEmployeeId());
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to fetch notifications : " + e.getMessage());
        }
    }

    public List<Notification> getUnreadNotifications() throws AppException {
        if (authService == null || authService.getLoggedInUser() == null) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "User not authenticated");
        }

        try {
            return notificationDAO.findUnreadByEmployee(
                    authService.getLoggedInUser().getEmployeeId());
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to fetch notifications : " + e.getMessage());
        }
    }

    public int getUnreadCount() throws AppException {
        if (authService == null || authService.getLoggedInUser() == null) {
            return 0;
        }

        try {
            return notificationDAO.countUnread(
                    authService.getLoggedInUser().getEmployeeId());
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to count notifications : " + e.getMessage());
        }
    }

    public void markAsRead(int notificationId) throws AppException {
        try {
            Notification notification = notificationDAO.findById(notificationId);

            if (notification == null) {
                throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Notification not found");
            }

            if (notification.getEmployeeId() != authService.getLoggedInUser().getEmployeeId()) {
                throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Access denied");
            }

            notificationDAO.markAsRead(notificationId);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to mark notification : " + e.getMessage());
        }
    }

    public void markAllAsRead() throws AppException {
        try {
            notificationDAO.markAllAsRead(
                    authService.getLoggedInUser().getEmployeeId());
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to mark notifications : " + e.getMessage());
        }
    }

    public void deleteNotification(int notificationId) throws AppException {
        try {
            Notification notification = notificationDAO.findById(notificationId);

            if (notification == null) {
                throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Notification not found");
            }

            if (notification.getEmployeeId() != authService.getLoggedInUser().getEmployeeId()) {
                throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Access denied");
            }

            notificationDAO.deleteNotification(notificationId);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to delete notification : " + e.getMessage());
        }
    }


    // ==================== ANNOUNCEMENTS ====================


    public void createAnnouncement(String title, String content) throws AppException {
        if (authService == null || !authService.isAdmin()) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Only Admin can post announcements");
        }

        try {
            Announcement announcement = new Announcement();
            announcement.setTitle(title);
            announcement.setContent(content);
            announcement.setCreatedBy(authService.getLoggedInUser().getEmployeeId());
            announcement.setCreatedAt(LocalDateTime.now());
            announcement.setActive(true);

            int generatedId = announcementDAO.createAnnouncement(announcement);
            logAudit("CREATE", "ANNOUNCEMENT", generatedId, "New Announcement : " + title);

            System.out.println("Announcement saved to database successfully.");

        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to save announcement : " + e.getMessage());
        }
    }

    public List<Announcement> getActiveAnnouncements() throws AppException {
        try {
            return announcementDAO.findActive();
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to fetch announcements");
        }
    }

    public List<AuditLog> getAuditLogs(int limit) throws AppException {
        try {
            AuditLogDAO auditLogDAO = new AuditLogDAOImpl();
            return auditLogDAO.findRecent(limit);
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Audit logs could not be loaded");

        }
    }

    public Announcement updateAnnouncement(Announcement announcement) throws AppException {
        if (!authService.isAdmin()) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Only Admin can update announcements");
        }

        try {
            Announcement existing = announcementDAO.findById(announcement.getAnnouncementId()).orElse(null);

            if (existing == null) {
                throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Announcement not found");
            }

            boolean isUpdated = announcementDAO.updateAnnouncement(announcement);

            if (!isUpdated) {
                throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to update announcement");
            }

            return announcement;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Error : " + e.getMessage());
        }
    }


    // ==================== ANNOUNCEMENTS ====================


    public void deactivateAnnouncement(int announcementId) throws AppException {
        if (authService == null || !authService.isAdmin()) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Only Admin can deactivate announcements");
        }

        try {
            boolean success = announcementDAO.setActive(announcementId, false);
            if (!success) {
                throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Announcement not found");
            }

            logAudit("UPDATE", "ANNOUNCEMENT", announcementId, "Announcement deactivated");
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to deactivate announcement");
        }
    }

    public List<Announcement> getAllAnnouncements() throws AppException {
        if (authService == null || !authService.isAdmin()) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Access denied");
        }
        try {
            return announcementDAO.findAll();
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to fetch all announcements");
        }
    }

    public List<Announcement> getRecentAnnouncements(int limit) throws AppException {
        try {
            return announcementDAO.findAll().stream()
                    .limit(limit)
                    .toList();
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to fetch recent announcements");
        }
    }


    // ==================== HELPER METHODS ====================


    private void logAudit(String action, String entity, Integer recordId, String description) {
        try {
            AuditLog log = new AuditLog();
            log.setActionType(AuditLog.ActionType.valueOf(action.toUpperCase()));

            String entityName = entity.toUpperCase();
            if(entityName.endsWith("S")) {
                entityName = entityName.substring(0, entityName.length() - 1);
            }
            log.setEntityType(AuditLog.EntityType.valueOf(entityName));

            log.setEntityId(recordId);
            log.setNewValue(description);

            if (authService != null && authService.getLoggedInUser() != null) {
                log.setEmployeeId(authService.getLoggedInUser().getEmployeeId());
            }

            log.setActionTimestamp(LocalDateTime.now());

            if (auditLogDAO != null) {
                auditLogDAO.logAudit(log);
            }
        } catch (Exception e) {
            System.err.println("Failed to log audit : " + e.getMessage());
        }
    }
}


