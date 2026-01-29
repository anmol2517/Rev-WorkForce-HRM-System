package com.revature.service;

import com.revature.dao.*;
import com.revature.exception.AppException;
import com.revature.model.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class LeaveService {
    private final LeaveRequestDAO leaveRequestDAO;
    private final LeaveBalanceDAO leaveBalanceDAO;
    private final LeaveTypeDAO leaveTypeDAO;
    private final HolidayDAO holidayDAO;
    private final EmployeeDAO employeeDAO;
    private final AuditLogDAO auditLogDAO;
    private final NotificationService notificationService;
    private final AuthService authService;

    public LeaveService(AuthService authService, NotificationService notificationService) {
        this.leaveRequestDAO = new LeaveRequestDAOImpl();
        this.leaveBalanceDAO = new LeaveBalanceDAOImpl();
        this.leaveTypeDAO = new LeaveTypeDAOImpl();
        this.holidayDAO = new HolidayDAOImpl();
        this.employeeDAO = new EmployeeDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
        this.notificationService = notificationService;
        this.authService = authService;
    }

    public void approveLeave(int leaveRequestId, String managerComments) throws AppException {
        Employee user = authService.getLoggedInUser();
        if (user.getRole() != Employee.Role.MANAGER && user.getRole() != Employee.Role.ADMIN) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Only Manager/Admin can approve");
        }
        try {
            LeaveRequest request = leaveRequestDAO.findById(leaveRequestId)
                    .orElseThrow(() -> new AppException(AppException.ErrorCode.INVALID_INPUT, "Request not found"));
            if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
                throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Request is already " + request.getStatus());
            }
            boolean success = leaveRequestDAO.approveLeaveWithBalance(leaveRequestId, user.getEmployeeId(), managerComments);
            if (!success) {
                throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Approval failed due to balance or DB error");
            }
            logAudit(null, AuditLog.ActionType.APPROVE, AuditLog.EntityType.LEAVE_REQUEST,
                    leaveRequestId, "Status : PENDING", "Status : APPROVED | Comments : " + managerComments);
            sendApprovalNotification(request, true, managerComments);
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Database issue : " + e.getMessage());
        }
    }

    public void rejectLeave(int leaveRequestId, String managerComments) throws AppException {
        Employee user = authService.getLoggedInUser();
        if (user.getRole() != Employee.Role.MANAGER && user.getRole() != Employee.Role.ADMIN) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Only Manager/Admin can reject");
        }
        try {
            LeaveRequest request = leaveRequestDAO.findById(leaveRequestId)
                    .orElseThrow(() -> new AppException(AppException.ErrorCode.INVALID_INPUT, "Request not found"));
            boolean success = leaveRequestDAO.updateStatus(leaveRequestId, LeaveRequest.LeaveStatus.REJECTED, user.getEmployeeId(), managerComments);
            if (success) {
                logAudit(null, AuditLog.ActionType.REJECT, AuditLog.EntityType.LEAVE_REQUEST,
                        leaveRequestId, "Status: PENDING", "Status: REJECTED | Reason: " + managerComments);
                sendApprovalNotification(request, false, managerComments);
            }
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, e.getMessage());
        }
    }

    public void cancelLeave(int requestId) throws AppException {
        try {
            boolean success = leaveRequestDAO.cancelLeaveWithBalance(requestId);
            if (!success) throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Cancel failed.");
            logAudit(null, AuditLog.ActionType.CANCEL, AuditLog.EntityType.LEAVE_REQUEST,
                    requestId, "Action: USER_REQUEST", "Status: CANCELLED");
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, e.getMessage());
        }
    }

    public LeaveRequest applyLeave(LeaveRequest request) throws AppException {
        Employee loggedInUser = authService.getLoggedInUser();
        request.setEmployeeId(loggedInUser.getEmployeeId());

        LocalDate backDateLimit = LocalDate.now().minusDays(7);
        if (request.getStartDate().isBefore(backDateLimit)) {
            throw new AppException(AppException.ErrorCode.INVALID_INPUT,
                    "Back-dated leaves older than 7 days are not allowed. Limit is : " + backDateLimit);
        }
        try {
            if (leaveRequestDAO.hasOverlappingLeaves(request.getEmployeeId(), 0, request.getStartDate(), request.getEndDate())) {
                throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Overlap detected");
            }
            int workingDays = calculateWorkingDays(request.getStartDate(), request.getEndDate());
            request.setTotalDays(workingDays);

            int leaveYear = request.getStartDate().getYear();
            boolean hasBalance = leaveBalanceDAO.hasSufficientBalance(
                    request.getEmployeeId(),
                    request.getLeaveTypeId(),
                    leaveYear,
                    workingDays
            );
            if (!hasBalance) {
                throw new AppException(AppException.ErrorCode.INVALID_INPUT,
                        "Insufficient balance or No record found for " + leaveYear);
            }
            return leaveRequestDAO.create(request);
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, e.getMessage());
        }
    }

    public int calculateWorkingDays(LocalDate startDate, LocalDate endDate) throws AppException {
        try {
            List<LocalDate> holidays = holidayDAO.findByYear(startDate.getYear()).stream().map(Holiday::getHolidayDate).collect(Collectors.toList());
            int days = 0;
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY && !holidays.contains(date))
                    days++;
            }
            return days;
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, e.getMessage());
        }
    }

    public List<LeaveRequest> getMyLeaveHistory() throws AppException {
        try {
            return leaveRequestDAO.findByEmployee(authService.getLoggedInUser().getEmployeeId());
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, e.getMessage());
        }
    }

    public List<LeaveRequest> getTeamLeaveRequests() throws AppException {
        Employee user = authService.getLoggedInUser();
        try {
            return authService.isAdmin() ? leaveRequestDAO.findAll() : leaveRequestDAO.findByManager(user.getEmployeeId());
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, e.getMessage());
        }
    }

    public List<LeaveRequest> getPendingRequests() throws AppException {
        Employee user = authService.getLoggedInUser();
        try {
            return authService.isAdmin() ? leaveRequestDAO.findAll().stream().filter(r -> r.getStatus() == LeaveRequest.LeaveStatus.PENDING).collect(Collectors.toList()) : leaveRequestDAO.findPendingByManager(user.getEmployeeId());
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, e.getMessage());
        }
    }

    public List<LeaveBalance> getMyLeaveBalances() throws AppException {
        try {
            int currentYear = LocalDate.now().getYear();
            return leaveBalanceDAO.findByEmployeeAndYear(
                    authService.getLoggedInUser().getEmployeeId(),
                    currentYear
            );
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, e.getMessage());
        }
    }

    private void logAudit(Connection conn, AuditLog.ActionType action, AuditLog.EntityType entity,
                          Integer id, String oldVal, String newVal) {
        try {
            AuditLog log = new AuditLog();
            log.setActionType(action);
            log.setEntityType(entity);
            log.setEntityId(id);
            log.setOldValue(oldVal);
            log.setNewValue(newVal);
            log.setEmployeeId(authService.getLoggedInUser().getEmployeeId());
            log.setActionTimestamp(LocalDateTime.now());
            if (conn != null) {
                auditLogDAO.logAudit(conn, log);
            } else {
                auditLogDAO.logAudit(log);
            }
        } catch (Exception ex) {
            System.err.println("Audit Logging Failed : " + ex.getMessage());
        }
    }

    public List<Holiday> getHolidays() throws AppException {
        try {
            return holidayDAO.findByYear(LocalDate.now().getYear());
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Error fetching holidays : " + e.getMessage());
        }
    }

    public void addHoliday(Holiday holiday) throws AppException {
        try {
            holidayDAO.create(holiday);
            logAudit(null, AuditLog.ActionType.CREATE, AuditLog.EntityType.LEAVE_REQUEST,
                    null, "System Update", "Added Holiday : " + holiday.getHolidayName());
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Error adding holiday : " + e.getMessage());
        }
    }

    public List<LeaveType> getAllLeaveTypes() throws AppException {
        try {
            return leaveTypeDAO.findAll();
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Error fetching leave types : " + e.getMessage());
        }
    }

    public List<LeaveBalance> getLeaveBalances(int employeeId) throws AppException {
        try {
            int currentYear = LocalDate.now().getYear();
            return leaveBalanceDAO.findByEmployeeAndYear(employeeId, currentYear);
        } catch (SQLException e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Error fetching member balances : " + e.getMessage());
        }
    }

    private void sendApprovalNotification(LeaveRequest request, boolean isApproved, String comments) {
        try {
            String status = isApproved ? "APPROVED" : "REJECTED";
            String title = "Leave Request " + status;
            String message = String.format("Your leave request for %s to %s has been %s. Manager Comments: %s",
                    request.getStartDate(), request.getEndDate(), status.toLowerCase(),
                    (comments == null || comments.isEmpty()) ? "None" : comments);
            notificationService.sendNotification(
                    request.getEmployeeId(),
                    title,
                    message,
                    "LEAVE"
            );
        } catch (Exception e) {
            System.err.println("Notification Trigger Failed : " + e.getMessage());
        }
    }
}