package com.revature.dao;

import com.revature.model.LeaveBalance;
import com.revature.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LeaveBalanceDAOImpl implements LeaveBalanceDAO {

    private static final String INITIALIZE_SQL = """
        INSERT IGNORE INTO leave_balances (employee_id, leave_type_id, year, total_leaves, used_leaves)
        SELECT ?, leave_type_id, ?, max_days_per_year, 0
        FROM leave_types WHERE is_active = TRUE
        """;

    private static final String SELECT_BASE = """
        SELECT lb.*, lt.type_name as leave_type_name, 
        (lb.total_leaves - lb.used_leaves) as remaining_days,
        CONCAT(e.first_name, ' ', e.last_name) as employee_name
        FROM leave_balances lb
        JOIN leave_types lt ON lb.leave_type_id = lt.leave_type_id
        JOIN employees e ON lb.employee_id = e.employee_id
        """;

    @Override
    public LeaveBalance create(LeaveBalance balance) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO leave_balances (employee_id, leave_type_id, year, total_leaves, used_leaves) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, balance.getEmployeeId());
                stmt.setInt(2, balance.getLeaveTypeId());
                stmt.setInt(3, balance.getYear());
                stmt.setInt(4, balance.getTotalLeaves());
                stmt.setInt(5, balance.getUsedLeaves());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) balance.setBalanceId(rs.getInt(1));
                }
                return balance;
            }
        }
    }

    @Override
    public Optional<LeaveBalance> findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE lb.balance_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToLeaveBalance(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<LeaveBalance> findAll() throws SQLException {
        List<LeaveBalance> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_BASE)) {
            while (rs.next()) list.add(mapResultSetToLeaveBalance(rs));
        }
        return list;
    }

    @Override
    public boolean update(LeaveBalance balance) throws SQLException {
        String sql = "UPDATE leave_balances SET total_leaves = ?, used_leaves = ? WHERE balance_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, balance.getTotalLeaves());
            stmt.setInt(2, balance.getUsedLeaves());
            stmt.setInt(3, balance.getBalanceId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM leave_balances WHERE balance_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public void initializeBalances(int employeeId, int year) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INITIALIZE_SQL)) {
            stmt.setInt(1, employeeId);
            stmt.setInt(2, year);
            stmt.executeUpdate();
        }
    }

    @Override
    public boolean deductLeaves(Connection conn, int employeeId, int leaveTypeId, int year, int days) throws SQLException {
        String sql = "UPDATE leave_balances SET used_leaves = used_leaves + ? " +
                "WHERE employee_id = ? AND leave_type_id = ? AND year = ? " +
                "AND (total_leaves - used_leaves) >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, days);
            stmt.setInt(2, employeeId);
            stmt.setInt(3, leaveTypeId);
            stmt.setInt(4, year);
            stmt.setInt(5, days);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean creditLeaves(Connection conn, int employeeId, int leaveTypeId, int year, int days) throws SQLException {
        String sql = "UPDATE leave_balances SET used_leaves = GREATEST(0, used_leaves - ?) " +
                "WHERE employee_id = ? AND leave_type_id = ? AND year = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, days);
            stmt.setInt(2, employeeId);
            stmt.setInt(3, leaveTypeId);
            stmt.setInt(4, year);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean hasSufficientBalance(int employeeId, int leaveTypeId, int year, int daysRequired) throws SQLException {
        String sql = "SELECT (total_leaves - used_leaves) as remaining_days FROM leave_balances " +
                "WHERE employee_id = ? AND leave_type_id = ? AND year = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeId);
            pstmt.setInt(2, leaveTypeId);
            pstmt.setInt(3, year);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int remaining = rs.getInt("remaining_days");
                    System.out.println("DEBUG: DB Remaining = " + remaining + ", Required = " + daysRequired);
                    return remaining >= daysRequired;
                } else {
                    System.out.println("DEBUG : No record found in DB for Emp : " + employeeId + " Type : " + leaveTypeId + " Year : " + year);
                }
            }
        }
        return false;
    }

    @Override
    public List<LeaveBalance> findByEmployeeAndYear(int employeeId, int year) throws SQLException {
        String sql = SELECT_BASE + " WHERE lb.employee_id = ? AND lb.year = ? ORDER BY lt.type_name";
        List<LeaveBalance> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeeId);
            stmt.setInt(2, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToLeaveBalance(rs));
            }
        }
        return list;
    }

    private LeaveBalance mapResultSetToLeaveBalance(ResultSet rs) throws SQLException {
        LeaveBalance lb = new LeaveBalance();
        lb.setBalanceId(rs.getInt("balance_id"));
        lb.setEmployeeId(rs.getInt("employee_id"));
        lb.setLeaveTypeId(rs.getInt("leave_type_id"));
        lb.setYear(rs.getInt("year"));
        lb.setTotalLeaves(rs.getInt("total_leaves"));
        lb.setUsedLeaves(rs.getInt("used_leaves"));
        try {
            lb.setRemainingLeaves(rs.getInt("remaining_days"));
            lb.setLeaveTypeName(rs.getString("leave_type_name"));
        } catch (SQLException ignored) {}
        return lb;
    }
}