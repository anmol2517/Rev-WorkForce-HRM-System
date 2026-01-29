package com.revature.service;

import com.revature.dao.*;
import com.revature.exception.AppException;
import com.revature.model.*;
import com.revature.util.DBConnection;
import com.revature.util.PasswordUtil;
import com.revature.util.ValidationUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class EmployeeService {

    private final EmployeeDAO employeeDAO;
    private final DepartmentDAO departmentDAO;
    private final DesignationDAO designationDAO;
    private final AuditLogDAO auditLogDAO;
    private final LeaveBalanceDAO leaveBalanceDAO;
    private final LeaveTypeDAO leaveTypeDAO;
    private final AuthService authService;

    public EmployeeService(AuthService authService) {
        this.employeeDAO = new EmployeeDAOImpl();
        this.departmentDAO = new DepartmentDAOImpl();
        this.designationDAO = new DesignationDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
        this.leaveBalanceDAO = new LeaveBalanceDAOImpl();
        this.leaveTypeDAO = new LeaveTypeDAOImpl();
        this.authService = authService;
    }

    public Employee viewMyProfile() throws AppException {
        try {
            int currentUserId = authService.getLoggedInUser().getEmployeeId();
            return employeeDAO.findById(currentUserId)
                    .orElseThrow(() -> new AppException(AppException.ErrorCode.INVALID_INPUT, "Profile not found"));
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Error fetching profile");
        }
    }

    public Employee updateEmployee(Employee employee) throws AppException {
        Employee current = authService.getLoggedInUser();
        if (!authService.isAdmin() && (current == null || current.getEmployeeId() != employee.getEmployeeId())) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED);
        }
        try {
            boolean success = employeeDAO.update(employee);
            if (!success) throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Update failed");
            logAudit(AuditLog.ActionType.UPDATE, AuditLog.EntityType.EMPLOYEE, employee.getEmployeeId(), "Updated");
            return employee;
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Update failed : " + e.getMessage());
        }
    }

    public Employee getEmployeeById(int id) throws AppException {
        try {
            return employeeDAO.findById(id)
                    .orElseThrow(() -> new AppException(AppException.ErrorCode.EMPLOYEE_NOT_FOUND));
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Fetch failed");
        }
    }

    public List<Employee> getAllEmployees() throws AppException {
        try {
            if (authService.isAdmin()) {
                return employeeDAO.findAll();
            } else {
                Employee current = authService.getLoggedInUser();
                if (current == null) throw new AppException(AppException.ErrorCode.UNAUTHORIZED);
                return employeeDAO.findByManagerId(current.getEmployeeId());
            }
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Fetch all failed");
        }
    }

    public Employee addEmployee(Employee employee, String password) throws AppException {
        if (!authService.isAdmin()) throw new AppException(AppException.ErrorCode.UNAUTHORIZED);
        validateEmployeeData(employee);
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            if (employeeDAO.findByEmail(employee.getEmail()).isPresent()) {
                throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Email already exists");
            }
            employee.setPassword(PasswordUtil.hashPassword(password));
            employee.setActive(true);

            Employee createdEmployee = employeeDAO.create(employee);
            initializeLeaveBalances(createdEmployee.getEmployeeId(), conn);

            logAudit(AuditLog.ActionType.CREATE, AuditLog.EntityType.EMPLOYEE, createdEmployee.getEmployeeId(), "Created");
            conn.commit();
            return createdEmployee;
        } catch (Exception e) {
            rollback(conn);
            if (e instanceof AppException) throw (AppException) e;
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Add failed : " + e.getMessage());
        } finally {
            resetAutoCommit(conn);
            if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    public void deactivateEmployee(int employeeId) throws AppException {
        if (!authService.isAdmin()) throw new AppException(AppException.ErrorCode.UNAUTHORIZED);
        try {
            boolean success = employeeDAO.updateStatus(employeeId, false);
            if (!success) throw new AppException(AppException.ErrorCode.NOT_FOUND, "Employee not found");
            logAudit(AuditLog.ActionType.UPDATE, AuditLog.EntityType.EMPLOYEE, employeeId, "Deactivated");
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Deactivation failed");
        }
    }

    public List<Designation> getAllDesignations() throws AppException {
        try {
            return designationDAO.findAll();
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to fetch designations");
        }
    }

    public List<Employee> getAllManagers() throws AppException {
        try {
            return employeeDAO.findByRole(Employee.Role.MANAGER);
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to fetch managers list");
        }
    }

    public List<Employee> searchEmployees(String searchTerm) throws AppException {
        try {
            return employeeDAO.search(searchTerm);
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Search failed for : " + searchTerm);
        }
    }

    public void assignManager(int employeeId, int managerId) throws AppException {
        try {
            boolean success = employeeDAO.updateManager(employeeId, managerId);
            if (!success) {
                throw new AppException(AppException.ErrorCode.NOT_FOUND, "Employee not found");
            }
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to assign manager");
        }
    }

    public void reactivateEmployee(int employeeId) throws AppException {
        try {
            boolean success = employeeDAO.updateStatus(employeeId, true);
            if (!success) {
                throw new AppException(AppException.ErrorCode.NOT_FOUND, "Employee not found");
            }
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to reactivate employee");
        }
    }

    public List<Employee> getTeamMembers() throws AppException {
        Employee current = authService.getLoggedInUser();
        if (current == null) throw new AppException(AppException.ErrorCode.UNAUTHORIZED);
        return employeeDAO.findByManagerId(current.getEmployeeId());
    }

    public List<Department> getAllDepartments() throws AppException {
        return departmentDAO.getAll();
    }

    private void initializeLeaveBalances(int empId, Connection conn) throws SQLException {
        List<LeaveType> types = leaveTypeDAO.findAll();
        int currentYear = LocalDate.now().getYear();

        for (LeaveType t : types) {
            if (t.isActive()) {
                LeaveBalance b = new LeaveBalance();
                b.setEmployeeId(empId);
                b.setLeaveTypeId(t.getLeaveTypeId());
                b.setYear(currentYear);
                b.setTotalLeaves(t.getMaxDaysPerYear());
                b.setUsedLeaves(0);
                leaveBalanceDAO.create(b);
            }
        }
    }

    private void validateEmployeeData(Employee e) throws AppException {
        if (!ValidationUtil.isValidEmail(e.getEmail())) throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Invalid Email");
        if (e.getRole() == null) throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Role required");
    }

    private void logAudit(AuditLog.ActionType action, AuditLog.EntityType entity, Integer id, String desc) {
        try {
            AuditLog log = new AuditLog();
            log.setActionType(action);
            log.setEntityType(entity);
            log.setEntityId(id);
            log.setNewValue(desc);
            Employee current = authService.getLoggedInUser();
            log.setEmployeeId(current != null ? current.getEmployeeId() : null);
            log.setActionTimestamp(LocalDateTime.now());
            auditLogDAO.logAudit(log);
        } catch (Exception ignored) {}
    }

    private void rollback(Connection c) {
        if (c != null) try { c.rollback(); } catch (SQLException ignored) {}
    }

    private void resetAutoCommit(Connection c) {
        if (c != null) try { c.setAutoCommit(true); } catch (SQLException ignored) {}
    }
}


