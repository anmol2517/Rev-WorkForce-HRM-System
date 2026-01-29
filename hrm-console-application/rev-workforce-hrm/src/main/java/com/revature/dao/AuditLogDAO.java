package com.revature.dao;

import com.revature.exception.AppException;
import com.revature.model.AuditLog;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface AuditLogDAO {
    int logAudit(AuditLog log);
    int logAudit(Connection conn, AuditLog log) throws SQLException;
    List<AuditLog> findRecent(int limit) throws AppException;
    int deleteOldLogs() throws AppException;
}


