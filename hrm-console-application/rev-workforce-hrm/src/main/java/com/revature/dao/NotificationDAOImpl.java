package com.revature.dao;

import com.revature.model.Notification;
import com.revature.model.Notification.NotificationType;
import com.revature.util.DBConnection;
import com.revature.exception.AppException;
import com.revature.exception.AppException.ErrorCode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAOImpl implements NotificationDAO {

    private static final String INSERT_NOTIFICATION = "INSERT INTO notifications (employee_id, title, message, notification_type, related_id, is_read, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String MARK_AS_READ = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";
    private static final String MARK_ALL_AS_READ = "UPDATE notifications SET is_read = TRUE WHERE employee_id = ? AND is_read = FALSE";
    private static final String DELETE_NOTIFICATION = "DELETE FROM notifications WHERE notification_id = ?";
    private static final String DELETE_OLD_NOTIFICATIONS = "DELETE FROM notifications WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY)";
    private static final String SELECT_BY_ID = "SELECT * FROM notifications WHERE notification_id = ?";
    private static final String SELECT_BY_EMPLOYEE = "SELECT * FROM notifications WHERE employee_id = ? ORDER BY created_at DESC LIMIT 50";
    private static final String SELECT_UNREAD_BY_EMPLOYEE = "SELECT * FROM notifications WHERE employee_id = ? AND is_read = FALSE ORDER BY created_at DESC";
    private static final String COUNT_UNREAD = "SELECT COUNT(*) FROM notifications WHERE employee_id = ? AND is_read = FALSE";
    private static final String SELECT_BY_TYPE = "SELECT * FROM notifications WHERE employee_id = ? AND notification_type = ? ORDER BY created_at DESC LIMIT 20";

    @Override
    public int createNotification(Notification notification) {
        try (Connection conn = DBConnection.getConnection()) {
            return createNotification(conn, notification);
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Error creating notification: " + e.getMessage());
        }
    }

    @Override
    public int createNotification(Connection conn, Notification notification) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_NOTIFICATION, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, notification.getEmployeeId());
            stmt.setString(2, notification.getTitle());
            stmt.setString(3, notification.getMessage());
            stmt.setString(4, notification.getNotificationType().name());
            stmt.setObject(5, notification.getRelatedId());
            stmt.setBoolean(6, notification.isRead());
            stmt.setTimestamp(7, Timestamp.valueOf(notification.getCreatedAt()));

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;
        }
    }

    @Override
    public boolean markAsRead(int notificationId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(MARK_AS_READ)) {
            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { throw new AppException(ErrorCode.DATABASE_ERROR, "Mark as read failed"); }
    }

    @Override
    public int markAllAsRead(int employeeId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(MARK_ALL_AS_READ)) {
            stmt.setInt(1, employeeId);
            return stmt.executeUpdate();
        } catch (SQLException e) { throw new AppException(ErrorCode.DATABASE_ERROR, "Mark all failed"); }
    }

    @Override
    public boolean deleteNotification(int notificationId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_NOTIFICATION)) {
            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { throw new AppException(ErrorCode.DATABASE_ERROR, "Delete failed"); }
    }

    @Override
    public int deleteOldNotifications() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_OLD_NOTIFICATIONS)) {
            return stmt.executeUpdate();
        } catch (SQLException e) { throw new AppException(ErrorCode.DATABASE_ERROR, "Cleanup failed"); }
    }

    @Override
    public Notification findById(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Fetch failed : " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Notification> findByEmployee(int empId) {
        List<Notification> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMPLOYEE)) {
            stmt.setInt(1, empId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { throw new AppException(ErrorCode.DATABASE_ERROR, "Fetch failed"); }
        return list;
    }

    @Override
    public List<Notification> findUnreadByEmployee(int empId) {
        List<Notification> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_UNREAD_BY_EMPLOYEE)) {
            stmt.setInt(1, empId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { throw new AppException(ErrorCode.DATABASE_ERROR, "Fetch failed"); }
        return list;
    }

    @Override
    public int countUnread(int empId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_UNREAD)) {
            stmt.setInt(1, empId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { throw new AppException(ErrorCode.DATABASE_ERROR, "Count failed"); }
        return 0;
    }

    @Override
    public List<Notification> findByType(int empId, NotificationType type) {
        List<Notification> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_TYPE)) {
            stmt.setInt(1, empId);
            stmt.setString(2, type.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { throw new AppException(ErrorCode.DATABASE_ERROR, "Fetch failed"); }
        return list;
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("notification_id"));
        n.setEmployeeId(rs.getInt("employee_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setNotificationType(NotificationType.valueOf(rs.getString("notification_type")));
        n.setRead(rs.getBoolean("is_read"));
        n.setRelatedId(rs.getObject("related_id", Integer.class));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) n.setCreatedAt(ts.toLocalDateTime());
        return n;
    }
}

