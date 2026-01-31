package com.revature.dao;

import com.revature.model.Announcement;
import com.revature.model.Announcement.Priority;
import com.revature.util.ConnectionFactory;
import com.revature.exception.AppException;
import com.revature.exception.AppException.ErrorCode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AnnouncementDAOImpl implements AnnouncementDAO {


    // SQL Queries

    private static final String INSERT_SQL = "INSERT INTO announcements (title, content, employee_id, priority, valid_from, valid_until) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE announcements SET title = ?, content = ?, priority = ?, valid_from = ?, valid_until = ? WHERE announcement_id = ?";
    private static final String SELECT_BASE = "SELECT a.*, CONCAT(e.first_name, ' ', e.last_name) as created_by_name FROM announcements a LEFT JOIN employees e ON a.employee_id = e.employee_id ";


    // ---  GenericDAO Methods Implementation ---

    @Override
    public Announcement create(Announcement a) throws SQLException {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, a.getTitle());
            stmt.setString(2, a.getContent());
            stmt.setInt(3, a.getEmployeeId());
            String p = (a.getPriority() != null) ? a.getPriority().name() : "MEDIUM";
            stmt.setString(4, p);
            stmt.setObject(5, (a.getValidFrom() != null) ? a.getValidFrom() : java.time.LocalDate.now());
            stmt.setObject(6, a.getValidUntil());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) a.setAnnouncementId(rs.getInt(1));
            }
            return a;
        }
    }

    @Override
    public Optional<Announcement> findById(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE a.announcement_id = ?";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToAnnouncement(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Announcement> findAll() throws SQLException {
        List<Announcement> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY a.created_at DESC";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToAnnouncement(rs));
        }
        return list;
    }

    @Override
    public boolean update(Announcement a) throws SQLException {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, a.getTitle());
            stmt.setString(2, a.getContent());
            stmt.setString(3, a.getPriority().name());
            stmt.setObject(4, a.getValidFrom());
            stmt.setObject(5, a.getValidUntil());
            stmt.setInt(6, a.getAnnouncementId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM announcements WHERE announcement_id = ?";
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public int createAnnouncement(Announcement a) {
        try {
            return create(a).getAnnouncementId();
        } catch (SQLException e) {
            System.out.println(com.revature.util.ConsoleUtil.RED + "[SQL ERROR]: " + e.getMessage() + com.revature.util.ConsoleUtil.RESET);
            throw new AppException(ErrorCode.DATABASE_ERROR, "Creation failed: " + e.getMessage());
        }
    }

    @Override
    public boolean updateAnnouncement(Announcement a) {
        try {
            return update(a);
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Update failed");
        }
    }

    @Override
    public boolean deleteAnnouncement(int id) {
        try {
            return delete(id);
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Delete failed");
        }
    }

    @Override
    public List<Announcement> findActive() {
        try {
            List<Announcement> list = new ArrayList<>();
            String sql = SELECT_BASE + "WHERE a.is_active = TRUE AND (a.valid_from IS NULL OR a.valid_from <= CURRENT_DATE) AND (a.valid_until IS NULL OR a.valid_until >= CURRENT_DATE) ORDER BY a.priority DESC";
            try (Connection conn = ConnectionFactory.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToAnnouncement(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Fetch active failed");
        }
    }

    @Override
    public boolean setActive(int id, boolean active) {
        try {
            String sql = "UPDATE announcements SET is_active = ? WHERE announcement_id = ?";
            try (Connection conn = ConnectionFactory.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBoolean(1, active);
                stmt.setInt(2, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Status toggle failed");
        }
    }


    private Announcement mapResultSetToAnnouncement(ResultSet rs) throws SQLException {
        Announcement a = new Announcement();
        a.setAnnouncementId(rs.getInt("announcement_id"));
        a.setTitle(rs.getString("title"));
        a.setContent(rs.getString("content"));
        a.setEmployeeId(rs.getInt("employee_id"));

        try {
            String pStr = rs.getString("priority");

            if (pStr.equals("URGENT") || pStr.equals("CRITICAL")) {
                a.setPriority(Announcement.Priority.HIGH);
            } else {
                a.setPriority(Announcement.Priority.valueOf(pStr));
            }
        } catch (Exception e) {
            a.setPriority(Announcement.Priority.NORMAL);
        }

        a.setActive(rs.getBoolean("is_active"));

        Date vFrom = rs.getDate("valid_from");
        if (vFrom != null) a.setValidFrom(vFrom.toLocalDate());
        Date vUntil = rs.getDate("valid_until");
        if (vUntil != null) a.setValidUntil(vUntil.toLocalDate());

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            a.setCreatedAt(ts.toLocalDateTime());
        }


        try { a.setCreatedByName(rs.getString("created_by_name")); } catch (SQLException e) {}
        return a;
    }
}

