package com.revature.dao;

import com.revature.model.LeaveBalance;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface LeaveBalanceDAO extends GenericDAO<LeaveBalance> {
    void initializeBalances(int employeeId, int year) throws SQLException;

    boolean deductLeaves(Connection conn, int employeeId, int leaveTypeId, int year, int days) throws SQLException;
    boolean creditLeaves(Connection conn, int employeeId, int leaveTypeId, int year, int days) throws SQLException;

    boolean hasSufficientBalance(int employeeId, int leaveTypeId, int year, int daysRequired) throws SQLException;
    List<LeaveBalance> findByEmployeeAndYear(int employeeId, int year) throws SQLException;
}