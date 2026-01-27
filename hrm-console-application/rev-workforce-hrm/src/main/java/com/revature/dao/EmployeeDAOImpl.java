package com.revature.dao;

import com.revature.exception.AppException;
import com.revature.model.Employee;
import com.revature.model.Employee.Role;
import com.revature.util.ConnectionFactory;
import com.revature.util.ConsoleUtil;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeDAOImpl implements EmployeeDAO {

    private static final String SELECT_BASE = """
        SELECT e.*, d.department_name, des.designation_name,
            CONCAT(m.first_name, ' ', m.last_name) as manager_name
        FROM employees e
        LEFT JOIN departments d ON e.department_id = d.department_id
        LEFT JOIN designations des ON e.designation_id = des.designation_id
        LEFT JOIN employees m ON e.manager_id = m.employee_id
        """;

    @Override
    public Employee create(Employee e) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection()) {
            if (e.getEmployeeCode() == null || e.getEmployeeCode().isEmpty()) {
                e.setEmployeeCode(getNextEmployeeCode());
            }
            String sql = "INSERT INTO employees (employee_code, first_name, last_name, email, password, phone, address, date_of_birth, emergency_contact, department_id, designation_id, manager_id, role, salary, joining_date, security_question, security_answer) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                mapEmployeeToStatement(stmt, e);
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) e.setEmployeeId(rs.getInt(1));
                }
                return e;
            }
        } catch (SQLException ex) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Create failed : " + ex.getMessage());
        }
    }

    @Override
    public boolean updateManager(int employeeId, int managerId) throws SQLException {
        String sql = "UPDATE employees SET manager_id = ? WHERE employee_id = ?";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, managerId);
            pstmt.setInt(2, employeeId);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<Employee> findByRole(Employee.Role role) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE e.role = ? AND e.is_active = true AND e.is_deleted = FALSE";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role.name());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }
        }
        return employees;
    }

    @Override
    public boolean updateStatus(int id, boolean s) {
        String sql = "UPDATE employees SET is_active = ? WHERE employee_id = ?";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, s);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Update failed: " + e.getMessage());
        }
    }

    @Override
    public boolean setActiveStatus(int id, boolean s) {
        return updateStatus(id, s);
    }


    @Override
    public List<Employee> getManagers() {
        List<Employee> managers = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE role IN ('MANAGER', 'ADMIN', '2', '1') AND is_deleted = FALSE";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                managers.add(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Managers fetch failed");
        }
        return managers;
    }

    @Override
    public boolean updateSalaryAndRole(int empId, BigDecimal newSalary, Role newRole) {
        String sql = "UPDATE employees SET salary = ?, role = ? WHERE employee_id = ?";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, newSalary);
            stmt.setString(2, newRole.name());
            stmt.setInt(3, empId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Salary update failed");
        }
    }

    @Override
    public Optional<Employee> findById(int id) {
        String sql = SELECT_BASE + " WHERE e.employee_id = ? AND e.is_deleted = FALSE";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Find by ID failed");
        }
        return Optional.empty();
    }

    @Override
    public List<Employee> findAll() {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_BASE + " WHERE e.is_deleted = FALSE")) {
            List<Employee> list = new ArrayList<>();
            while (rs.next()) list.add(mapResultSetToEmployee(rs));
            return list;
        } catch (SQLException ex) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Fetch All failed");
        }
    }

    @Override
    public boolean update(Employee e) {
        String sql = "UPDATE employees SET first_name=?, last_name=?, email=?, department_id=?, designation_id=?, role=?, salary=? WHERE employee_id=?";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, e.getFirstName());
            stmt.setString(2, e.getLastName());
            stmt.setString(3, e.getEmail());
            stmt.setInt(4, e.getDepartmentId());
            stmt.setInt(5, e.getDesignationId());
            stmt.setString(6, e.getRole().name());
            stmt.setBigDecimal(7, e.getSalary());
            stmt.setInt(8, e.getEmployeeId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Update failed");
        }
    }


    @Override
    public boolean delete(int id) {
        String sql = "UPDATE employees SET is_deleted = TRUE WHERE employee_id = ?";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Delete failed");
        }
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        String sql = SELECT_BASE + " WHERE e.email = ? AND e.is_deleted = FALSE";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToEmployee(rs));
            }
        } catch (SQLException ex) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Email fetch failed");
        }
        return Optional.empty();
    }

    @Override
    public void updatePassword(int id, String pass) {
        String sql = "UPDATE employees SET password = ? WHERE employee_id = ?";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pass);
            stmt.setInt(2, id);
            if (stmt.executeUpdate() == 0) throw new AppException(AppException.ErrorCode.NOT_FOUND);
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR);
        }
    }

    @Override
    public boolean updateProfile(int id, String phone, String addr, String ec) {
        String sql = "UPDATE employees SET phone=?, address=?, emergency_contact=? WHERE employee_id=?";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            stmt.setString(2, addr);
            stmt.setString(3, ec);
            stmt.setInt(4, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Profile update failed");
        }
    }

    @Override
    public void assignManager(int empId, int managerId) {
        String sql = "UPDATE employees SET manager_id = ? WHERE employee_id = ?";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, managerId);
            stmt.setInt(2, empId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Manager assignment failed");
        }
    }

    @Override
    public List<Employee> findByManagerId(int mid) {
        List<Employee> list = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE e.manager_id = ? AND e.is_deleted = FALSE";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mid);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToEmployee(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Manager lookup failed");
        }
    }

    @Override
    public String getNextEmployeeCode() {
        String sql = "SELECT MAX(CAST(SUBSTRING(employee_code, 4) AS UNSIGNED)) FROM employees";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return String.format("EMP%03d", rs.getInt(1) + 1);
        } catch (SQLException ex) {
            return "EMP001";
        }
        return "EMP001";
    }

    @Override
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM employees WHERE email = ? AND is_deleted = FALSE";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Check failed");
        }
    }

    @Override
    public boolean codeExists(String code) {
        String sql = "SELECT COUNT(*) FROM employees WHERE employee_code = ? AND is_deleted = FALSE";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Code check failed");
        }
    }

    @Override
    public Optional<Employee> findByCode(String code) {
        String sql = SELECT_BASE + " WHERE e.employee_code = ? AND e.is_deleted = FALSE";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Find by code failed");
        }
        return Optional.empty();
    }

    @Override
    public List<Employee> search(String keyword) {
        String sql = SELECT_BASE + """
             WHERE (e.first_name LIKE ? 
                OR e.last_name LIKE ? 
                OR e.employee_code LIKE ? 
                OR d.department_name LIKE ?) 
             AND e.is_deleted = FALSE
            """;
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            stmt.setString(1, k); stmt.setString(2, k);
            stmt.setString(3, k); stmt.setString(4, k);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Employee> list = new ArrayList<>();
                while (rs.next()) list.add(mapResultSetToEmployee(rs));
                return list;
            }
        } catch (SQLException ex) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Search failed");
        }
    }

    @Override
    public List<Employee> findBirthdaysThisMonth() {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_BASE + " WHERE MONTH(date_of_birth) = MONTH(CURRENT_DATE) AND is_deleted = FALSE")) {
            List<Employee> list = new ArrayList<>();
            while (rs.next()) list.add(mapResultSetToEmployee(rs));
            return list;
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Birthday fetch failed");
        }
    }

    @Override
    public List<Employee> findWorkAnniversariesThisMonth() {
        String sql = SELECT_BASE + " WHERE MONTH(joining_date) = MONTH(CURRENT_DATE) AND is_deleted = FALSE";
        List<Employee> list = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSetToEmployee(rs));
            return list;
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Anniversary fetch failed");
        }
    }

    private void mapEmployeeToStatement(PreparedStatement stmt, Employee e) throws SQLException {
        stmt.setString(1, e.getEmployeeCode());
        stmt.setString(2, e.getFirstName());
        stmt.setString(3, e.getLastName());
        stmt.setString(4, e.getEmail());
        stmt.setString(5, e.getPassword());
        stmt.setString(6, e.getPhone());
        stmt.setString(7, e.getAddress());
        stmt.setObject(8, e.getDateOfBirth());
        stmt.setString(9, e.getEmergencyContact());
        stmt.setInt(10, e.getDepartmentId());
        stmt.setInt(11, e.getDesignationId());
        if (e.getManagerId() != null) stmt.setInt(12, e.getManagerId()); else stmt.setNull(12, Types.INTEGER);
        stmt.setString(13, e.getRole().name());
        stmt.setBigDecimal(14, e.getSalary());
        stmt.setObject(15, e.getJoiningDate());
        stmt.setString(16, e.getSecurityQuestion());
        stmt.setString(17, e.getSecurityAnswer());
    }

    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setEmployeeId(rs.getInt("employee_id"));
        e.setEmployeeCode(rs.getString("employee_code"));
        e.setFirstName(rs.getString("first_name"));
        e.setLastName(rs.getString("last_name"));
        e.setEmail(rs.getString("email"));
        e.setPassword(rs.getString("password"));

        String roleStr = rs.getString("role");
        if (roleStr != null) {
            roleStr = roleStr.toUpperCase().trim();
            if (roleStr.equals("1")) {
                e.setRole(Role.ADMIN);
            } else if (roleStr.equals("2")) {
                e.setRole(Role.MANAGER);
            } else {
                try {
                    e.setRole(Role.valueOf(roleStr));
                } catch (IllegalArgumentException ex) {
                    e.setRole(Role.EMPLOYEE);
                }
            }
        }

        e.setPhone(rs.getString("phone"));
        e.setAddress(rs.getString("address"));
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) e.setDateOfBirth(dob.toLocalDate());
        e.setSalary(rs.getBigDecimal("salary"));
        try {
            e.setActive(rs.getBoolean("is_active"));
        } catch (SQLException ex) {
            String status = rs.getString("status");
            e.setActive("ACTIVE".equalsIgnoreCase(status));
        }
        Date joinDate = rs.getDate("joining_date");
        if (joinDate != null) e.setJoiningDate(joinDate.toLocalDate());
        e.setDepartmentId(rs.getInt("department_id"));
        e.setDesignationId(rs.getInt("designation_id"));
        int managerId = rs.getInt("manager_id");
        if (!rs.wasNull()) e.setManagerId(managerId);
        try {
            e.setDepartmentName(rs.getString("department_name"));
            e.setDesignationName(rs.getString("designation_name"));
            e.setManagerName(rs.getString("manager_name"));
        } catch (SQLException ignored) {}
        return e;
    }
}