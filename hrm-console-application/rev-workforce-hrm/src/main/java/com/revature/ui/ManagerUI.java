package com.revature.ui;

import com.revature.exception.AppException;
import com.revature.model.*;
import com.revature.service.*;
import com.revature.util.ConsoleUtil;

import java.util.List;


//   Manager Console UI  ||  Provides access to team management, leave approvals, and performance reviews.

public class ManagerUI {
    
    private final AuthService authService;
    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final PerformanceService performanceService;
    private final NotificationService notificationService;
    private final NotificationUI notificationUI;

    public ManagerUI(AuthService authService, EmployeeService employeeService,
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
                "Team Management",
                "Leave Management",
                "Performance Management",
                "My Leave",
                "My Performance",
                "Notifications",
                "My Profile",
                "Change Password",
                "Logout"
            };
            
            ConsoleUtil.printMenu(options);
            int choice = ConsoleUtil.readMenuChoice(9);

            switch (choice) {
                case 1: teamManagementMenu(); break;
                case 2: leaveManagementMenu(); break;
                case 3: performanceManagementMenu(); break;
                case 4: myLeaveMenu(); break;
                case 5: myPerformanceMenu(); break;
                case 6: notificationUI.handleMenu(); break;
                case 7: viewMyProfile(); break;
                case 8: changePassword(); break;
                case 0: return;
                default: ConsoleUtil.printError("Invalid option");
            }
        }
    }
    
    private void displayHeader() {
        Employee user = authService.getLoggedInUser();
        ConsoleUtil.printHeader("MANAGER DASHBOARD");
        System.out.println("  Logged in as : " + user.getFirstName() + " " + user.getLastName());
        System.out.println("  Role: MANAGER");
        
        try {
            int unread = notificationService.getUnreadCount();
            if (unread > 0) {
                ConsoleUtil.printWarning("You have " + unread + " unread notification(s)");
            }
            
            List<LeaveRequest> pending = leaveService.getPendingRequests();
            if (!pending.isEmpty()) {
                ConsoleUtil.printInfo(pending.size() + " leave request(s) awaiting approval");
            }
        } catch (Exception e) { }
        System.out.println();
    }
    
    //   TEAM MANAGEMENT
    
    private void teamManagementMenu() {
        while (true) {
            ConsoleUtil.printSubHeader("Team Management");
            
            String[] options = {
                "View My Team",
                "View Team Member Details",
                "View Team Leave Balances"
            };
            
            ConsoleUtil.printMenu(options);
            int choice = ConsoleUtil.readMenuChoice(3);
            
            switch (choice) {
                case 1: viewTeam(); break;
                case 2: viewTeamMemberDetails(); break;
                case 3: viewTeamLeaveBalances(); break;
                case 0: return;
            }
        }
    }
    
    private void viewTeam() {
        ConsoleUtil.printSubHeader("My Team");
        
        try {
            List<Employee> team = employeeService.getTeamMembers();
            
            if (team.isEmpty()) {
                ConsoleUtil.printInfo("No team members assigned to you.");
            } else {
                System.out.printf("%-5s %-25s %-30s %-10s%n", "ID", "Name", "Email", "Status");
                ConsoleUtil.printLine();
                
                for (Employee emp : team) {
                    System.out.printf("%-5d %-25s %-30s %-10s%n",
                            emp.getEmployeeId(),
                            emp.getFirstName() + " " + emp.getLastName(),
                            emp.getEmail(),
                            ConsoleUtil.formatStatus(emp.isActive() ? "ACTIVE" : "INACTIVE"));
                }
                
                ConsoleUtil.printInfo("Total team members: " + team.size());
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }
    
    private void viewTeamMemberDetails() {
        ConsoleUtil.printSubHeader("Team Member Details");
        
        int employeeId = ConsoleUtil.readInt("Enter Employee ID");
        
        try {
            Employee employee = employeeService.getEmployeeById(employeeId);
            printEmployeeDetails(employee);

            System.out.println("\nLeave Balances:");
            List<LeaveBalance> balances = leaveService.getLeaveBalances(employeeId);
            for (LeaveBalance balance : balances) {
                System.out.printf("  %s: %d/%d days remaining%n",
                        "Type #" + balance.getLeaveTypeId(),
                        balance.getRemainingLeaves(),
                        balance.getTotalDays());
            }
            
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }
    
    private void viewTeamLeaveBalances() {
        ConsoleUtil.printSubHeader("Team Leave Balances");
        
        try {
            List<Employee> team = employeeService.getTeamMembers();
            
            for (Employee emp : team) {
                System.out.println("\n" + emp.getFirstName() + " " + emp.getLastName() + ":");
                List<LeaveBalance> balances = leaveService.getLeaveBalances(emp.getEmployeeId());
                for (LeaveBalance balance : balances) {
                    System.out.printf("  Type #%d: %d/%d days (Used: %d)%n",
                            balance.getLeaveTypeId(),
                            balance.getRemainingLeaves(),
                            balance.getTotalDays(),
                            balance.getUsedDays());
                }
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }
    
    //   LEAVE MANAGEMENT (TEAM)
    
    private void leaveManagementMenu() {
        while (true) {
            ConsoleUtil.printSubHeader("Team Leave Management");
            
            String[] options = {
                "View Pending Requests",
                "View All Team Leave Requests",
                "Approve Leave",
                "Reject Leave",
                "View Holiday Calendar"
            };
            
            ConsoleUtil.printMenu(options);
            int choice = ConsoleUtil.readMenuChoice(5);
            
            switch (choice) {
                case 1: viewPendingLeaveRequests(); break;
                case 2: viewTeamLeaveRequests(); break;
                case 3: approveLeave(); break;
                case 4: rejectLeave(); break;
                case 5: viewHolidays(); break;
                case 0: return;
            }
        }
    }
    
    private void viewPendingLeaveRequests() {
        ConsoleUtil.printSubHeader("Pending Leave Requests");
        
        try {
            List<LeaveRequest> requests = leaveService.getPendingRequests();
            displayLeaveRequests(requests);
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }
    
    private void viewTeamLeaveRequests() {
        ConsoleUtil.printSubHeader("All Team Leave Requests");
        
        try {
            List<LeaveRequest> requests = leaveService.getTeamLeaveRequests();
            displayLeaveRequests(requests);
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }
    
    private void approveLeave() {
        ConsoleUtil.printSubHeader("Approve Leave Request");
        
        try {
            List<LeaveRequest> pending = leaveService.getPendingRequests();
            if (pending.isEmpty()) {
                ConsoleUtil.printInfo("No pending leave requests.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }
            
            displayLeaveRequests(pending);
            
            int requestId = ConsoleUtil.readInt("\nEnter Request ID to approve");
            String comments = ConsoleUtil.readOptionalString("Comments (optional)");
            
            leaveService.approveLeave(requestId, comments);
            ConsoleUtil.printSuccess("Leave request approved successfully!");
            
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }
    
    private void rejectLeave() {
        ConsoleUtil.printSubHeader("Reject Leave Request");
        
        try {
            List<LeaveRequest> pending = leaveService.getPendingRequests();
            if (pending.isEmpty()) {
                ConsoleUtil.printInfo("No pending leave requests.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }
            
            displayLeaveRequests(pending);
            
            int requestId = ConsoleUtil.readInt("\nEnter Request ID to reject");
            String reason = ConsoleUtil.readRequiredString("Rejection Reason");
            
            leaveService.rejectLeave(requestId, reason);
            ConsoleUtil.printSuccess("Leave request rejected.");
            
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
                ConsoleUtil.printInfo("No holidays configured.");
            } else {
                System.out.printf("%-15s %-30s %-10s%n", "Date", "Holiday", "Optional");
                ConsoleUtil.printLine();
                
                for (Holiday holiday : holidays) {
                    System.out.printf("%-15s %-30s %-10s%n",
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
    
    //   PERFORMANCE MANAGEMENT (TEAM)
    
    private void performanceManagementMenu() {
        while (true) {
            ConsoleUtil.printSubHeader("Team Performance Management");
            
            String[] options = {
                "View Pending Reviews",
                "Submit Review Feedback",
                "View Team Goals",
                "Create Goal for Team Member",
                "Update Goal Progress"
            };
            
            ConsoleUtil.printMenu(options);
            int choice = ConsoleUtil.readMenuChoice(5);
            
            switch (choice) {
                case 1: viewPendingReviews(); break;
                case 2: submitReviewFeedback(); break;
                case 3: viewTeamGoals(); break;
                case 4: createTeamGoal(); break;
                case 5: updateGoalProgress(); break;
                case 0: return;
            }
        }
    }
    
    private void viewPendingReviews() {
        ConsoleUtil.printSubHeader("Pending Performance Reviews");
        
        try {
            List<PerformanceReview> reviews = performanceService.getPendingReviewsForManager();
            
            if (reviews.isEmpty()) {
                ConsoleUtil.printInfo("No pending reviews to evaluate.");
            } else {
                System.out.printf("%-5s %-10s %-10s %-10s %-20s%n",
                        "ID", "Employee", "Period", "Self Rating", "Status");
                ConsoleUtil.printLine();

                for (PerformanceReview review : reviews) {
                    System.out.printf("%-5d %-10s %-10s %-10s %-20s%n",
                            review.getReviewId(),
                            "Emp #" + review.getEmployeeId(),
                            review.getReviewPeriod() + " " + review.getReviewYear(),
                            review.getSelfRating() != null ? review.getSelfRating() + "/5" : "N/A",
                            ConsoleUtil.formatStatus(review.getStatus().name()));
                }
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }
    
    private void submitReviewFeedback() {
        ConsoleUtil.printSubHeader("Submit Performance Feedback");
        
        try {
            List<PerformanceReview> pending = performanceService.getPendingReviewsForManager();
            
            if (pending.isEmpty()) {
                ConsoleUtil.printInfo("No pending reviews.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            System.out.println("Pending Reviews:");
            for (PerformanceReview review : pending) {
                System.out.printf("  ID: %d - Employee #%d - Self Rating: %s%n",
                        review.getReviewId(),
                        review.getEmployeeId(),
                        review.getSelfRating() != null ? review.getSelfRating() + "/5" : "N/A");
                if (review.getSelfAssessment() != null) {
                    System.out.println("    Self-Assessment: " + 
                            (review.getSelfAssessment().length() > 50 ? 
                                    review.getSelfAssessment().substring(0, 47) + "..." : 
                                    review.getSelfAssessment()));
                }
            }
            
            int reviewId = ConsoleUtil.readInt("\nEnter Review ID");
            
            System.out.println("\nProvide your feedback:");
            String feedback = ConsoleUtil.readRequiredString("Manager Feedback");
            int rating = ConsoleUtil.readIntInRange("Rating (1-5)", 1, 5);
            
            performanceService.submitManagerFeedback(reviewId, feedback, rating);
            ConsoleUtil.printSuccess("Feedback submitted successfully!");
            
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }
    
    private void viewTeamGoals() {
        ConsoleUtil.printSubHeader("Team Goals");
        
        try {
            List<Goal> goals = performanceService.getTeamGoals();
            displayGoals(goals);
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }
    
    private void createTeamGoal() {
        ConsoleUtil.printSubHeader("Create Goal for Team Member");
        
        try {
            List<Employee> team = employeeService.getTeamMembers();
            if (team.isEmpty()) {
                ConsoleUtil.printInfo("No team members.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }
            
            System.out.println("Team Members :");
            for (int i = 0; i < team.size(); i++) {
                Employee emp = team.get(i);
                System.out.println("  " + (i + 1) + ". " + emp.getFirstName() + " " + emp.getLastName());
            }
            
            int empChoice = ConsoleUtil.readIntInRange("Select Employee", 1, team.size());
            int employeeId = team.get(empChoice - 1).getEmployeeId();
            
            Goal goal = new Goal();
            goal.setEmployeeId(employeeId);
            goal.setGoalDescription(ConsoleUtil.readRequiredString("Goal Title"));
            goal.setDeadline(ConsoleUtil.readFutureDate("Target Date"));
            goal.setDescription(ConsoleUtil.readOptionalString("Description"));
            
            performanceService.createGoal(goal);
            ConsoleUtil.printSuccess("Goal created successfully!");
            
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }
    
    private void updateGoalProgress() {
        ConsoleUtil.printSubHeader("Update Goal Progress");
        
        int goalId = ConsoleUtil.readInt("Enter Goal ID");
        
        try {
            int progress = ConsoleUtil.readIntInRange("Progress Percentage", 0, 100);
            String notes = ConsoleUtil.readOptionalString("Notes");
            
            performanceService.updateGoalProgress(goalId, progress, notes);
            ConsoleUtil.printSuccess("Goal progress updated!");
            
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }
    
    //   MY LEAVE
    
    private void myLeaveMenu() {
        while (true) {
            ConsoleUtil.printSubHeader("My Leave");
            
            String[] options = {
                "View My Leave Balance",
                "Apply for Leave",
                "View My Leave History",
                "Cancel Leave Request"
            };
            
            ConsoleUtil.printMenu(options);
            int choice = ConsoleUtil.readMenuChoice(4);
            
            switch (choice) {
                case 1: viewMyLeaveBalance(); break;
                case 2: applyForLeave(); break;
                case 3: viewMyLeaveHistory(); break;
                case 4: cancelLeaveRequest(); break;
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
                        balance.getRemainingLeaves());
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
            System.out.println("Leave Types:");
            for (int i = 0; i < types.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + types.get(i).getTypeName());
            }
            
            int typeChoice = ConsoleUtil.readIntInRange("Select Leave Type", 1, types.size());
            
            LeaveRequest request = new LeaveRequest();
            request.setLeaveTypeId(types.get(typeChoice - 1).getLeaveTypeId());
            request.setStartDate(ConsoleUtil.readFutureDate("Start Date"));
            request.setEndDate(ConsoleUtil.readFutureDate("End Date"));
            request.setReason(ConsoleUtil.readRequiredString("Reason"));

            int workingDays = leaveService.calculateWorkingDays(
                    request.getStartDate(), request.getEndDate());
            System.out.println("\nWorking days : " + workingDays);
            
            if (ConsoleUtil.confirm("Submit this leave request?")) {
                LeaveRequest created = leaveService.applyLeave(request);
                ConsoleUtil.printSuccess("Leave request submitted! ID : " + created.getLeaveRequestId());
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
            displayLeaveRequests(requests);
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
                ConsoleUtil.printInfo("No requests available to cancel.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }
            
            displayLeaveRequests(cancellable);
            
            int requestId = ConsoleUtil.readInt("\nEnter Request ID to cancel");
            
            if (ConsoleUtil.confirm("Are you sure you want to cancel this request?")) {
                leaveService.cancelLeave(requestId);
                ConsoleUtil.printSuccess("Leave request cancelled.");
            }
            
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }
    
    //   MY PERFORMANCE
    
    private void myPerformanceMenu() {
        while (true) {
            ConsoleUtil.printSubHeader("My Performance");
            
            String[] options = {
                "View Current Review",
                "Submit Self-Assessment",
                "View My Review History",
                "View My Goals",
                "Create Personal Goal",
                "Update Goal Progress"
            };
            
            ConsoleUtil.printMenu(options);
            int choice = ConsoleUtil.readMenuChoice(6);
            
            switch (choice) {
                case 1: viewCurrentReview(); break;
                case 2: submitSelfAssessment(); break;
                case 3: viewReviewHistory(); break;
                case 4: viewMyGoals(); break;
                case 5: createPersonalGoal(); break;
                case 6: updateMyGoalProgress(); break;
                case 0: return;
            }
        }
    }
    
    private void viewCurrentReview() {
        ConsoleUtil.printSubHeader("Current Review Period");
        
        try {
            PerformanceReview review = performanceService.getMyCurrentReview();
            System.out.println("Review Period : " + review.getReviewPeriod() + " " + review.getReviewYear());
            System.out.println("Status : " + ConsoleUtil.formatStatus(review.getStatus().name()));
            
            if (review.getSelfAssessment() != null) {
                System.out.println("\nYour Self-Assessment :");
                System.out.println("  " + review.getSelfAssessment());
                System.out.println("  Self Rating : " + review.getSelfRating() + "/5");
            }
            
            if (review.getManagerFeedback() != null) {
                System.out.println("\nManager Feedback :");
                System.out.println("  " + review.getManagerFeedback());
                System.out.println("  Manager Rating : " + review.getManagerRating() + "/5");
            }
            
            if (review.getFinalRating() != null) {
                System.out.println("\nFinal Rating : " + String.format("%.1f", review.getFinalRating()) + "/5");
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
            
            if (review.getSelfAssessment() != null) {
                ConsoleUtil.printWarning("You have already submitted a self-assessment for this period.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }
            
            System.out.println("Review Period: " + review.getReviewPeriod() + " " + review.getReviewYear());
            System.out.println("\nPlease provide your self-assessment :");
            
            String assessment = ConsoleUtil.readRequiredString("Self-Assessment");
            int rating = ConsoleUtil.readIntInRange("Self Rating (1-5)", 1, 5);
            
            performanceService.submitSelfAssessment(review.getReviewId(), assessment, rating);
            ConsoleUtil.printSuccess("Self-assessment submitted successfully!");
            
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }
    
    private void viewReviewHistory() {
        ConsoleUtil.printSubHeader("My Review History");
        
        try {
            List<PerformanceReview> reviews = performanceService.getMyReviewHistory();
            
            if (reviews.isEmpty()) {
                ConsoleUtil.printInfo("No review history available.");
            } else {
                System.out.printf("%-10s %-15s %-10s %-10s %-10s%n",
                        "Period", "Status", "Self", "Manager", "Final");
                ConsoleUtil.printLine();
                
                for (PerformanceReview review : reviews) {
                    System.out.printf("%-10s %-15s %-10s %-10s %-10s%n",
                            review.getReviewPeriod() + " " + review.getReviewYear(),
                            review.getStatus(),
                            review.getSelfRating() != null ? review.getSelfRating() + "/5" : "N/A",
                            review.getManagerRating() != null ? review.getManagerRating() + "/5" : "N/A",
                            review.getFinalRating() != null ? 
                                    String.format("%.1f", review.getFinalRating()) : "N/A");
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
            displayGoals(goals);
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }
    
    private void createPersonalGoal() {
        ConsoleUtil.printSubHeader("Create Personal Goal");
        
        try {
            Goal goal = new Goal();
            goal.setEmployeeId(authService.getLoggedInUser().getEmployeeId());
            goal.setGoalDescription(ConsoleUtil.readRequiredString("Goal Title"));
            goal.setDescription(ConsoleUtil.readOptionalString("Description"));
            goal.setDeadline(ConsoleUtil.readFutureDate("Target Date"));
            performanceService.createGoal(goal);
            ConsoleUtil.printSuccess("Goal created successfully!");
            
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }
    
    private void updateMyGoalProgress() {
        ConsoleUtil.printSubHeader("Update My Goal Progress");
        
        try {
            List<Goal> goals = performanceService.getMyGoals();
            List<Goal> inProgress = goals.stream()
                    .filter(g -> !"COMPLETED".equals(g.getStatus()))
                    .toList();
            
            if (inProgress.isEmpty()) {
                ConsoleUtil.printInfo("No goals to update.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }
            
            displayGoals(inProgress);
            
            int goalId = ConsoleUtil.readInt("\nEnter Goal ID");
            int progress = ConsoleUtil.readIntInRange("Progress Percentage", 0, 100);
            String notes = ConsoleUtil.readOptionalString("Notes");
            
            performanceService.updateGoalProgress(goalId, progress, notes);
            ConsoleUtil.printSuccess("Progress updated!");
            
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
                for (Notification notif : notifications) {
                    String readStatus = notif.isRead() ? "" : "[NEW] ";
                    System.out.println(readStatus + notif.getTitle());
                    System.out.println("  " + notif.getMessage());
                    System.out.println("  " + notif.getCreatedAt());
                    ConsoleUtil.printLine();
                }
                
                if (ConsoleUtil.confirm("Mark all as read?")) {
                    notificationService.markAllAsRead();
                }
            }

            System.out.println("\n--- Recent Announcements ---\n");
            List<Announcement> announcements = notificationService.getRecentAnnouncements(5);
            for (Announcement ann : announcements) {
                System.out.println(ann.getTitle());
                System.out.println("  " + ann.getContent());
                ConsoleUtil.printLine();
            }
            
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }


    //  PROFILE
    
    private void viewMyProfile() {
        ConsoleUtil.printSubHeader("My Profile");
        
        try {
            Employee profile = employeeService.viewMyProfile();
            printEmployeeDetails(profile);
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        
        ConsoleUtil.pressEnterToContinue();
    }
    
    private void changePassword() {
        ConsoleUtil.printSubHeader("Change Password");
        
        try {
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


    //  HELPER METHODS

    private void printEmployeeDetails(Employee emp) {
        System.out.println("\nEmployee Details:");
        System.out.println("  ID : " + emp.getEmployeeId());
        System.out.println("  Name : " + emp.getFirstName() + " " + emp.getLastName());
        System.out.println("  Email : " + emp.getEmail());
        System.out.println("  Phone : " + (emp.getPhone() != null ? emp.getPhone() : "N/A"));
        System.out.println("  Role : " + emp.getRole());
        System.out.println("  Date of Joining : " + ConsoleUtil.formatDate(emp.getDateOfJoining()));
        System.out.println("  Status : " + ConsoleUtil.formatStatus(emp.isActive() ? "ACTIVE" : "INACTIVE"));
    }
    
    private void displayLeaveRequests(List<LeaveRequest> requests) {
        if (requests.isEmpty()) {
            ConsoleUtil.printInfo("No leave requests found.");
            return;
        }
        
        System.out.printf("%-5s %-10s %-12s %-12s %-5s %-10s%n",
                "ID", "Employee", "From", "To", "Days", "Status");
        ConsoleUtil.printLine();

        for (LeaveRequest req : requests) {
            System.out.printf("%-5d %-10s %-12s %-12s %-5d %-10s%n",
                    req.getLeaveRequestId(),
                    "Emp #" + req.getEmployeeId(),
                    ConsoleUtil.formatDate(req.getStartDate()),
                    ConsoleUtil.formatDate(req.getEndDate()),
                    req.getTotalDays(),
                    ConsoleUtil.formatStatus(req.getStatus().name()));
        }
    }
    
    private void displayGoals(List<Goal> goals) {
        if (goals.isEmpty()) {
            ConsoleUtil.printInfo("No goals found.");
            return;
        }

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
    }
}


