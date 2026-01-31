package com.revature.dao;

import com.revature.model.Designation;
import com.revature.util.DBConnection;
import com.revature.exception.AppException;
import com.revature.exception.AppException.ErrorCode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DesignationDAOImpl implements DesignationDAO {

    private static final String SELECT_ALL = "SELECT * FROM designations WHERE is_active = TRUE ORDER BY level, designation_name";
    private static final String SELECT_BY_ID = "SELECT * FROM designations WHERE designation_id = ?";
    private static final String INSERT_DESIGNATION = "INSERT INTO designations (designation_name, level) VALUES (?, ?)";
    private static final String UPDATE_DESIGNATION = "UPDATE designations SET designation_name = ?, level = ? WHERE designation_id = ?";

    @Override
    public List<Designation> findAll() {
        List<Designation> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToDesignation(rs));
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Error fetching designations");
        }
        return list;
    }

    @Override
    public List<Designation> getAll() throws AppException {
        List<Designation> list = new ArrayList<>();
        String sql = "SELECT * FROM designations";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Designation desig = new Designation();
                desig.setDesignationId(rs.getInt("designation_id"));
                desig.setDesignationName(rs.getString("designation_name"));
                list.add(desig);
            }
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR);
        }
        return list;
    }

    @Override
    public Optional<Designation> findById(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToDesignation(rs));
            }
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Error finding designation");
        }
        return Optional.empty();
    }

    @Override
    public int createDesignation(Designation designation) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_DESIGNATION, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, designation.getDesignationName());
            stmt.setInt(2, designation.getLevel());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "MySQL Insert Error");
        }
    }

    @Override
    public boolean updateDesignation(Designation designation) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_DESIGNATION)) {

            stmt.setString(1, designation.getDesignationName());
            stmt.setInt(2, designation.getLevel());
            stmt.setInt(3, designation.getDesignationId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Error updating designation");
        }
    }

    private Designation mapResultSetToDesignation(ResultSet rs) throws SQLException {
        Designation des = new Designation();
        des.setDesignationId(rs.getInt("designation_id"));
        des.setDesignationName(rs.getString("designation_name"));
        des.setLevel(rs.getInt("level"));
        des.setActive(rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) des.setCreatedAt(ts.toLocalDateTime());
        return des;
    }
}

