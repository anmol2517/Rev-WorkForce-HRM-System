package com.revature.dao;

import com.revature.model.LeaveType;
import com.revature.util.DBConnection;
import com.revature.exception.AppException;
import com.revature.exception.AppException.ErrorCode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LeaveTypeDAOImpl implements LeaveTypeDAO {
    private static final String INSERT_SQL = "INSERT INTO leave_types (type_name, description, max_days_per_year, is_carry_forward) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE leave_types SET type_name = ?, description = ?, max_days_per_year = ?, is_carry_forward = ? WHERE leave_type_id = ?";
    private static final String SELECT_BY_ID = "SELECT * FROM leave_types WHERE leave_type_id = ?";
    private static final String SELECT_ALL = "SELECT * FROM leave_types ORDER BY type_name";

    @Override
    public LeaveType create(LeaveType type) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, type.getTypeName());
            stmt.setString(2, type.getDescription());
            stmt.setInt(3, type.getMaxDaysPerYear());
            stmt.setBoolean(4, type.isCarryForward());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) type.setLeaveTypeId(rs.getInt(1));
            }
            return type;
        }
    }

    @Override
    public Optional<LeaveType> findById(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToLeaveType(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<LeaveType> findAll() throws SQLException {
        List<LeaveType> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToLeaveType(rs));
        }
        return list;
    }

    @Override
    public boolean update(LeaveType type) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, type.getTypeName());
            stmt.setString(2, type.getDescription());
            stmt.setInt(3, type.getMaxDaysPerYear());
            stmt.setBoolean(4, type.isCarryForward());
            stmt.setInt(5, type.getLeaveTypeId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE leave_types SET is_active = FALSE WHERE leave_type_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // --- LeaveType Specific Methods ---

    @Override
    public Optional<LeaveType> findByName(String name) throws SQLException {
        String sql = "SELECT * FROM leave_types WHERE type_name = ? AND is_active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToLeaveType(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean setActive(int id, boolean active) throws SQLException {
        String sql = "UPDATE leave_types SET is_active = ? WHERE leave_type_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<LeaveType> findAllActive() throws SQLException {
        String sql = "SELECT * FROM leave_types WHERE is_active = TRUE";
        List<LeaveType> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToLeaveType(rs));
        }
        return list;
    }

    private LeaveType mapResultSetToLeaveType(ResultSet rs) throws SQLException {
        LeaveType type = new LeaveType();
        type.setLeaveTypeId(rs.getInt("leave_type_id"));
        type.setTypeName(rs.getString("type_name"));
        type.setDescription(rs.getString("description"));
        type.setMaxDaysPerYear(rs.getInt("max_days_per_year"));
        type.setCarryForward(rs.getBoolean("is_carry_forward"));
        type.setActive(rs.getBoolean("is_active"));
        return type;
    }
}

