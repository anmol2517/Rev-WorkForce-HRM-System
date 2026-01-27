package com.revature.dao;

import com.revature.model.AuditLog;
import com.revature.model.AuditLog.ActionType;
import com.revature.model.AuditLog.EntityType;
import com.revature.util.DBConnection;
import com.revature.exception.AppException;
import com.revature.exception.AppException.ErrorCode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAOImpl implements AuditLogDAO {

    private static final String INSERT_LOG = "INSERT INTO audit_logs (employee_id, action_type, entity_type, entity_id, old_value, new_value, ip_address) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_RECENT = "SELECT al.*, CONCAT(e.first_name, ' ', e.last_name) as employee_name FROM audit_logs al LEFT JOIN employees e ON al.employee_id = e.employee_id ORDER BY al.action_timestamp DESC LIMIT ?";
    private static final String DELETE_OLD_LOGS = "DELETE FROM audit_logs WHERE action_timestamp < DATE_SUB(NOW(), INTERVAL 365 DAY)";

    @Override
    public int logAudit(AuditLog log) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_LOG, Statement.RETURN_GENERATED_KEYS)) {
            setParams(stmt, log);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            return -1;
        }
    }

    @Override
    public int logAudit(Connection conn, AuditLog log) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_LOG, Statement.RETURN_GENERATED_KEYS)) {
            setParams(stmt, log);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    @Override
    public List<AuditLog> findRecent(int limit) {
        List<AuditLog> logs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_RECENT)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) logs.add(mapRow(rs));
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Audit fetch failed");
        }
        return logs;
    }

    @Override
    public int deleteOldLogs() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_OLD_LOGS)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Cleanup failed");
        }
    }

    private void setParams(PreparedStatement stmt, AuditLog log) throws SQLException {
        stmt.setObject(1, log.getEmployeeId());
        stmt.setString(2, log.getActionType().name());
        stmt.setString(3, log.getEntityType().name());
        stmt.setObject(4, log.getEntityId());
        stmt.setString(5, log.getOldValue());
        stmt.setString(6, log.getNewValue());
        stmt.setString(7, log.getIpAddress());
    }

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getInt("log_id"));
        log.setEmployeeId(rs.getObject("employee_id", Integer.class));
        log.setActionType(ActionType.valueOf(rs.getString("action_type")));
        log.setEntityType(EntityType.valueOf(rs.getString("entity_type")));

        log.setEntityId(rs.getObject("entity_id", Integer.class));
        log.setIpAddress(rs.getString("ip_address"));

        log.setOldValue(rs.getString("old_value"));
        log.setNewValue(rs.getString("new_value"));

        Timestamp ts = rs.getTimestamp("action_timestamp");
        if (ts != null) log.setActionTimestamp(ts.toLocalDateTime());

        return log;
    }
}

