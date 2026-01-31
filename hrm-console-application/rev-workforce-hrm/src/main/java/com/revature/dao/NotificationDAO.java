package com.revature.dao;

import com.revature.model.Notification;
import com.revature.model.Notification.NotificationType;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface NotificationDAO {
    int createNotification(Notification notification);
    int createNotification(Connection conn, Notification notification) throws SQLException;
    boolean markAsRead(int notificationId);
    int markAllAsRead(int employeeId);
    boolean deleteNotification(int notificationId);

    int deleteOldNotifications();
    List<Notification> findByEmployee(int employeeId);
    List<Notification> findUnreadByEmployee(int employeeId);
    int countUnread(int employeeId);
    List<Notification> findByType(int employeeId, NotificationType type);
    Notification findById(int notificationId) throws SQLException;
}

