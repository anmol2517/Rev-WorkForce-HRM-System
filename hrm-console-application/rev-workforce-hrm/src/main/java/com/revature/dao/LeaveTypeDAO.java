package com.revature.dao;

import com.revature.model.LeaveType;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface LeaveTypeDAO extends GenericDAO<LeaveType> {
    Optional<LeaveType> findByName(String typeName) throws SQLException;
    List<LeaveType> findAllActive() throws SQLException;
    boolean setActive(int id, boolean active) throws SQLException;
}

