package com.revature.exception;

//  --  Custom Application Exception - Base exception for all application errors

public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public enum ErrorCode {


//         General & Base Errors (0xx)

        INVALID_INPUT(001, "Invalid input provided"),
        UNAUTHORIZED(002, "Unauthorized access"),
        OPERATION_FAILED(003, "Operation failed. Please try again."),
        NOT_FOUND(102, "Resource Not Found"),


//          Authentication Errors (1xx)

        AUTH_INVALID_CREDENTIALS(111, "Invalid email/employee ID or password"),
        AUTH_ACCOUNT_INACTIVE(112, "Account is inactive. Please contact admin."),
        AUTH_ACCOUNT_DELETED(113, "Account has been deleted"),
        AUTH_SESSION_EXPIRED(114, "Session expired. Please login again."),
        AUTH_PASSWORD_MISMATCH(115, "Current password is incorrect"),
        AUTH_WEAK_PASSWORD(116, "Password must be at least 8 characters"),
        AUTH_SECURITY_ANSWER_WRONG(117, "Security answer is incorrect"),
        INVALID_CREDENTIALS(118, "Invalid Username or Password"),


//         Employee Errors (2xx)

        EMPLOYEE_NOT_FOUND(201, "Employee not found"),
        EMPLOYEE_ALREADY_EXISTS(202, "Employee with this email already exists"),
        EMPLOYEE_CODE_EXISTS(203, "Employee code already exists"),
        EMPLOYEE_INVALID_DATA(204, "Invalid employee data provided"),
        EMPLOYEE_CANNOT_DELETE_SELF(205, "Cannot delete your own account"),
        EMPLOYEE_INVALID_MANAGER(206, "Invalid manager assignment"),


//         Leave Errors (3xx)

        LEAVE_INSUFFICIENT_BALANCE(301, "Insufficient leave balance"),
        LEAVE_REQUEST_NOT_FOUND(302, "Leave request not found"),
        LEAVE_INVALID_DATES(303, "Invalid leave dates. End date must be after start date."),
        LEAVE_OVERLAP(304, "Leave dates overlap with existing application"),
        LEAVE_PAST_DATE(305, "Cannot apply for leave on past dates"),
        LEAVE_CANNOT_CANCEL(306, "Only pending leave requests can be cancelled"),
        LEAVE_ALREADY_PROCESSED(307, "Leave request has already been processed"),
        LEAVE_TYPE_NOT_FOUND(308, "Leave type not found"),
        LEAVE_UNAUTHORIZED_ACTION(309, "Not authorized to perform this action"),


//         Performance Errors (4xx)

        REVIEW_NOT_FOUND(401, "Performance review not found"),
        REVIEW_ALREADY_EXISTS(402, "Performance review for this year already exists"),
        REVIEW_NOT_EDITABLE(403, "Performance review is not editable in current status"),
        REVIEW_CANNOT_SUBMIT(404, "Cannot submit review. Please complete all required fields."),
        REVIEW_NOT_REVIEWABLE(405, "Review is not in submitted status"),
        GOAL_NOT_FOUND(406, "Goal not found"),
        GOAL_INVALID_PROGRESS(407, "Progress must be between 0 and 100"),


//         Notification Errors (5xx)

        NOTIFICATION_NOT_FOUND(501, "Notification not found"),


//         Database Errors (9xx)

        DATABASE_ERROR(901, "Database operation failed"),
        TRANSACTION_FAILED(902, "Transaction failed and was rolled back"),
        CONNECTION_FAILED(903, "Could not connect to database");

        private final int code;
        private final String defaultMessage;

        ErrorCode(int code, String defaultMessage) {
            this.code = code;
            this.defaultMessage = defaultMessage;
        }

        public int getCode() { return code; }
        public String getDefaultMessage() { return defaultMessage; }
    }


    public AppException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getCode() {
        return errorCode.getCode();
    }

    @Override
    public String toString() {
        return String.format("Error [%d]: %s", errorCode.getCode(), getMessage());
    }
}


