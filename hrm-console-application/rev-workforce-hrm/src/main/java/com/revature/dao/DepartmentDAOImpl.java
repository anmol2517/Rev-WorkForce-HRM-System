package com.revature.dao;

import com.revature.model.Department;
import com.revature.util.DBConnection;
import com.revature.exception.AppException;
import com.revature.exception.AppException.ErrorCode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepartmentDAOImpl implements DepartmentDAO {

    private static final String SELECT_ALL = "SELECT * FROM departments WHERE is_active = TRUE ORDER BY department_name";
    private static final String SELECT_BY_ID = "SELECT * FROM departments WHERE department_id = ?";
    private static final String INSERT_DEPARTMENT = "INSERT INTO departments (department_name, description) VALUES (?, ?)";
    private static final String UPDATE_DEPARTMENT = "UPDATE departments SET department_name = ?, description = ? WHERE department_id = ?";

    @Override
    public List<Department> findAll() {
        List<Department> departments = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                departments.add(mapResultSetToDepartment(rs));
            }
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Error fetching departments");
        }
        return departments;
    }

    @Override
    public Optional<Department> findById(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDepartment(rs));
                }
            }
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Error finding department");
        }
        return Optional.empty();
    }

    @Override
    public int createDepartment(Department department) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_DEPARTMENT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, department.getDepartmentName());
            stmt.setString(2, department.getDescription());
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
    public boolean updateDepartment(Department department) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_DEPARTMENT)) {
            stmt.setString(1, department.getDepartmentName());
            stmt.setString(2, department.getDescription());
            stmt.setInt(3, department.getDepartmentId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Error updating department");
        }
    }

    private Department mapResultSetToDepartment(ResultSet rs) throws SQLException {
        Department dept = new Department();
        dept.setDepartmentId(rs.getInt("department_id"));
        dept.setDepartmentName(rs.getString("department_name"));
        dept.setDescription(rs.getString("description"));
        dept.setActive(rs.getBoolean("is_active"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) dept.setCreatedAt(createdAt.toLocalDateTime());
        return dept;
    }

    @Override
    public List<Department> getAll() throws AppException {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT * FROM departments";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Department dept = new Department();
                dept.setDepartmentId(rs.getInt("department_id"));
                dept.setDepartmentName(rs.getString("department_name"));
                list.add(dept);
            }
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR);
        }
        return list;
    }
}

