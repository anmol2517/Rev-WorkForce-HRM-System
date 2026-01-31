package com.revature.util;

//  Application constants

public final class Constants {
    
    private Constants() {
    }


    //   ROLES

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_EMPLOYEE = "EMPLOYEE";

    //   EMPLOYEE STATUS

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_ON_LEAVE = "ON_LEAVE";
    public static final String STATUS_TERMINATED = "TERMINATED";

    //    LEAVE STATUS

    public static final String LEAVE_PENDING = "PENDING";
    public static final String LEAVE_APPROVED = "APPROVED";
    public static final String LEAVE_REJECTED = "REJECTED";
    public static final String LEAVE_CANCELLED = "CANCELLED";

    //    LEAVE TYPES

    public static final String LEAVE_TYPE_ANNUAL = "Annual Leave";
    public static final String LEAVE_TYPE_SICK = "Sick Leave";
    public static final String LEAVE_TYPE_CASUAL = "Casual Leave";
    public static final String LEAVE_TYPE_MATERNITY = "Maternity Leave";
    public static final String LEAVE_TYPE_PATERNITY = "Paternity Leave";
    public static final String LEAVE_TYPE_UNPAID = "Unpaid Leave";

    //    PERFORMANCE REVIEW STATUS

    public static final String REVIEW_DRAFT = "DRAFT";
    public static final String REVIEW_SUBMITTED = "SUBMITTED";
    public static final String REVIEW_REVIEWED = "REVIEWED";
    public static final String REVIEW_COMPLETED = "COMPLETED";

    //   GOAL STATUS

    public static final String GOAL_NOT_STARTED = "NOT_STARTED";
    public static final String GOAL_IN_PROGRESS = "IN_PROGRESS";
    public static final String GOAL_COMPLETED = "COMPLETED";
    public static final String GOAL_CANCELLED = "CANCELLED";

    //   NOTIFICATION TYPES

    public static final String NOTIFICATION_LEAVE_REQUEST = "LEAVE_REQUEST";
    public static final String NOTIFICATION_LEAVE_APPROVED = "LEAVE_APPROVED";
    public static final String NOTIFICATION_LEAVE_REJECTED = "LEAVE_REJECTED";
    public static final String NOTIFICATION_PERFORMANCE_REVIEW = "PERFORMANCE_REVIEW";
    public static final String NOTIFICATION_GOAL_ASSIGNED = "GOAL_ASSIGNED";
    public static final String NOTIFICATION_ANNOUNCEMENT = "ANNOUNCEMENT";
    public static final String NOTIFICATION_SYSTEM = "SYSTEM";

    //    AUDIT ACTION TYPES

    public static final String AUDIT_LOGIN = "LOGIN";
    public static final String AUDIT_LOGOUT = "LOGOUT";
    public static final String AUDIT_CREATE = "CREATE";
    public static final String AUDIT_UPDATE = "UPDATE";
    public static final String AUDIT_DELETE = "DELETE";
    public static final String AUDIT_APPROVE = "APPROVE";
    public static final String AUDIT_REJECT = "REJECT";
    public static final String AUDIT_VIEW = "VIEW";

    //    VALIDATION

    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 50;
    public static final int MIN_USERNAME_LENGTH = 4;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 100;
    public static final int MAX_PHONE_LENGTH = 20;
    public static final int MAX_ADDRESS_LENGTH = 500;
    public static final int MAX_DESCRIPTION_LENGTH = 1000;
    public static final int MAX_COMMENT_LENGTH = 2000;

    //   DEFAULT VALUES

    public static final int DEFAULT_ANNUAL_LEAVE_BALANCE = 20;
    public static final int DEFAULT_SICK_LEAVE_BALANCE = 10;
    public static final int DEFAULT_CASUAL_LEAVE_BALANCE = 5;
    public static final int DEFAULT_PAGE_SIZE = 10;

    //   DATE FORMATS

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd | HH:mm:ss";
    public static final String DISPLAY_DATE_FORMAT = "dd-MMM-yyyy";
    
    //   PERFORMANCE RATINGS

    public static final int RATING_MIN = 1;
    public static final int RATING_MAX = 5;
    public static final String RATING_POOR = "Poor";
    public static final String RATING_BELOW_AVERAGE = "Below Average";
    public static final String RATING_AVERAGE = "Average";
    public static final String RATING_GOOD = "Good";
    public static final String RATING_EXCELLENT = "Excellent";

    public static String getRatingDescription(int rating) {
        switch (rating) {
            case 1: return RATING_POOR;
            case 2: return RATING_BELOW_AVERAGE;
            case 3: return RATING_AVERAGE;
            case 4: return RATING_GOOD;
            case 5: return RATING_EXCELLENT;
            default: return "Unknown";
        }
    }

    public static boolean isValidRole(String role) {
        if (role == null) return false;
        String upperRole = role.toUpperCase();
        return ROLE_ADMIN.equals(upperRole) ||
                ROLE_MANAGER.equals(upperRole) ||
                ROLE_EMPLOYEE.equals(upperRole);
    }

    public static boolean isValidEmployeeStatus(String status) {
        if (status == null) return false;
        String upperStatus = status.toUpperCase();
        return STATUS_ACTIVE.equals(upperStatus) || 
               STATUS_INACTIVE.equals(upperStatus) || 
               STATUS_ON_LEAVE.equals(upperStatus) || 
               STATUS_TERMINATED.equals(upperStatus);
    }

    public static boolean isValidLeaveStatus(String status) {
        if (status == null) return false;
        String upperStatus = status.toUpperCase();
        return LEAVE_PENDING.equals(upperStatus) || 
               LEAVE_APPROVED.equals(upperStatus) || 
               LEAVE_REJECTED.equals(upperStatus) || 
               LEAVE_CANCELLED.equals(upperStatus);
    }

    public static boolean isValidGoalStatus(String status) {
        if (status == null) return false;
        String upperStatus = status.toUpperCase();
        return GOAL_NOT_STARTED.equals(upperStatus) || 
               GOAL_IN_PROGRESS.equals(upperStatus) || 
               GOAL_COMPLETED.equals(upperStatus) || 
               GOAL_CANCELLED.equals(upperStatus);
    }
}


