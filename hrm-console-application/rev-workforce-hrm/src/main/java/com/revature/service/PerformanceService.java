package com.revature.service;

import com.revature.dao.*;
import com.revature.exception.AppException;
import com.revature.model.*;
import com.revature.util.DBConnection;
import com.revature.util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


//  Service class for Performance Management operations.
//  Handles self-assessments, goal setting, manager reviews, and ratings.

public class PerformanceService {

    private final PerformanceReviewDAO reviewDAO;
    private final GoalDAO goalDAO;

    private final EmployeeDAO employeeDAO;
    private final AuditLogDAO auditLogDAO;
    private final NotificationService notificationService;
    private final AuthService authService;

    public PerformanceService(AuthService authService, NotificationService notificationService) {
        this.reviewDAO = new PerformanceReviewDAOImpl();
        this.goalDAO = new GoalDAOImpl();
        this.employeeDAO = new EmployeeDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
        this.notificationService = notificationService;
        this.authService = authService;
    }


    // ==================== SELF ASSESSMENT ====================


    public PerformanceReview submitSelfAssessment(int reviewId, String selfAssessment,
                                                  Integer selfRating) throws AppException {
        if (ValidationUtil.isNullOrEmpty(selfAssessment)) {
            throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Self-assessment content is required");
        }
        if (selfRating == null || selfRating < 1 || selfRating > 5) {
            throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Self-rating must be between 1 and 5");
        }

        try {
            PerformanceReview review = reviewDAO.findById(reviewId)
                    .orElseThrow(() -> new AppException(AppException.ErrorCode.INVALID_INPUT, "Review not found"));

            Employee currentUser = authService.getLoggedInUser();

            if (review.getEmployeeId() != currentUser.getEmployeeId()) {
                throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "You can only submit your own self-assessment");
            }

            if (review.getMajorAccomplishments() != null && !review.getMajorAccomplishments().isEmpty()) {
                throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Self-assessment already submitted");
            }

            review.setMajorAccomplishments(selfAssessment);
            review.setSelfRating(selfRating);
            review.setStatus(PerformanceReview.ReviewStatus.SUBMITTED);
            review.setUpdatedAt(LocalDateTime.now());

            boolean success = reviewDAO.updateReview(review);
            if (!success) {
                throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to update review record");
            }

            if (currentUser.getManagerId() != null) {
                notificationService.sendNotification(currentUser.getManagerId(),
                        "Self-Assessment Submitted",
                        currentUser.getFirstName() + " " + currentUser.getLastName() +
                                " has submitted their self-assessment.",
                        "PERFORMANCE");
            }

            logAudit("UPDATE", "performance_reviews", reviewId, "self_assessment", "Self-assessment submitted");

            return review;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to submit : " + e.getMessage());
        }
    }


    public PerformanceReview getMyCurrentReview() throws AppException {
        try {
            int currentYear = LocalDate.now().getYear();
            String currentPeriod = getCurrentReviewPeriod();

            PerformanceReview review = reviewDAO.findByEmployeeAndYear(
                            authService.getLoggedInUser().getEmployeeId(), currentYear)
                    .stream()
                    .filter(r -> r.getReviewPeriod().equalsIgnoreCase(currentPeriod))
                    .findFirst()
                    .orElse(null);

            if (review == null) {
                review = new PerformanceReview();
                review.setEmployeeId(authService.getLoggedInUser().getEmployeeId());
                review.setReviewYear(currentYear);
                review.setReviewPeriod(currentPeriod);

                int generatedId = reviewDAO.createReview(review);
                review.setReviewId(generatedId);
            }

            return review;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to fetch review : " + e.getMessage());
        }
    }

    public List<PerformanceReview> getMyReviewHistory() throws AppException {
        try {
            return reviewDAO.findByEmployee(authService.getLoggedInUser().getEmployeeId());
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to fetch review history");
        }
    }


