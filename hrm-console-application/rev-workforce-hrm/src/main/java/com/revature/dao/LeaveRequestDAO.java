package com.revature.dao;

import com.revature.model.LeaveRequest;
import com.revature.model.LeaveRequest.LeaveStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LeaveRequestDAO extends GenericDAO<LeaveRequest> {

    boolean updateStatus(Connection conn, int requestId, LeaveStatus status, int approverId, String comments) throws SQLException;
    boolean updateStatus(int requestId, LeaveStatus status, int approverId, String comments) throws SQLException;

    List<LeaveRequest> findByManager(int managerId) throws SQLException;
    List<LeaveRequest> findByEmployee(int employeeId) throws SQLException;
    List<LeaveRequest> findPendingByManager(int managerId) throws SQLException;

    boolean cancelRequest(int requestId) throws SQLException;
    boolean hasOverlappingLeaves(int employeeId, int excludeRequestId, LocalDate start, LocalDate end) throws SQLException;

    boolean approveLeaveWithBalance(int requestId, int approverId, String comments) throws SQLException;
    boolean cancelLeaveWithBalance(int requestId) throws SQLException;
}