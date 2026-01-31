package com.revature.dao;

import com.revature.model.Goal;
import com.revature.model.Goal.Priority;
import com.revature.model.Goal.GoalStatus;
import com.revature.util.DBConnection;
import com.revature.exception.AppException;
import com.revature.exception.AppException.ErrorCode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GoalDAOImpl implements GoalDAO {

    private static final String ADD_MANAGER_GUIDANCE = "UPDATE goals SET manager_guidance = ?, updated_at = CURRENT_TIMESTAMP WHERE goal_id = ?";
    private static final String DELETE_GOAL = "DELETE FROM goals WHERE goal_id = ? AND status = 'NOT_STARTED'";
    private static final String SELECT_ALL_BY_YEAR = "SELECT g.*, CONCAT(e.first_name, ' ', e.last_name) as employee_name, e.employee_code FROM goals g JOIN employees e ON g.employee_id = e.employee_id WHERE g.goal_year = ? ORDER BY e.employee_code, g.priority";
    private static final String INSERT_GOAL = "INSERT INTO goals (employee_id, goal_year, goal_description, success_metrics, priority, deadline, status) VALUES (?, ?, ?, ?, ?, ?, 'NOT_STARTED')";
    private static final String UPDATE_GOAL = "UPDATE goals SET goal_description = ?, success_metrics = ?, priority = ?, deadline = ?, updated_at = CURRENT_TIMESTAMP WHERE goal_id = ?";
    private static final String UPDATE_PROGRESS = "UPDATE goals SET progress_percentage = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE goal_id = ?";
    private static final String SELECT_BY_ID = "SELECT g.*, CONCAT(e.first_name, ' ', e.last_name) as employee_name, e.employee_code FROM goals g JOIN employees e ON g.employee_id = e.employee_id WHERE g.goal_id = ?";
    private static final String SELECT_BY_EMPLOYEE = "SELECT g.*, CONCAT(e.first_name, ' ', e.last_name) as employee_name, e.employee_code FROM goals g JOIN employees e ON g.employee_id = e.employee_id WHERE g.employee_id = ? ORDER BY g.goal_year DESC, g.priority, g.deadline";

    @Override
    public int createGoal(Goal goal) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_GOAL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, goal.getEmployeeId());
            stmt.setInt(2, goal.getGoalYear());
            stmt.setString(3, goal.getGoalDescription());
            stmt.setString(4, goal.getSuccessMetrics());
            stmt.setString(5, goal.getPriority().name());
            stmt.setObject(6, goal.getDeadline());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Goal creation failed");
        }
    }

    @Override
    public boolean updateGoal(Goal goal) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_GOAL)) {
            stmt.setString(1, goal.getGoalDescription());
            stmt.setString(2, goal.getSuccessMetrics());
            stmt.setString(3, goal.getPriority().name());
            stmt.setObject(4, goal.getDeadline());
            stmt.setInt(5, goal.getGoalId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Update failed");
        }
    }

    @Override
    public boolean updateProgress(int goalId, int progress, GoalStatus status) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_PROGRESS)) {
            stmt.setInt(1, progress);
            stmt.setString(2, status.name());
            stmt.setInt(3, goalId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Progress update failed");
        }
    }

    @Override
    public Optional<Goal> findById(int goalId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setInt(1, goalId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToGoal(rs));
            }
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Fetch by ID failed");
        }
        return Optional.empty();
    }

    @Override
    public List<Goal> findByEmployee(int employeeId) {
        List<Goal> goals = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMPLOYEE)) {
            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) goals.add(mapResultSetToGoal(rs));
            }
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Fetch by employee failed");
        }
        return goals;
    }

    private Goal mapResultSetToGoal(ResultSet rs) throws SQLException {
        Goal goal = new Goal();
        goal.setGoalId(rs.getInt("goal_id"));
        goal.setEmployeeId(rs.getInt("employee_id"));
        goal.setGoalYear(rs.getInt("goal_year"));
        goal.setGoalDescription(rs.getString("goal_description"));
        goal.setPriority(Priority.valueOf(rs.getString("priority")));
        goal.setStatus(GoalStatus.valueOf(rs.getString("status")));
        Date deadline = rs.getDate("deadline");
        if (deadline != null) goal.setDeadline(deadline.toLocalDate());
        try { goal.setEmployeeName(rs.getString("employee_name")); } catch (SQLException e) {}
        return goal;
    }

    @Override
    public boolean addManagerGuidance(int goalId, String guidance) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(ADD_MANAGER_GUIDANCE)) {
            stmt.setString(1, guidance);
            stmt.setInt(2, goalId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Error adding guidance");
        }
    }

    @Override
    public boolean deleteGoal(int goalId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_GOAL)) {
            stmt.setInt(1, goalId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Error deleting goal");
        }
    }

    @Override
    public List<Goal> findByEmployeeAndYear(int employeeId, int year) {
        return new ArrayList<>();
    }

    @Override
    public List<Goal> findTeamGoalsByManager(int managerId, int year) {
        return new ArrayList<>();
    }

    @Override
    public List<Goal> findAllByYear(int year) {
        List<Goal> goals = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_BY_YEAR)) {
            stmt.setInt(1, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    goals.add(mapResultSetToGoal(rs));
                }
            }
        } catch (SQLException e) {
            throw new AppException(ErrorCode.DATABASE_ERROR, "Error fetching goals for year : " + year);
        }
        return goals;
    }

}

