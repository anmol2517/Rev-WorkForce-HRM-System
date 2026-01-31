package com.revature.ui;

import com.revature.exception.AppException;
import com.revature.model.*;
import com.revature.service.*;
import com.revature.util.ConsoleUtil;

import java.util.List;


//   Employee Console UI  ||  Provides access to self-service features for regular employees.

public class EmployeeUI {

    private final AuthService authService;
    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final PerformanceService performanceService;
    private final NotificationService notificationService;
    private final NotificationUI notificationUI;


    public EmployeeUI(AuthService authService, EmployeeService employeeService,
                      LeaveService leaveService, PerformanceService performanceService,
                      NotificationService notificationService, NotificationUI notificationUI) {
        this.authService = authService;
        this.employeeService = employeeService;
        this.leaveService = leaveService;
        this.performanceService = performanceService;
        this.notificationService = notificationService;
        this.notificationUI = notificationUI;
    }


    public void showMenu() {
        while (true) {
            ConsoleUtil.clearScreen();
            displayHeader();

            String[] options = {
                    "Leave Management",
                    "Performance & Goals",
                    "Employee Directory",
                    "Notifications",
                    "My Profile",
                    "Update My Details",
                    "Change Password",
                    "Logout"
            };

            ConsoleUtil.printMenu(options);
            int choice = ConsoleUtil.readMenuChoice(9);

            switch (choice) {
                case 1: leaveMenu(); break;
                case 2: performanceMenu(); break;
                case 3: employeeDirectory(); break;
                case 4: notificationUI.handleMenu(); break;
                case 5: viewMyProfile(); break;
                case 6: updateMyDetails(); break;
                case 7: changePassword(); break;
                case 8:
                case 0: return;
                default: ConsoleUtil.printError("Invalid option");
            }
        }
    }

    private void displayHeader() {
        Employee user = authService.getLoggedInUser();
        ConsoleUtil.printHeader("EMPLOYEE PORTAL");
        System.out.println("  Welcome, " + user.getFirstName() + " " + user.getLastName());
        System.out.println("  Role: EMPLOYEE");

        try {
            int unread = notificationService.getUnreadCount();
            if (unread > 0) {
                ConsoleUtil.printWarning("You have " + unread + " unread notification(s)");
            }
        } catch (Exception e) {  }
        System.out.println();
    }

    //   LEAVE MANAGEMENT

    private void leaveMenu() {
        while (true) {
            ConsoleUtil.printSubHeader("Leave Management");

            String[] options = {
                    "View My Leave Balance",
                    "Apply for Leave",
                    "View My Leave History",
                    "Cancel Leave Request",
                    "View Holiday Calendar"
            };

            ConsoleUtil.printMenu(options);
            int choice = ConsoleUtil.readMenuChoice( 5);

            switch (choice) {
                case 1: viewMyLeaveBalance(); break;
                case 2: applyForLeave(); break;
                case 3: viewMyLeaveHistory(); break;
                case 4: cancelLeaveRequest(); break;
                case 5: viewHolidays(); break;
                case 0: return;
            }
        }
    }

