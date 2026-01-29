package com.revature.dao;

import com.revature.model.LeaveRequest;
import com.revature.model.LeaveRequest.LeaveStatus;
import com.revature.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LeaveRequestDAOImpl implements LeaveRequestDAO {

    private static final String INSERT_SQL = "INSERT INTO leave_requests (employee_id, leave_type_id, start_date, end_date, total_days, reason, status) VALUES (?, ?, ?, ?, ?, ?, 'PENDING')";

    private static final String SELECT_BASE = """
        SELECT lr.*, lt.type_name as leave_type_name,
            CONCAT(e.first_name, ' ', e.last_name) as employee_name,
            e.employee_code,
            CONCAT(a.first_name, ' ', a.last_name) as approver_name
        FROM leave_requests lr
        JOIN leave_types lt ON lr.leave_type_id = lt.leave_type_id
        JOIN employees e ON lr.employee_id = e.employee_id
        LEFT JOIN employees a ON lr.approver_id = a.employee_id
        """;

    @Override
    public LeaveRequest create(LeaveRequest request) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, request.getEmployeeId());
            stmt.setInt(2, request.getLeaveTypeId());
            stmt.setObject(3, request.getStartDate());
            stmt.setObject(4, request.getEndDate());
            stmt.setInt(5, request.getTotalDays());
            stmt.setString(6, request.getReason());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) request.setRequestId(rs.getInt(1));
            }
            return request;
        }
    }

    @Override
    public Optional<LeaveRequest> findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE lr.request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToLeaveRequest(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<LeaveRequest> findAll() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY lr.applied_at DESC";
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToLeaveRequest(rs));
        }
        return list;
    }

    @Override
    public boolean update(LeaveRequest request) throws SQLException {
        String sql = "UPDATE leave_requests SET start_date = ?, end_date = ?, total_days = ?, reason = ? WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, request.getStartDate());
            stmt.setObject(2, request.getEndDate());
            stmt.setInt(3, request.getTotalDays());
            stmt.setString(4, request.getReason());
            stmt.setInt(5, request.getRequestId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return cancelRequest(id);
    }

    @Override
    public boolean updateStatus(int requestId, LeaveStatus status, int approverId, String comments) throws SQLException {
        String sql = "UPDATE leave_requests SET status = ?, approver_id = ?, approver_comments = ?, actioned_at = CURRENT_TIMESTAMP WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, approverId);
            stmt.setString(3, comments);
            stmt.setInt(4, requestId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(Connection conn, int requestId, LeaveStatus status, int approverId, String comments) throws SQLException {
        String sql = "UPDATE leave_requests SET status = ?, approver_id = ?, approver_comments = ?, actioned_at = CURRENT_TIMESTAMP WHERE request_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, approverId);
            stmt.setString(3, comments);
            stmt.setInt(4, requestId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean approveLeaveWithBalance(int requestId, int approverId, String comments) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Optional<LeaveRequest> opt = findById(requestId);
            if (opt.isEmpty()) return false;
            LeaveRequest req = opt.get();

            updateStatus(conn, requestId, LeaveStatus.APPROVED, approverId, comments);

            String deductSql = "UPDATE leave_balances SET used_leaves = used_leaves + ? " +
                    "WHERE employee_id = ? AND leave_type_id = ? AND year = ? " +
                    "AND (total_leaves - used_leaves) >= ?";

            try (PreparedStatement stmt = conn.prepareStatement(deductSql)) {
                stmt.setInt(1, req.getTotalDays());
                stmt.setInt(2, req.getEmployeeId());
                stmt.setInt(3, req.getLeaveTypeId());
                stmt.setInt(4, req.getStartDate().getYear());
                stmt.setInt(5, req.getTotalDays());

                if (stmt.executeUpdate() == 0) {
                    throw new SQLException("Insufficient balance for the year " + req.getStartDate().getYear());
                }
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            if (conn != null) conn.rollback();
            throw ex;
        } finally {
            if (conn != null) conn.close();
        }
    }

    @Override
    public boolean cancelLeaveWithBalance(int requestId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Optional<LeaveRequest> opt = findById(requestId);
            if (opt.isEmpty()) return false;
            LeaveRequest req = opt.get();

            if (req.getStatus() == LeaveStatus.APPROVED) {
                String creditSql = "UPDATE leave_balances SET used_leaves = used_leaves - ? WHERE employee_id = ? AND leave_type_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(creditSql)) {
                    stmt.setInt(1, req.getTotalDays());
                    stmt.setInt(2, req.getEmployeeId());
                    stmt.setInt(3, req.getLeaveTypeId());
                    stmt.executeUpdate();
                }
            }

            String cancelSql = "UPDATE leave_requests SET status = 'CANCELLED', actioned_at = CURRENT_TIMESTAMP WHERE request_id = ? AND status IN ('PENDING', 'APPROVED')";
            try (PreparedStatement stmt = conn.prepareStatement(cancelSql)) {
                stmt.setInt(1, requestId);
                if (stmt.executeUpdate() == 0) throw new SQLException("Cannot cancel this request.");
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            if (conn != null) conn.rollback();
            throw ex;
        } finally {
            if (conn != null) conn.close();
        }
    }

    @Override
    public boolean cancelRequest(int requestId) throws SQLException {
        String sql = "UPDATE leave_requests SET status = 'CANCELLED', actioned_at = CURRENT_TIMESTAMP WHERE request_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<LeaveRequest> findByEmployee(int employeeId) throws SQLException {
        String sql = SELECT_BASE + " WHERE lr.employee_id = ? ORDER BY lr.applied_at DESC";
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToLeaveRequest(rs));
            }
        }
        return list;
    }

    @Override
    public List<LeaveRequest> findByManager(int managerId) throws SQLException {
        String sql = SELECT_BASE + " WHERE e.manager_id = ? ORDER BY lr.applied_at DESC";
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, managerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToLeaveRequest(rs));
            }
        }
        return list;
    }

    @Override
    public List<LeaveRequest> findPendingByManager(int managerId) throws SQLException {
        String sql = SELECT_BASE + " WHERE e.manager_id = ? AND lr.status = 'PENDING' ORDER BY lr.applied_at ASC";
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, managerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToLeaveRequest(rs));
            }
        }
        return list;
    }

    @Override
    public boolean hasOverlappingLeaves(int employeeId, int excludeRequestId, LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT COUNT(*) FROM leave_requests WHERE employee_id = ? AND status IN ('PENDING', 'APPROVED') AND request_id != ? AND NOT (end_date < ? OR start_date > ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeeId);
            stmt.setInt(2, excludeRequestId);
            stmt.setObject(3, start);
            stmt.setObject(4, end);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private LeaveRequest mapResultSetToLeaveRequest(ResultSet rs) throws SQLException {
        LeaveRequest request = new LeaveRequest();
        request.setRequestId(rs.getInt("request_id"));
        request.setEmployeeId(rs.getInt("employee_id"));
        request.setLeaveTypeId(rs.getInt("leave_type_id"));
        request.setStartDate(rs.getDate("start_date").toLocalDate());
        request.setEndDate(rs.getDate("end_date").toLocalDate());
        request.setTotalDays(rs.getInt("total_days"));
        request.setReason(rs.getString("reason"));
        request.setStatus(LeaveStatus.valueOf(rs.getString("status")));


        int approverId = rs.getInt("approver_id");
        if (!rs.wasNull()) request.setApproverId(approverId);
        request.setApproverComments(rs.getString("approver_comments"));
        request.setAppliedAt(rs.getTimestamp("applied_at").toLocalDateTime());
        Timestamp actionedAt = rs.getTimestamp("actioned_at");
        if (actionedAt != null) request.setActionedAt(actionedAt.toLocalDateTime());
        try {
            request.setLeaveTypeName(rs.getString("leave_type_name"));
            request.setEmployeeName(rs.getString("employee_name"));
            request.setEmployeeCode(rs.getString("employee_code"));
            request.setApproverName(rs.getString("approver_name"));
        } catch (SQLException ignored) {}
        return request;
    }
}

