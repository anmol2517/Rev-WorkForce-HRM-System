package com.revature.dao;

import com.revature.model.Holiday;
import com.revature.util.DBConnection;
import com.revature.exception.AppException;
import com.revature.exception.AppException.ErrorCode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HolidayDAOImpl implements HolidayDAO {


    // SQL Queries


    private static final String INSERT_HOLIDAY = "INSERT INTO holidays (holiday_name, holiday_date, description, year, is_optional) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_HOLIDAY = "UPDATE holidays SET holiday_name = ?, holiday_date = ?, description = ?, is_optional = ? WHERE holiday_id = ?";
    private static final String DELETE_HOLIDAY = "DELETE FROM holidays WHERE holiday_id = ?";
    private static final String SELECT_BY_ID = "SELECT * FROM holidays WHERE holiday_id = ?";
    private static final String SELECT_BY_YEAR = "SELECT * FROM holidays WHERE year = ? ORDER BY holiday_date";
    private static final String SELECT_UPCOMING = "SELECT * FROM holidays WHERE holiday_date >= CURRENT_DATE ORDER BY holiday_date LIMIT 10";
    private static final String SELECT_ALL = "SELECT * FROM holidays ORDER BY holiday_date DESC";


    // --- GenericDAO Methods (Required for Interface Sync) ---

    @Override
    public Holiday create(Holiday holiday) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_HOLIDAY, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, holiday.getHolidayName());
            stmt.setObject(2, holiday.getHolidayDate());
            stmt.setString(3, holiday.getDescription());
            stmt.setInt(4, holiday.getYear());
            stmt.setBoolean(5, holiday.isOptional());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) holiday.setHolidayId(rs.getInt(1));
            }
            return holiday;
        }
    }

    @Override
    public boolean update(Holiday holiday) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_HOLIDAY)) {
            stmt.setString(1, holiday.getHolidayName());
            stmt.setObject(2, holiday.getHolidayDate());
            stmt.setString(3, holiday.getDescription());
            stmt.setBoolean(4, holiday.isOptional());
            stmt.setInt(5, holiday.getHolidayId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_HOLIDAY)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Holiday> findById(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToHoliday(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Holiday> findAll() throws SQLException {
        List<Holiday> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToHoliday(rs));
        }
        return list;
    }


    // --- HolidayDAO Specific Methods (Keep all features) ---

    @Override
    public int createHoliday(Holiday holiday) {
        try {
            return create(holiday).getHolidayId();
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Holiday creation failed");
        }
    }

    @Override
    public boolean updateHoliday(Holiday holiday) {
        try {
            return update(holiday);
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Holiday update failed");
        }
    }

    @Override
    public boolean deleteHoliday(int id) {
        try {
            return delete(id);
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Holiday delete failed");
        }
    }

    @Override
    public List<Holiday> findByYear(int year) throws SQLException {
        List<Holiday> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_YEAR)) {
            stmt.setInt(1, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToHoliday(rs));
            }
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Fetch by year failed");
        }
        return list;
    }

    @Override
    public List<Holiday> findUpcoming() throws SQLException {
        List<Holiday> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_UPCOMING);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToHoliday(rs));
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Fetch upcoming failed");
        }
        return list;
    }


    //   --- Helper Mapping ---

    private Holiday mapResultSetToHoliday(ResultSet rs) throws SQLException {
        Holiday h = new Holiday();
        h.setHolidayId(rs.getInt("holiday_id"));
        h.setHolidayName(rs.getString("holiday_name"));
        Date d = rs.getDate("holiday_date");
        if (d != null) h.setHolidayDate(d.toLocalDate());
        h.setDescription(rs.getString("description"));
        h.setYear(rs.getInt("year"));
        h.setOptional(rs.getBoolean("is_optional"));
        return h;
    }
}

