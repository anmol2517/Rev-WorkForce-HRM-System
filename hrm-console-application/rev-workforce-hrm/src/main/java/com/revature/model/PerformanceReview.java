package com.revature.model;


import java.time.LocalDateTime;

public class PerformanceReview {
    private int reviewId;
    private int employeeId;
    private int reviewYear;
    private String keyDeliverables;
    private String majorAccomplishments;
    private String areasOfImprovement;
    private Integer selfRating;

    private Integer managerRating;
    private String managerFeedback;
    private Integer managerId;
    private ReviewStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String employeeName;
    private String employeeCode;
    private String managerName;

    public enum ReviewStatus {
        PENDING, SUBMITTED, FINALIZED, DRAFT, REVIEWED, COMPLETED
    }

    private String reviewPeriod;
    private Double finalRating;

    public void setReviewPeriod(String reviewPeriod) { this.reviewPeriod = reviewPeriod; }
    public void setFinalRating(Double finalRating) { this.finalRating = finalRating; }


    public PerformanceReview() {
        this.status = ReviewStatus.PENDING;
        this.reviewYear = java.time.Year.now().getValue();
    }

    public PerformanceReview(int employeeId, int reviewYear) {
        this();
        this.employeeId = employeeId;
        this.reviewYear = reviewYear;
    }

    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public int getReviewYear() { return reviewYear; }
    public void setReviewYear(int reviewYear) { this.reviewYear = reviewYear; }

    public String getKeyDeliverables() { return keyDeliverables; }
    public void setKeyDeliverables(String keyDeliverables) { this.keyDeliverables = keyDeliverables; }

    public String getMajorAccomplishments() { return majorAccomplishments; }
    public void setMajorAccomplishments(String majorAccomplishments) { this.majorAccomplishments = majorAccomplishments; }

    public String getAreasOfImprovement() { return areasOfImprovement; }
    public void setAreasOfImprovement(String areasOfImprovement) { this.areasOfImprovement = areasOfImprovement; }

    public String getReviewPeriod() { return reviewPeriod; }

    public String getSelfAssessment() { return majorAccomplishments; }
    public void setSelfAssessment(String assessment) { this.majorAccomplishments = assessment; }

    public Double getFinalRating() { return finalRating; }



    public Integer getSelfRating() { return selfRating; }
    public void setSelfRating(Integer selfRating) {
        if (selfRating != null && (selfRating < 1 || selfRating > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.selfRating = selfRating;
    }

    public Integer getManagerRating() { return managerRating; }
    public void setManagerRating(Integer managerRating) {
        if (managerRating != null && (managerRating < 1 || managerRating > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.managerRating = managerRating;
    }

    public String getManagerFeedback() { return managerFeedback; }
    public void setManagerFeedback(String managerFeedback) { this.managerFeedback = managerFeedback; }

    public Integer getManagerId() { return managerId; }
    public void setManagerId(Integer managerId) { this.managerId = managerId; }

    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public boolean isEditable() {
        return this.status == ReviewStatus.DRAFT;
    }

    public boolean canBeReviewed() {
        return this.status == ReviewStatus.SUBMITTED;
    }

    public String getRatingDisplay(Integer rating) {
        if (rating == null) return "Not Rated";
        String[] labels = {"Poor", "Below Average", "Average", "Good", "Excellent"};
        return rating + "/5 - " + labels[rating - 1];
    }

    @Override
    public String toString() {
        return String.format("Review[%d] Year : %d - Status : %s, Self : %s, Manager : %s",
            reviewId, reviewYear, status,
            getRatingDisplay(selfRating), getRatingDisplay(managerRating));
    }
}