// ==================== GOAL MANAGEMENT ====================


    public Goal createGoal(Goal goal) throws AppException {
        Employee currentUser = authService.getLoggedInUser();
        if (currentUser == null) throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Please login");

        validateGoal(goal);

        if (!authService.isAdmin() && !authService.isManager()) {
            goal.setEmployeeId(currentUser.getEmployeeId());
        }

        if (authService.isManager() && goal.getEmployeeId() != currentUser.getEmployeeId()) {
            try {
                Employee employee = employeeDAO.findById(goal.getEmployeeId())
                        .orElseThrow(() -> new AppException(AppException.ErrorCode.INVALID_INPUT, "Employee not found"));

                if (employee.getManagerId() == null || employee.getManagerId() != currentUser.getEmployeeId()) {
                    throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Manager mismatch : Can't assign goal");
                }
            } catch (AppException e) { throw e;
            } catch (Exception e) {
                throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Verification failed");
            }
        }

        try {
            goal.setStatus(Goal.GoalStatus.NOT_STARTED);
            goal.setCreatedAt(LocalDateTime.now());

            int generatedId = goalDAO.createGoal(goal);
            goal.setGoalId(generatedId);
            Goal created = goal;

            if (goal.getEmployeeId() != currentUser.getEmployeeId()) {
                notificationService.sendNotification(goal.getEmployeeId(),
                        "New Goal Assigned", "Title : " + goal.getTitle(), "PERFORMANCE");
            }

            logAudit("INSERT", "GOALS", created.getGoalId(), "title", "Goal created : " + goal.getGoalDescription());

            return created;

        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Goal creation failed : " + e.getMessage());
        }
    }


    public Goal updateGoalProgress(int goalId, int progressPercentage, String notes)
            throws AppException {
        if (progressPercentage < 0 || progressPercentage > 100) {
            throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Progress must be between 0 and 100");
        }

        try {
            Goal goal = goalDAO.findById(goalId)
                    .orElseThrow(() -> new AppException(AppException.ErrorCode.INVALID_INPUT, "Goal not found"));
            if (goal.getEmployeeId() != authService.getLoggedInUser().getEmployeeId() &&
                    !authService.isAdmin() && !isManagerOf(goal.getEmployeeId())) {
                throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Access denied");
            }

            goal.setProgressPercentage(progressPercentage);
            if (!ValidationUtil.isNullOrEmpty(notes)) {
                goal.setManagerGuidance(notes);
            }

            if (progressPercentage == 100) {
                goal.setStatus(Goal.GoalStatus.COMPLETED);
            } else if (progressPercentage > 0) {
                goal.setStatus(Goal.GoalStatus.IN_PROGRESS);
            }

            goal.setUpdatedAt(LocalDateTime.now());
            goalDAO.updateGoal(goal);
            logAudit("UPDATE", "GOALS", goalId,
                    "progress_percentage", "Goal progress updated to " + progressPercentage + "%");

            return goal;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to update goal : " + e.getMessage());
        }
    }


    public List<Goal> getMyGoals() throws AppException {
        try {
            return goalDAO.findByEmployee(authService.getLoggedInUser().getEmployeeId());

        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to fetch goals : " + e.getMessage());
        }
    }

    public List<Goal> getMyGoalsByStatus(String status) throws AppException {
        try {
            int employeeId = authService.getLoggedInUser().getEmployeeId();
            List<Goal> allGoals = goalDAO.findByEmployee(employeeId);
            return allGoals.stream()
                    .filter(g -> g.getStatus() != null && g.getStatus().name().equalsIgnoreCase(status))
                    .toList();
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to fetch goals by status : " + e.getMessage());
        }
    }


    // ==================== MANAGER FEEDBACK ====================


    public PerformanceReview submitManagerFeedback(int reviewId, String managerFeedback,
                                                   Integer managerRating) throws AppException {
        if (!authService.isManager() && !authService.isAdmin()) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Only managers can submit feedback");
        }

        if (ValidationUtil.isNullOrEmpty(managerFeedback)) {
            throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Manager feedback is required");
        }
        if (managerRating == null || managerRating < 1 || managerRating > 5) {
            throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Rating must be 1-5");
        }

        try {
            PerformanceReview review = reviewDAO.findById(reviewId)
                    .orElseThrow(() -> new AppException(AppException.ErrorCode.INVALID_INPUT, "Review not found"));

            if (!authService.isAdmin()) {
                Employee employee = employeeDAO.findById(review.getEmployeeId())
                        .orElseThrow(() -> new AppException(AppException.ErrorCode.INVALID_INPUT, "Employee not found"));

                if (employee.getManagerId() == null ||
                        !employee.getManagerId().equals(authService.getLoggedInUser().getEmployeeId())) {
                    throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "You are not authorized to review this employee");
                }
            }

            review.setManagerFeedback(managerFeedback);
            review.setManagerRating(managerRating);
            review.setReviewedAt(LocalDateTime.now());

            review.setStatus(PerformanceReview.ReviewStatus.FINALIZED);
            review.setUpdatedAt(LocalDateTime.now());

            if (review.getSelfRating() != null) {
                double avg = (review.getSelfRating() + managerRating) / 2.0;
                review.setFinalRating(avg);
            } else {
                review.setFinalRating((double) managerRating);
            }

            boolean success = reviewDAO.updateReview(review);
            if (!success) throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Update failed");

            notificationService.sendNotification(review.getEmployeeId(),
                    "Performance Review Completed",
                    "Your manager has completed your review. Final Rating : " + review.getFinalRating(),
                    "PERFORMANCE");

            logAudit("UPDATE", "PERFORMANCE", reviewId, "manager_feedback",
                    "Manager feedback submitted. Rating : " + managerRating);

            return review;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to submit feedback : " + e.getMessage());
        }
    }

    public List<PerformanceReview> getPendingReviewsForManager() throws AppException {
        if (!authService.isManager() && !authService.isAdmin()) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Access denied : Managers or Admins only");
        }

        try {
            int currentYear = LocalDate.now().getYear();
            List<PerformanceReview> allReviews = reviewDAO.findAllByYear(currentYear);

            if (authService.isAdmin()) {
                return allReviews.stream()
                        .filter(r -> r.getStatus() == PerformanceReview.ReviewStatus.SUBMITTED)
                        .toList();
            } else {
                int managerId = authService.getLoggedInUser().getEmployeeId();

                return allReviews.stream()
                        .filter(r -> r.getStatus() == PerformanceReview.ReviewStatus.SUBMITTED)
                        .filter(r -> {
                            try {
                                return employeeDAO.findById(r.getEmployeeId())
                                        .map(emp -> emp.getManagerId() != null && emp.getManagerId() == managerId)
                                        .orElse(false);
                            } catch (SQLException e) {
                                return false;
                            }
                        })
                        .toList();
            }
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to fetch pending reviews : " + e.getMessage());
        }
    }

    public List<Goal> getTeamGoals() throws AppException {
        if (!authService.isManager() && !authService.isAdmin()) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Access denied : Managers or Admins only");
        }

        try {
            if (authService.isAdmin()) {
                return goalDAO.findAllByYear(LocalDate.now().getYear());
            } else {
                int managerId = authService.getLoggedInUser().getEmployeeId();
                return goalDAO.findTeamGoalsByManager(managerId, LocalDate.now().getYear());
            }
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to fetch team goals : " + e.getMessage());
        }
    }


    public PerformanceReview getEmployeeReview(int employeeId, int year, String period)
            throws AppException {
        if (!authService.isAdmin() && !isManagerOf(employeeId)) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED,
                    "Access denied: Only Admin or Manager can view this review");
        }

        try {
            return reviewDAO.findByEmployeeAndYear(employeeId, year)
                    .filter(r -> r.getReviewPeriod().equalsIgnoreCase(period))
                    .orElseThrow(() -> new AppException(AppException.ErrorCode.INVALID_INPUT,
                            "No review found for this employee in the specified period/year"));
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR,
                    "Database error while fetching review : " + e.getMessage());
        }
    }

    public List<Goal> getEmployeeGoals(int employeeId) throws AppException {
        Employee currentUser = authService.getLoggedInUser();
        if (currentUser.getEmployeeId() != employeeId &&
                !authService.isAdmin() && !isManagerOf(employeeId)) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Access denied : You don't have permission to view these goals");
        }

        try {
            return goalDAO.findByEmployee(employeeId);
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to fetch goals : " + e.getMessage());
        }
    }


    // ==================== ADMIN FUNCTIONS ====================


    public void initiateReviewCycle(int year, String period) throws AppException {
        if (!authService.isAdmin()) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Only Admin can initiate review cycles");
        }

        try {
            List<Employee> employees = employeeDAO.findAll().stream()
                    .filter(Employee::isActive)
                    .toList();

            if (employees.isEmpty()) {
                throw new AppException(AppException.ErrorCode.INVALID_INPUT, "No active employees found");
            }

            int created = 0;
            for (Employee employee : employees) {
                boolean exists = reviewDAO.findByEmployeeAndYear(employee.getEmployeeId(), year)
                        .stream()
                        .anyMatch(r -> period.equalsIgnoreCase(r.getReviewPeriod()));

                if (!exists) {
                    PerformanceReview review = new PerformanceReview();
                    review.setEmployeeId(employee.getEmployeeId());
                    review.setReviewYear(year);
                    review.setReviewPeriod(period);
                    review.setStatus(PerformanceReview.ReviewStatus.PENDING);
                    review.setCreatedAt(LocalDateTime.now());

                    reviewDAO.createReview(review);
                    created++;

                    try {
                        notificationService.sendNotification(employee.getEmployeeId(),
                                "Performance Review Cycle Started",
                                "A new review cycle initiated for " + period + " " + year,
                                "PERFORMANCE");
                    } catch (Exception ignored) {}
                }
            }

            logAudit("INSERT", "PERFORMANCE", 0, "SYSTEM",
                    "Review cycle initiated for " + period + " " + year + ". Created " + created + " reviews.");

        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Review creation failed: " + e.getMessage());
        }
    }

    public List<PerformanceReview> getAllReviews(int year) throws AppException {
        if (!authService.isAdmin()) {
            throw new AppException(AppException.ErrorCode.UNAUTHORIZED, "Access denied: Admin privileges required");
        }

        try {
            return reviewDAO.findAllByYear(year);
        } catch (Exception e) {
            throw new AppException(AppException.ErrorCode.DATABASE_ERROR, "Failed to fetch reviews for year " + year + " : " + e.getMessage());
        }
    }


    // ==================== HELPER METHODS ====================


    private PerformanceReview createReviewForPeriod(int employeeId, int year, String period)
            throws SQLException {
        PerformanceReview review = new PerformanceReview();
        review.setEmployeeId(employeeId);
        review.setReviewYear(year);
        review.setReviewPeriod(period);
        review.setStatus(PerformanceReview.ReviewStatus.PENDING);

        review.setCreatedAt(LocalDateTime.now());
        int generatedId = reviewDAO.createReview(review);
        review.setReviewId(generatedId);

        return review;
    }

    private boolean isManagerOf(int employeeId) {
        try {
            return employeeDAO.findById(employeeId)
                    .map(emp -> {
                        Integer managerId = emp.getManagerId();
                        Employee loggedInUser = authService.getLoggedInUser();

                        return managerId != null && loggedInUser != null &&
                                managerId.equals(loggedInUser.getEmployeeId());
                    })
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    private String getCurrentReviewPeriod() {
        int month = LocalDate.now().getMonthValue();
        if (month <= 6) {
            return "H1";
        } else {
            return "H2";
        }
    }

    private void validateGoal(Goal goal) throws AppException {
        if (ValidationUtil.isNullOrEmpty(goal.getTitle())) {
            throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Goal title is required");
        }

        if (goal.getTargetDate() == null) {
            throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Target date is required");
        }

        if (goal.getTargetDate().isBefore(LocalDate.now())) {
            throw new AppException(AppException.ErrorCode.INVALID_INPUT, "Target date cannot be in the past");
        }
    }

    private void logAudit(String action, String tableName, Integer recordId,
                          String columnName, String description) {
        try {
            AuditLog log = new AuditLog();
            log.setActionType(AuditLog.ActionType.valueOf(action.toUpperCase()));
            log.setEntityType(AuditLog.EntityType.valueOf(tableName.toUpperCase()));
            log.setEntityId(recordId);
            log.setNewValue(columnName + " : " + description);
            Employee currentUser = authService.getLoggedInUser();
            log.setEmployeeId(currentUser != null ? currentUser.getEmployeeId() : null);
            log.setActionTimestamp(LocalDateTime.now());
            auditLogDAO.logAudit(log);
        } catch (Exception e) {
            System.err.println("Failed to log audit : " + e.getMessage());
        }
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                System.err.println("Rollback failed : " + e.getMessage());
            }
        }
    }

    private void resetAutoCommit(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Failed to reset auto-commit : " + e.getMessage());
            }
        }
    }
}


