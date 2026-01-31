package com.revature.dao;

import com.revature.model.PerformanceReview;
import com.revature.model.PerformanceReview.ReviewStatus;
import com.revature.util.DBConnection;
import com.revature.exception.AppException;
import com.revature.exception.AppException.ErrorCode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PerformanceReviewDAOImpl implements PerformanceReviewDAO {

    private static final String INSERT_REVIEW = "INSERT INTO performance_reviews (employee_id, review_year, review_period, status) VALUES (?, ?, ?, 'PENDING')";
    private static final String UPDATE_REVIEW = "UPDATE performance_reviews SET key_deliverables = ?, major_accomplishments = ?, areas_of_improvement = ?, self_rating = ?, updated_at = CURRENT_TIMESTAMP WHERE review_id = ? AND status = 'PENDING'";
    private static final String SUBMIT_REVIEW = "UPDATE performance_reviews SET status = 'SUBMITTED', submitted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE review_id = ? AND status = 'PENDING'";
    private static final String ADD_MANAGER_FEEDBACK = "UPDATE performance_reviews SET manager_rating = ?, manager_feedback = ?, manager_id = ?, status = 'REVIEWED', reviewed_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE review_id = ? AND status = 'SUBMITTED'";
    private static final String COMPLETE_REVIEW = "UPDATE performance_reviews SET status = 'COMPLETED', updated_at = CURRENT_TIMESTAMP WHERE review_id = ? AND status = 'REVIEWED'";
    private static final String SELECT_BASE = "SELECT pr.*, CONCAT(e.first_name, ' ', e.last_name) as employee_name, e.employee_code, CONCAT(m.first_name, ' ', m.last_name) as manager_name FROM performance_reviews pr JOIN employees e ON pr.employee_id = e.employee_id LEFT JOIN employees m ON pr.manager_id = m.employee_id ";

    @Override
    public int createReview(PerformanceReview review) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_REVIEW, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, review.getEmployeeId());
            stmt.setInt(2, review.getReviewYear());
            stmt.setString(3, review.getReviewPeriod());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Review creation failed : " + e.getMessage());
        }
    }

    @Override
    public boolean updateReview(PerformanceReview review) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_REVIEW)) {
            stmt.setString(1, review.getKeyDeliverables());
            stmt.setString(2, review.getMajorAccomplishments());
            stmt.setString(3, review.getAreasOfImprovement());
            stmt.setObject(4, review.getSelfRating());
            stmt.setInt(5, review.getReviewId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Update failed");
        }
    }

    @Override
    public boolean submitReview(int reviewId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SUBMIT_REVIEW)) {
            stmt.setInt(1, reviewId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Submission failed");
        }
    }

    @Override
    public boolean addManagerFeedback(int reviewId, int rating, String feedback, int managerId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(ADD_MANAGER_FEEDBACK)) {
            stmt.setInt(1, rating);
            stmt.setString(2, feedback);
            stmt.setInt(3, managerId);
            stmt.setInt(4, reviewId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Feedback update failed");
        }
    }

    @Override
    public boolean completeReview(int reviewId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COMPLETE_REVIEW)) {
            stmt.setInt(1, reviewId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Completion failed");
        }
    }

    @Override
    public Optional<PerformanceReview> findById(int reviewId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE pr.review_id = ?")) {
            stmt.setInt(1, reviewId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) { throw new AppException(ErrorCode.DATABASE_ERROR, "Fetch failed : " + e.getMessage()); }
        return Optional.empty();
    }

    @Override
    public List<PerformanceReview> findByEmployee(int empId) {
        List<PerformanceReview> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE pr.employee_id = ? ORDER BY pr.review_year DESC")) {
            stmt.setInt(1, empId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { throw new AppException(ErrorCode.DATABASE_ERROR, "Fetch failed : " + e.getMessage()); }
        return list;
    }

    @Override
    public Optional<PerformanceReview> findByEmployeeAndYear(int empId, int year) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE pr.employee_id = ? AND pr.review_year = ?")) {
            stmt.setInt(1, empId);
            stmt.setInt(2, year);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) { throw new AppException(ErrorCode.DATABASE_ERROR, "Fetch failed : " + e.getMessage()); }
        return Optional.empty();
    }

    @Override
    public List<PerformanceReview> findSubmittedByManager(int managerId) {
        List<PerformanceReview> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE e.manager_id = ? AND pr.status = 'SUBMITTED'")) {
            stmt.setInt(1, managerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { throw new AppException(ErrorCode.DATABASE_ERROR, "Fetch failed : " + e.getMessage()); }
        return list;
    }

    @Override
    public List<PerformanceReview> findTeamReviewsByManager(int managerId, int year) {
        List<PerformanceReview> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE e.manager_id = ? AND pr.review_year = ?")) {
            stmt.setInt(1, managerId);
            stmt.setInt(2, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { throw new AppException(ErrorCode.DATABASE_ERROR, "Fetch failed : " + e.getMessage()); }
        return list;
    }

    @Override
    public List<PerformanceReview> findAllByYear(int year) {
        List<PerformanceReview> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE pr.review_year = ?")) {
            stmt.setInt(1, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { throw new AppException(ErrorCode.DATABASE_ERROR, "Fetch failed : " + e.getMessage()); }
        return list;
    }

    @Override
    public boolean reviewExists(int empId, int year) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM performance_reviews WHERE employee_id = ? AND review_year = ?")) {
            stmt.setInt(1, empId);
            stmt.setInt(2, year);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { throw new AppException(ErrorCode.DATABASE_ERROR, "Check failed : " + e.getMessage()); }
        return false;
    }

    private PerformanceReview mapRow(ResultSet rs) throws SQLException {
        PerformanceReview review = new PerformanceReview();
        review.setReviewId(rs.getInt("review_id"));
        review.setEmployeeId(rs.getInt("employee_id"));
        review.setReviewYear(rs.getInt("review_year"));
        review.setReviewPeriod(rs.getString("review_period"));
        review.setKeyDeliverables(rs.getString("key_deliverables"));
        review.setMajorAccomplishments(rs.getString("major_accomplishments"));
        review.setAreasOfImprovement(rs.getString("areas_of_improvement"));
        review.setSelfRating(rs.getObject("self_rating", Integer.class));
        review.setManagerRating(rs.getObject("manager_rating", Integer.class));
        review.setManagerFeedback(rs.getString("manager_feedback"));
        review.setStatus(ReviewStatus.valueOf(rs.getString("status")));
        Timestamp sub = rs.getTimestamp("submitted_at");
        if (sub != null) review.setSubmittedAt(sub.toLocalDateTime());
        try {
            review.setEmployeeName(rs.getString("employee_name"));
            review.setEmployeeCode(rs.getString("employee_code"));
            review.setManagerName(rs.getString("manager_name"));
        } catch (SQLException ignored) {}
        return review;
    }
}

