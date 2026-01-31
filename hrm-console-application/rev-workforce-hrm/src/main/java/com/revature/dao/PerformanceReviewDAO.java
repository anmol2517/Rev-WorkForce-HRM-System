package com.revature.dao;

import com.revature.model.PerformanceReview;
import java.util.List;
import java.util.Optional;

public interface PerformanceReviewDAO {
    int createReview(PerformanceReview review);
    boolean updateReview(PerformanceReview review);
    boolean submitReview(int reviewId);
    boolean addManagerFeedback(int reviewId, int managerRating, String feedback, int managerId);
    boolean completeReview(int reviewId);

    Optional<PerformanceReview> findById(int reviewId);
    List<PerformanceReview> findByEmployee(int employeeId);
    Optional<PerformanceReview> findByEmployeeAndYear(int employeeId, int year);
    List<PerformanceReview> findSubmittedByManager(int managerId);
    List<PerformanceReview> findTeamReviewsByManager(int managerId, int year);
    List<PerformanceReview> findAllByYear(int year);
    boolean reviewExists(int employeeId, int year);
}

