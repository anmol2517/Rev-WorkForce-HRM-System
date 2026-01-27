package com.revature.service;

import com.revature.dao.AuditLogDAOImpl;
import com.revature.dao.EmployeeDAO;
import com.revature.dao.AuditLogDAO;
import com.revature.dao.EmployeeDAOImpl;
import com.revature.exception.AppException;
import com.revature.model.Employee;
import com.revature.model.AuditLog;
import com.revature.util.ValidationUtil;
import java.time.LocalDateTime;
import java.util.Optional;

public class AuthService {

    private final EmployeeDAO employeeDAO;
    private final AuditLogDAO auditLogDAO;
    private static Employee currentUser = null;

    public AuthService() {
        this.employeeDAO = new EmployeeDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    public Employee getLoggedInUser() {
        return currentUser;
    }

    public Employee login(String email, String password) throws AppException {
        if (!ValidationUtil.isValidEmail(email)) {
            throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Invalid email format");
        }

        Optional<Employee> employeeOpt = employeeDAO.findByEmail(email.trim());

        Employee employee = employeeOpt.orElseThrow(() -> {
            logFailedLogin(email, "User not found");
            return new AppException(AppException.ErrorCode.AUTH_INVALID_CREDENTIALS, "User not found");
        });

        if (!employee.isActive()) {
            logFailedLogin(email, "Account inactive");
            throw new AppException(AppException.ErrorCode.AUTH_ACCOUNT_INACTIVE, "Account is inactive");
        }

        if (!password.equals(employee.getPassword())) {
            logFailedLogin(email, "Invalid password");
            throw new AppException(AppException.ErrorCode.AUTH_INVALID_CREDENTIALS, "Invalid credentials");
        }

        currentUser = employee;
        logAudit(AuditLog.ActionType.LOGIN, AuditLog.EntityType.EMPLOYEE,
                employee.getEmployeeId(), "User logged in successfully");

        return employee;
    }

    public void logout() {
        if (currentUser != null) {
            logAudit(AuditLog.ActionType.LOGOUT, AuditLog.EntityType.EMPLOYEE, currentUser.getEmployeeId(), "User logged out");
            currentUser = null;
        }
    }

    public void changePassword(String oldPassword, String newPassword, String confirmPassword) throws AppException {
        if (currentUser == null) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "User not logged in");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Passwords do not match");
        }

        if (!oldPassword.equals(currentUser.getPassword())) {
            throw new AppException(AppException.ErrorCode.AUTH_PASSWORD_MISMATCH, "Incorrect old password");
        }

        employeeDAO.updatePassword(currentUser.getEmployeeId(), newPassword);
        currentUser.setPassword(newPassword);
        logAudit(AuditLog.ActionType.UPDATE, AuditLog.EntityType.EMPLOYEE, currentUser.getEmployeeId(), "Password changed successfully");
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == com.revature.model.Employee.Role.ADMIN;
    }

    public boolean isManager() {
        return currentUser != null && currentUser.getRole() == com.revature.model.Employee.Role.MANAGER;
    }

    private void logFailedLogin(String email, String reason) {
        try {
            AuditLog log = new AuditLog();
            log.setActionType(AuditLog.ActionType.LOGIN);
            log.setEntityType(AuditLog.EntityType.EMPLOYEE);
            log.setNewValue("Email : " + email + ", Reason : " + reason);
            log.setActionTimestamp(LocalDateTime.now());
            auditLogDAO.logAudit(log);
        } catch (Exception e) {
            System.err.println("Audit error (failed login) : " + e.getMessage());
        }
    }

    private void logAudit(AuditLog.ActionType action, AuditLog.EntityType entity, Integer recordId, String desc) {
        try {
            AuditLog log = new AuditLog();
            log.setActionType(action);
            log.setEntityType(entity);
            log.setEntityId(recordId);
            log.setNewValue(desc);
            log.setEmployeeId(currentUser != null ? currentUser.getEmployeeId() : null);
            log.setActionTimestamp(LocalDateTime.now());
            auditLogDAO.logAudit(log);
        } catch (Exception e) {
            System.err.println("Audit error : " + e.getMessage());
        }
    }

    public void resetPassword(int employeeId, String newPassword) throws AppException {
        employeeDAO.updatePassword(employeeId, newPassword);
    }
}