    private void viewMyLeaveBalance() {
        ConsoleUtil.printSubHeader("My Leave Balance");

        try {
            List<LeaveBalance> balances = leaveService.getMyLeaveBalances();
            List<LeaveType> types = leaveService.getAllLeaveTypes();

            System.out.printf("%-20s %-10s %-10s %-10s%n", "Leave Type", "Total", "Used", "Remaining");
            ConsoleUtil.printLine();

            for (LeaveBalance balance : balances) {
                String typeName = types.stream()
                        .filter(t -> t.getLeaveTypeId() == balance.getLeaveTypeId())
                        .map(LeaveType::getTypeName)
                        .findFirst()
                        .orElse("Type #" + balance.getLeaveTypeId());

                System.out.printf("%-20s %-10d %-10d %-10d%n",
                        typeName,
                        balance.getTotalDays(),
                        balance.getUsedDays(),
                        balance.getRemainingDays());
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void applyForLeave() {
        ConsoleUtil.printSubHeader("Apply for Leave");

        try {
            List<LeaveType> types = leaveService.getAllLeaveTypes();
            List<LeaveBalance> balances = leaveService.getMyLeaveBalances();

            System.out.println("Available Leave Types:");
            for (int i = 0; i < types.size(); i++) {
                LeaveType type = types.get(i);
                int remaining = balances.stream()
                        .filter(b -> b.getLeaveTypeId() == type.getLeaveTypeId())
                        .mapToInt(LeaveBalance::getRemainingDays)
                        .findFirst()
                        .orElse(0);

                System.out.println("  " + (i + 1) + ". " + type.getTypeName() +
                        " (Remaining: " + remaining + " days)");
            }

            int typeChoice = ConsoleUtil.readIntInRange("Select Leave Type", 1, types.size());

            LeaveRequest request = new LeaveRequest();
            request.setLeaveTypeId(types.get(typeChoice - 1).getLeaveTypeId());
            request.setStartDate(ConsoleUtil.readFutureDate("Start Date"));
            request.setEndDate(ConsoleUtil.readFutureDate("End Date"));

            if (request.getEndDate().isBefore(request.getStartDate())) {
                ConsoleUtil.printError("End date cannot be before start date.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            request.setReason(ConsoleUtil.readRequiredString("Reason for Leave"));

            int workingDays = leaveService.calculateWorkingDays(
                    request.getStartDate(), request.getEndDate());

            System.out.println("\n--- Leave Request Summary ---");
            System.out.println("Leave Type : " + types.get(typeChoice - 1).getTypeName());
            System.out.println("From : " + request.getStartDate() + " To : " + request.getEndDate());
            System.out.println("Working Days : " + workingDays);
            System.out.println("Reason : " + request.getReason());

            if (ConsoleUtil.confirm("\nSubmit this leave request?")) {
                LeaveRequest created = leaveService.applyLeave(request);
                ConsoleUtil.printSuccess("Leave request submitted successfully!");
                ConsoleUtil.printInfo("Request ID : " + created.getLeaveRequestId());
                ConsoleUtil.printInfo("Status : PENDING - Awaiting manager approval");
            } else {
                ConsoleUtil.printInfo("Leave request cancelled.");
            }

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void viewMyLeaveHistory() {
        ConsoleUtil.printSubHeader("My Leave History");

        try {
            List<LeaveRequest> requests = leaveService.getMyLeaveHistory();

            if (requests.isEmpty()) {
                ConsoleUtil.printInfo("No leave history found.");
            } else {
                System.out.printf("%-5s %-12s %-12s %-5s %-12s %-20s%n",
                        "ID", "From", "To", "Days", "Status", "Reason");
                ConsoleUtil.printLine();

                for (LeaveRequest req : requests) {
                    System.out.printf("%-5d %-12s %-12s %-5d %-12s %-20s%n",
                            req.getRequestId(),
                            ConsoleUtil.formatDate(req.getStartDate()),
                            ConsoleUtil.formatDate(req.getEndDate()),
                            req.getTotalDays(),
                            ConsoleUtil.formatStatus(req.getStatus().name()),
                            req.getReason() != null ?
                                    (req.getReason().length() > 20 ?
                                            req.getReason().substring(0, 17) + "..." : req.getReason()) : "");
                }

                ConsoleUtil.printInfo("\nTotal requests: " + requests.size());
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void cancelLeaveRequest() {
        ConsoleUtil.printSubHeader("Cancel Leave Request");

        try {
            List<LeaveRequest> requests = leaveService.getMyLeaveHistory();
            List<LeaveRequest> cancellable = requests.stream()
                    .filter(r -> "PENDING".equals(r.getStatus()) || "APPROVED".equals(r.getStatus()))
                    .toList();

            if (cancellable.isEmpty()) {
                ConsoleUtil.printInfo("No leave requests available to cancel.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            System.out.println("Cancellable Leave Requests:");
            System.out.printf("%-5s %-12s %-12s %-5s %-10s%n", "ID", "From", "To", "Days", "Status");
            ConsoleUtil.printLine();

            for (LeaveRequest req : cancellable) {
                System.out.printf("%-5d %-12s %-12s %-5d %-10s%n",
                        req.getLeaveRequestId(),
                        ConsoleUtil.formatDate(req.getStartDate()),
                        ConsoleUtil.formatDate(req.getEndDate()),
                        req.getTotalDays(),
                        ConsoleUtil.formatStatus(req.getStatus().name()));
            }

            int requestId = ConsoleUtil.readInt("\nEnter Request ID to cancel");


            boolean found = cancellable.stream()
                    .anyMatch(r -> r.getLeaveRequestId() == requestId);

            if (!found) {
                ConsoleUtil.printError("Invalid request ID.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            if (ConsoleUtil.confirm("Are you sure you want to cancel this leave request?")) {
                leaveService.cancelLeave(requestId);
                ConsoleUtil.printSuccess("Leave request cancelled successfully!");
            }

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void viewHolidays() {
        ConsoleUtil.printSubHeader("Holiday Calendar");

        try {
            List<Holiday> holidays = leaveService.getHolidays();

            if (holidays.isEmpty()) {
                ConsoleUtil.printInfo("No holidays configured for this year.");
            } else {
                System.out.printf("%-15s %-35s %-10s%n", "Date", "Holiday", "Optional");
                ConsoleUtil.printLine();

                for (Holiday holiday : holidays) {
                    System.out.printf("%-15s %-35s %-10s%n",
                            ConsoleUtil.formatDate(holiday.getHolidayDate()),
                            holiday.getHolidayName(),
                            holiday.isOptional() ? "Yes" : "No");
                }
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    //   PERFORMANCE & GOALS

    private void performanceMenu() {
        while (true) {
            ConsoleUtil.printSubHeader("Performance & Goals");

            String[] options = {
                    "View Current Review",
                    "Submit Self-Assessment",
                    "View My Review History",
                    "View My Goals",
                    "Create New Goal",
                    "Update Goal Progress"
            };

            ConsoleUtil.printMenu(options);
            int choice = ConsoleUtil.readMenuChoice(6);

            switch (choice) {
                case 1: viewCurrentReview(); break;
                case 2: submitSelfAssessment(); break;
                case 3: viewReviewHistory(); break;
                case 4: viewMyGoals(); break;
                case 5: createGoal(); break;
                case 6: updateGoalProgress(); break;
                case 0: return;
            }
        }
    }

    private void viewCurrentReview() {
        ConsoleUtil.printSubHeader("Current Performance Review");

        try {
            PerformanceReview review = performanceService.getMyCurrentReview();

            System.out.println("Review Year : " + review.getReviewYear());
            System.out.println("Status: " + ConsoleUtil.formatStatus(review.getStatus().name()));
            ConsoleUtil.printLine();

            if (review.getSelfAssessment() != null) {
                System.out.println("\nYour Self-Assessment :");
                System.out.println("  " + review.getSelfAssessment());
                System.out.println("  Self Rating : " + review.getSelfRating() + "/5");
            } else {
                ConsoleUtil.printWarning("Self-assessment not yet submitted.");
            }

            if (review.getManagerFeedback() != null) {
                System.out.println("\nManager Feedback :");
                System.out.println("  " + review.getManagerFeedback());
                System.out.println("  Manager Rating : " + review.getManagerRating() + "/5");
            } else if ("SELF_ASSESSMENT_SUBMITTED".equals(review.getStatus())) {
                ConsoleUtil.printInfo("Awaiting manager feedback.");
            }

            if (review.getFinalRating() != null) {
                System.out.println("\n" + ConsoleUtil.BOLD + "Final Rating : " +
                        String.format("%.1f", review.getFinalRating()) + "/5" + ConsoleUtil.RESET);
            }

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void submitSelfAssessment() {
        ConsoleUtil.printSubHeader("Submit Self-Assessment");

        try {
            PerformanceReview review = performanceService.getMyCurrentReview();

            if (review.getSelfAssessment() != null && !review.getSelfAssessment().isEmpty()) {
                ConsoleUtil.printWarning("You have already submitted a self-assessment for this review period.");
                System.out.println("Your submitted assessment : " + review.getSelfAssessment());
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            System.out.println("Review Year: " + review.getReviewYear());
            System.out.println("\nPlease provide a comprehensive self-assessment of your performance.");
            System.out.println("Consider your achievements, challenges, and areas for improvement.\n");

            String assessment = ConsoleUtil.readRequiredString("Self-Assessment");

            System.out.println("\nRate your overall performance:");
            System.out.println("  1 - Needs Improvement");
            System.out.println("  2 - Below Expectations");
            System.out.println("  3 - Meets Expectations");
            System.out.println("  4 - Exceeds Expectations");
            System.out.println("  5 - Outstanding");

            int rating = ConsoleUtil.readIntInRange("Self Rating", 1, 5);

            System.out.println("\n--- Review Summary ---");
            System.out.println("Assessment : " + assessment);
            System.out.println("Self Rating : " + rating + "/5");

            if (ConsoleUtil.confirm("\nSubmit this self-assessment?")) {
                performanceService.submitSelfAssessment(review.getReviewId(), assessment, rating);
                ConsoleUtil.printSuccess("Self-assessment submitted successfully!");
                ConsoleUtil.printInfo("Your manager will be notified to review your assessment.");
            }

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }


    private void viewReviewHistory() {
        ConsoleUtil.printSubHeader("My Performance Review History");

        try {
            List<PerformanceReview> reviews = performanceService.getMyReviewHistory();

            if (reviews.isEmpty()) {
                ConsoleUtil.printInfo("No review history available.");
            } else {
                System.out.printf("%-12s %-20s %-12s %-12s %-12s%n",
                        "Period", "Status", "Self Rating", "Mgr Rating", "Final");
                ConsoleUtil.printLine();

                for (PerformanceReview review : reviews) {
                    System.out.printf("%-12s %-20s %-12s %-12s %-12s%n",
                            String.valueOf(review.getReviewYear()),
                            review.getStatus(),
                            review.getSelfRating() != null ? review.getSelfRating() + "/5" : "N/A",
                            review.getManagerRating() != null ? review.getManagerRating() + "/5" : "N/A",
                            review.getFinalRating() != null ?
                                    String.format("%.1f/5", review.getFinalRating()) : "N/A");
                }
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void viewMyGoals() {
        ConsoleUtil.printSubHeader("My Goals");

        try {
            List<Goal> goals = performanceService.getMyGoals();

            if (goals.isEmpty()) {
                ConsoleUtil.printInfo("No goals found. Create a new goal to get started!");
            } else {
                System.out.printf("%-5s %-30s %-12s %-10s %-12s%n",
                        "ID", "Title", "Target Date", "Progress", "Status");
                ConsoleUtil.printLine();

                for (Goal goal : goals) {
                    System.out.printf("%-5d %-30s %-12s %-10s %-12s%n",
                            goal.getGoalId(),
                            goal.getGoalDescription().length() > 30 ?
                                    goal.getGoalDescription().substring(0, 27) + "..." : goal.getGoalDescription(),
                            ConsoleUtil.formatDate(goal.getDeadline()),
                            goal.getProgressPercentage() + "%",
                            ConsoleUtil.formatStatus(goal.getStatus().name()));
                }


                long completed = goals.stream().filter(g -> "COMPLETED".equals(g.getStatus())).count();
                long inProgress = goals.stream().filter(g -> "IN_PROGRESS".equals(g.getStatus())).count();
                System.out.println("\nSummary: " + completed + " completed, " +
                        inProgress + " in progress, " + (goals.size() - completed - inProgress) + " not started");
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void createGoal() {
        ConsoleUtil.printSubHeader("Create New Goal");

        try {
            System.out.println("Define a SMART goal (Specific, Measurable, Achievable, Relevant, Time-bound)\n");

            Goal goal = new Goal();
            goal.setEmployeeId(authService.getLoggedInUser().getEmployeeId());
            goal.setDescription(ConsoleUtil.readOptionalString("Description (detailed explanation)"));
            goal.setGoalDescription(ConsoleUtil.readRequiredString("Goal Title"));
            goal.setDeadline(ConsoleUtil.readFutureDate("Target Completion Date"));

            System.out.println("\n--- Goal Summary ---");
            System.out.println("Title : " + goal.getGoalDescription());
            System.out.println("Description : " + (goal.getDescription() != null ? goal.getDescription() : "N/A"));
            System.out.println("Target Date : " + goal.getDeadline());

            if (ConsoleUtil.confirm("\nCreate this goal?")) {
                Goal created = performanceService.createGoal(goal);
                ConsoleUtil.printSuccess("Goal created successfully! ID : " + created.getGoalId());
            }

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void updateGoalProgress() {
        ConsoleUtil.printSubHeader("Update Goal Progress");

        try {
            List<Goal> goals = performanceService.getMyGoals();
            List<Goal> updateable = goals.stream()
                    .filter(g -> !"COMPLETED".equals(g.getStatus()))
                    .toList();

            if (updateable.isEmpty()) {
                ConsoleUtil.printInfo("No goals available to update. All goals are completed!");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            System.out.println("Goals to Update :");
            System.out.printf("%-5s %-30s %-10s %-12s%n", "ID", "Title", "Progress", "Status");
            ConsoleUtil.printLine();

            for (Goal goal : updateable) {
                System.out.printf("%-5d %-30s %-10s %-12s%n",
                        goal.getGoalId(),
                        goal.getGoalDescription().length() > 30 ?
                                goal.getGoalDescription().substring(0, 27) + "..." : goal.getGoalDescription(),
                        goal.getProgressPercentage() + "%",
                        goal.getStatus());
            }

            int goalId = ConsoleUtil.readInt("\nEnter Goal ID to update");

            boolean found = updateable.stream().anyMatch(g -> g.getGoalId() == goalId);
            if (!found) {
                ConsoleUtil.printError("Invalid goal ID.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            int currentProgress = updateable.stream()
                    .filter(g -> g.getGoalId() == goalId)
                    .mapToInt(Goal::getProgressPercentage)
                    .findFirst()
                    .orElse(0);

            System.out.println("Current progress : " + currentProgress + "%");
            int newProgress = ConsoleUtil.readIntInRange("New Progress Percentage", 0, 100);
            String notes = ConsoleUtil.readOptionalString("Progress Notes");

            performanceService.updateGoalProgress(goalId, newProgress, notes);

            if (newProgress == 100) {
                ConsoleUtil.printSuccess("Congratulations! Goal marked as COMPLETED!");
            } else {
                ConsoleUtil.printSuccess("Goal progress updated to " + newProgress + "%");
            }

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    //   EMPLOYEE DIRECTORY

    private void employeeDirectory() {
        ConsoleUtil.printSubHeader("Employee Directory");

        String searchTerm = ConsoleUtil.readRequiredString("Search by name or email");

        try {
            List<Employee> employees = employeeService.searchEmployees(searchTerm);

            if (employees.isEmpty()) {
                ConsoleUtil.printInfo("No employees found matching '" + searchTerm + "'");
            } else {
                System.out.printf("%-25s %-30s %-12s%n", "Name", "Email", "Role");
                ConsoleUtil.printLine();

                for (Employee emp : employees) {
                    if (emp.isActive()) {
                        System.out.printf("%-25s %-30s %-12s%n",
                                emp.getFirstName() + " " + emp.getLastName(),
                                emp.getEmail(),
                                emp.getRole());
                    }
                }
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    //   NOTIFICATIONS

    private void viewNotifications() {
        ConsoleUtil.printSubHeader("My Notifications");

        try {
            List<Notification> notifications = notificationService.getMyNotifications();

            if (notifications.isEmpty()) {
                ConsoleUtil.printInfo("No notifications.");
            } else {
                System.out.println("--- Notifications ---\n");

                for (Notification notif : notifications) {
                    String readStatus = notif.isRead() ? "  " : ConsoleUtil.YELLOW + "[NEW] " + ConsoleUtil.RESET;
                    System.out.println(readStatus + notif.getTitle());
                    System.out.println("      " + notif.getMessage());
                    System.out.println("      " + notif.getCreatedAt());
                    System.out.println();
                }

                if (ConsoleUtil.confirm("Mark all notifications as read?")) {
                    notificationService.markAllAsRead();
                    ConsoleUtil.printSuccess("All notifications marked as read.");
                }
            }


            //   Show recent announcements

            System.out.println("\n--- Company Announcements ---\n");
            List<Announcement> announcements = notificationService.getRecentAnnouncements(5);

            if (announcements.isEmpty()) {
                ConsoleUtil.printInfo("No recent announcements.");
            } else {
                for (Announcement ann : announcements) {
                    System.out.println(ConsoleUtil.BOLD + ann.getTitle() + ConsoleUtil.RESET);
                    System.out.println("  " + ann.getContent());
                    System.out.println("  Posted : " + ann.getCreatedAt());
                    ConsoleUtil.printLine();
                }
            }

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    //   PROFILE MANAGEMENT

    private void viewMyProfile() {
        ConsoleUtil.printSubHeader("My Profile");

        try {
            Employee profile = employeeService.viewMyProfile();

            System.out.println("\n" + ConsoleUtil.BOLD + "Personal Information" + ConsoleUtil.RESET);
            System.out.println("  Employee ID : " + profile.getEmployeeId());
            System.out.println("  Name : " + profile.getFirstName() + " " + profile.getLastName());
            System.out.println("  Email : " + profile.getEmail());
            System.out.println("  Phone : " + (profile.getPhone() != null ? profile.getPhone() : "Not provided"));
            System.out.println("  Address : " + (profile.getAddress() != null ? profile.getAddress() : "Not provided"));

            System.out.println("\n" + ConsoleUtil.BOLD + "Employment Information" + ConsoleUtil.RESET);
            System.out.println("  Role : " + profile.getRole());
            System.out.println("  Date of Joining : " + ConsoleUtil.formatDate(profile.getDateOfJoining()));
            System.out.println("  Status : " + ConsoleUtil.formatStatus(profile.isActive() ? "ACTIVE" : "INACTIVE"));

            if (profile.getManagerId() != null) {
                try {
                    Employee manager = employeeService.getEmployeeById(profile.getManagerId());
                    System.out.println("  Reports to : " + manager.getFirstName() + " " + manager.getLastName());
                } catch (Exception e) {
                    System.out.println("  Reports to : Manager #" + profile.getManagerId());
                }
            }

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void updateMyDetails() {
        ConsoleUtil.printSubHeader("Update My Details");

        try {
            Employee profile = employeeService.viewMyProfile();

            System.out.println("You can update your phone number and address.");
            System.out.println("(Press Enter to keep current value)\n");

            System.out.println("Current Phone : " + (profile.getPhone() != null ? profile.getPhone() : "Not set"));
            String newPhone = ConsoleUtil.readString("New Phone (optional)");
            if (!newPhone.isEmpty()) {
                profile.setPhone(newPhone);
            }

            System.out.println("\nCurrent Address : " + (profile.getAddress() != null ? profile.getAddress() : "Not set"));
            String newAddress = ConsoleUtil.readString("New Address (optional)");
            if (!newAddress.isEmpty()) {
                profile.setAddress(newAddress);
            }

            if (!newPhone.isEmpty() || !newAddress.isEmpty()) {
                employeeService.updateEmployee(profile);
                ConsoleUtil.printSuccess("Profile updated successfully!");
            } else {
                ConsoleUtil.printInfo("No changes made.");
            }

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void changePassword() {
        ConsoleUtil.printSubHeader("Change Password");

        try {
            System.out.println("Password requirements:");
            System.out.println("  - At least 8 characters");
            System.out.println("  - At least one uppercase letter");
            System.out.println("  - At least one lowercase letter");
            System.out.println("  - At least one digit");
            System.out.println("  - At least one special character\n");

            String oldPassword = ConsoleUtil.readPassword("Current Password");
            String newPassword = ConsoleUtil.readNewPassword("New Password");
            String confirmPassword = ConsoleUtil.readPassword("Confirm New Password");

            authService.changePassword(oldPassword, newPassword, confirmPassword);
            ConsoleUtil.printSuccess("Password changed successfully!");

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }
}

