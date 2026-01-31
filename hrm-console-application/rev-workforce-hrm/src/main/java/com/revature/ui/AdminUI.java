package com.revature.ui;

import com.revature.exception.AppException;
import com.revature.model.*;
import com.revature.service.*;
import com.revature.util.ConsoleUtil;
import com.revature.model.Employee.Role;


import java.time.LocalDate;
import java.util.List;


//    Admin Console UI   ||  Provides full access to all HRM modules for administrators.

public class AdminUI {

    private final AuthService authService;
    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final PerformanceService performanceService;
    private final NotificationService notificationService;
    private final NotificationUI notificationUI;

    public AdminUI(AuthService authService, EmployeeService employeeService,
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
                    "Employee Management",
                    "Leave Management",
                    "Performance Management",
                    "System Notifications",
                    "Audit Logs",
                    "Department Management",
                    "Change Password",
                    "Logout"
            };

            ConsoleUtil.printMenu(options);
            int choice = ConsoleUtil.readInt("Enter your choice (0-8)");

            switch (choice) {
                case 1: employeeManagementMenu(); break;
                case 2: leaveManagementMenu(); break;
                case 3: performanceManagementMenu(); break;
                case 4: notificationUI.handleMenu(); break;
                case 5: viewAuditLogs(); break;
                case 6: departmentManagementMenu(); break;
                case 7: changePassword(); break;
                case 8:
                case 0: return;
                default: ConsoleUtil.printError("Invalid option");
            }
        }
    }

    private void departmentManagementMenu() {
        while (true) {
            ConsoleUtil.printSubHeader("Department Management");
            System.out.println("1. View All Departments");
            System.out.println("2. Add New Department");
            System.out.println("3. View Designations");
            System.out.println("0. Back");

            int choice = ConsoleUtil.readInt("Enter your choice (0-3)");
            if (choice == 0) return;

            try {
                if (choice == 1) {
                    viewDepartments();
                } else if (choice == 2) {
                    String name = ConsoleUtil.readRequiredString("Department Name");
                    String desc = ConsoleUtil.readOptionalString("Description");
                    com.revature.dao.DepartmentDAO deptDao = new com.revature.dao.DepartmentDAOImpl();
                    com.revature.model.Department d = new com.revature.model.Department();
                    d.setDepartmentName(name);
                    d.setDescription(desc);
                    deptDao.createDepartment(d);

                    ConsoleUtil.printSuccess("Department added successfully!");
                } else if (choice == 3) {
                    viewDesignations();
                } else {
                    ConsoleUtil.printError("Invalid option");
                }
            } catch (Exception e) {
                ConsoleUtil.printError("Error : " + e.getMessage());
            }
            ConsoleUtil.pressEnterToContinue();
        }
    }

    private void displayHeader() {
        Employee user = authService.getLoggedInUser();
        ConsoleUtil.printHeader("ADMIN DASHBOARD");
        System.out.println("  Logged in as : " + user.getFirstName() + " " + user.getLastName());
        System.out.println("  Role : ADMIN");

        try {
            int unread = notificationService.getUnreadCount();
            if (unread > 0) {
                ConsoleUtil.printWarning("You have " + unread + " unread notification(s)");
            }
        } catch (Exception e) {  }
        System.out.println();
    }

    //   EMPLOYEE MANAGEMENT

    private void employeeManagementMenu() {
        while (true) {
            ConsoleUtil.printSubHeader("Employee Management");

            String[] options = {
                "Add New Employee",
                "View All Employees",
                "Search Employee",
                "Update Employee",
                "Assign Manager",
                "Deactivate Employee",
                "Reactivate Employee",
                "Reset Employee Password"
            };

            ConsoleUtil.printMenu(options);
            int choice = ConsoleUtil.readInt("Enter your choice (0-8)");

            switch (choice) {
                case 1: addEmployee(); break;
                case 2: viewAllEmployees(); break;
                case 3: searchEmployee(); break;
                case 4: updateEmployee(); break;
                case 5: assignManager(); break;
                case 6: deactivateEmployee(); break;
                case 7: reactivateEmployee(); break;
                case 8: resetEmployeePassword(); break;
                case 0: return;
            }
        }
    }

    private void addEmployee() {
        ConsoleUtil.printSubHeader("Add New Employee");

        try {
            Employee employee = new Employee();

            employee.setFirstName(ConsoleUtil.readRequiredString("First Name"));
            employee.setLastName(ConsoleUtil.readRequiredString("Last Name"));
            employee.setEmail(ConsoleUtil.readEmail("Email"));
            employee.setPhone(ConsoleUtil.readPhone("Phone"));
            employee.setRole(Employee.Role.valueOf(ConsoleUtil.readString("Enter Role").toUpperCase()));
            employee.setDateOfJoining(ConsoleUtil.readDate("Date of Joining"));
            employee.setAddress(ConsoleUtil.readOptionalString("Address"));

            //  Select department

            List<Department> departments = employeeService.getAllDepartments();
            if (!departments.isEmpty()) {
                System.out.println("\nAvailable Departments : ");
                for (int i = 0; i < departments.size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + departments.get(i).getDepartmentName());
                }
                int deptChoice = ConsoleUtil.readIntInRange("Select Department", 1, departments.size());
                employee.setDepartmentId(departments.get(deptChoice - 1).getDepartmentId());
            }

            //  Select designation

            List<Designation> designations = employeeService.getAllDesignations();
            if (!designations.isEmpty()) {
                System.out.println("\nAvailable Designations : ");
                for (int i = 0; i < designations.size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + designations.get(i).getDesignationName());
                }
                int desigChoice = ConsoleUtil.readIntInRange("Select Designation", 1, designations.size());
                employee.setDesignationId(designations.get(desigChoice - 1).getDesignationId());
            }

            if (employee.getRole() != null && !Role.ADMIN.equals(employee.getRole())) {
                List<Employee> managers = employeeService.getAllManagers();
                if (!managers.isEmpty()) {
                    System.out.println("\nAvailable Managers : ");
                    for (int i = 0; i < managers.size(); i++) {
                        Employee mgr = managers.get(i);
                        System.out.println("  " + (i + 1) + ". " + mgr.getFirstName() + " " +
                                mgr.getLastName() + " (" + mgr.getRole().name() + ")");
                    }
                    System.out.println("  0. No Manager");
                    int mgrChoice = ConsoleUtil.readIntInRange("Select Manager", 0, managers.size());
                    if (mgrChoice > 0) {
                        employee.setManagerId(managers.get(mgrChoice - 1).getEmployeeId());
                    }
                }
            }

            String password = ConsoleUtil.readPasswordWithConfirmation("Password");

            Employee created = employeeService.addEmployee(employee, password);
            ConsoleUtil.printSuccess("Employee created successfully! ID : " + created.getEmployeeId());

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void viewAllEmployees() {
        ConsoleUtil.printSubHeader("All Employees");

        try {
            List<Employee> employees = employeeService.getAllEmployees();

            if (employees.isEmpty()) {
                ConsoleUtil.printInfo("No employees found.");
            } else {
                System.out.printf("%-5s %-20s %-25s %-12s %-10s%n",
                        "ID", "Name", "Email", "Role", "Status");
                ConsoleUtil.printLine();

                for (Employee emp : employees) {
                    System.out.printf("%-5d %-20s %-25s %-12s %-10s%n",
                            emp.getEmployeeId(),
                            emp.getFirstName() + " " + emp.getLastName(),
                            emp.getEmail(),
                            emp.getRole(),
                            ConsoleUtil.formatStatus(emp.isActive() ? "ACTIVE" : "INACTIVE"));
                }

                ConsoleUtil.printInfo("Total: " + employees.size() + " employee(s)");
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void searchEmployee() {
        ConsoleUtil.printSubHeader("Search Employee");

        String searchTerm = ConsoleUtil.readRequiredString("Search (name or email)");

        try {
            List<Employee> employees = employeeService.searchEmployees(searchTerm);

            if (employees.isEmpty()) {
                ConsoleUtil.printInfo("No employees found matching '" + searchTerm + "'");
            } else {
                for (Employee emp : employees) {
                    printEmployeeDetails(emp);
                    ConsoleUtil.printLine();
                }
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void updateEmployee() {
        ConsoleUtil.printSubHeader("Update Employee");

        int employeeId = ConsoleUtil.readInt("Enter Employee ID");

        try {
            Employee employee = employeeService.getEmployeeById(employeeId);
            printEmployeeDetails(employee);

            if (!ConsoleUtil.confirm("Do you want to update this employee?")) {
                return;
            }

            System.out.println("\n(Press Enter to keep current value)");

            String firstName = ConsoleUtil.readString("First Name [" + employee.getFirstName() + "]");
            if (!firstName.isEmpty()) employee.setFirstName(firstName);

            String lastName = ConsoleUtil.readString("Last Name [" + employee.getLastName() + "]");
            if (!lastName.isEmpty()) employee.setLastName(lastName);

            String phone = ConsoleUtil.readString("Phone [" +
                    (employee.getPhone() != null ? employee.getPhone() : "N/A") + "]");
            if (!phone.isEmpty()) employee.setPhone(phone);

            String address = ConsoleUtil.readString("Address [" +
                    (employee.getAddress() != null ? employee.getAddress() : "N/A") + "]");
            if (!address.isEmpty()) employee.setAddress(address);

            employeeService.updateEmployee(employee);
            ConsoleUtil.printSuccess("Employee updated successfully!");

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void assignManager() {
        ConsoleUtil.printSubHeader("Assign Manager");

        int employeeId = ConsoleUtil.readInt("Enter Employee ID");

        try {
            Employee employee = employeeService.getEmployeeById(employeeId);
            System.out.println("Employee: " + employee.getFirstName() + " " + employee.getLastName());

            List<Employee> managers = employeeService.getAllManagers();
            System.out.println("\nAvailable Managers:");
            for (int i = 0; i < managers.size(); i++) {
                Employee mgr = managers.get(i);
                System.out.println("  " + (i + 1) + ". " + mgr.getFirstName() + " " +
                        mgr.getLastName() + " (" + mgr.getRole() + ")");
            }

            int mgrChoice = ConsoleUtil.readIntInRange("Select Manager", 1, managers.size());
            int managerId = managers.get(mgrChoice - 1).getEmployeeId();

            employeeService.assignManager(employeeId, managerId);
            ConsoleUtil.printSuccess("Manager assigned successfully!");

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void deactivateEmployee() {
        ConsoleUtil.printSubHeader("Deactivate Employee");
        int employeeId = ConsoleUtil.readInt("Enter Employee ID");
        try {
            Employee employee = employeeService.getEmployeeById(employeeId);
            System.out.println("Employee: " + employee.getFirstName() + " " + employee.getLastName());
            if (ConsoleUtil.confirm("Are you sure you want to deactivate this employee?")) {
                employeeService.deactivateEmployee(employeeId);
                ConsoleUtil.printSuccess("Employee deactivated successfully!");
            }
        } catch (AppException e) { ConsoleUtil.printError(e.getMessage()); }
        ConsoleUtil.pressEnterToContinue();
    }

    private void reactivateEmployee() {
        ConsoleUtil.printSubHeader("Reactivate Employee");
        int employeeId = ConsoleUtil.readInt("Enter Employee ID");
        try {
            employeeService.reactivateEmployee(employeeId);
            ConsoleUtil.printSuccess("Employee reactivated successfully!");
        } catch (AppException e) { ConsoleUtil.printError(e.getMessage()); }
        ConsoleUtil.pressEnterToContinue();
    }
    private void resetEmployeePassword() {
        ConsoleUtil.printSubHeader("Reset Employee Password");

        int employeeId = ConsoleUtil.readInt("Enter Employee ID");

        try {
            Employee employee = employeeService.getEmployeeById(employeeId);
            System.out.println("Employee: " + employee.getFirstName() + " " + employee.getLastName());

            String newPassword = ConsoleUtil.readPasswordWithConfirmation("New Password");
            authService.resetPassword(employeeId, newPassword);
            ConsoleUtil.printSuccess("Password reset successfully!");

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    //   LEAVE MANAGEMENT

    private void leaveManagementMenu() {
        while (true) {
            ConsoleUtil.printSubHeader("Leave Management");

            String[] options = {
                    "View All Leave Requests",
                    "View Pending Requests",
                    "Approve/Reject Leave",
                    "View Holiday Calendar",
                    "Add Holiday",
                    "View Leave Types"
            };

            ConsoleUtil.printMenu(options);
            int choice = ConsoleUtil.readInt("Enter your choice (0-6)");

            if (choice == 0) return;

            switch (choice) {
                case 1: viewAllLeaveRequests(); break;
                case 2: viewPendingLeaveRequests(); break;
                case 3: approveRejectLeave(); break;
                case 4: viewHolidays(); break;
                case 5: addHoliday(); break;
                case 6: viewLeaveTypes(); break;
                default: ConsoleUtil.printError("Invalid option");
            }
        }
    }

    private void viewAllLeaveRequests() {
        ConsoleUtil.printSubHeader("All Leave Requests");

        try {
            List<LeaveRequest> requests = leaveService.getTeamLeaveRequests();
            displayLeaveRequests(requests);
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
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

    private void approveRejectLeave() {
        ConsoleUtil.printSubHeader("Approve/Reject Leave Request");

        int requestId = ConsoleUtil.readInt("Enter Leave Request ID");

        try {
            System.out.println("\n1. Approve");
            System.out.println("2. Reject");
            int action = ConsoleUtil.readIntInRange("Select Action", 1, 2);

            String comments = ConsoleUtil.readOptionalString("Comments");

            if (action == 1) {
                leaveService.approveLeave(requestId, comments);
                ConsoleUtil.printSuccess("Leave request approved!");
            } else {
                if (comments == null || comments.isEmpty()) {
                    comments = ConsoleUtil.readRequiredString("Rejection reason (required)");
                }
                leaveService.rejectLeave(requestId, comments);
                ConsoleUtil.printSuccess("Leave request rejected!");
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void viewHolidays() {
        ConsoleUtil.printSubHeader("Holiday Calendar " + LocalDate.now().getYear());

        try {
            List<Holiday> holidays = leaveService.getHolidays();

            if (holidays.isEmpty()) {
                ConsoleUtil.printInfo("No holidays configured for this year.");
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

    private void addHoliday() {
        ConsoleUtil.printSubHeader("Add Holiday");

        try {
            Holiday holiday = new Holiday();
            holiday.setHolidayName(ConsoleUtil.readRequiredString("Holiday Name"));
            holiday.setHolidayDate(ConsoleUtil.readDate("Holiday Date"));
            holiday.setOptional(ConsoleUtil.confirm("Is this an optional holiday?"));

            leaveService.addHoliday(holiday);
            ConsoleUtil.printSuccess("Holiday added successfully!");

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void viewLeaveTypes() {
        ConsoleUtil.printSubHeader("Leave Types");

        try {
            List<LeaveType> types = leaveService.getAllLeaveTypes();

            System.out.printf("%-5s %-20s %-15s%n", "ID", "Type", "Default Days");
            ConsoleUtil.printLine();

            for (LeaveType type : types) {
                System.out.printf("%-5d %-20s %-15d%n",
                        type.getLeaveTypeId(),
                        type.getTypeName(),
                        type.getDefaultDays());
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    //   PERFORMANCE MANAGEMENT

    private void performanceManagementMenu() {
        while (true) {
            ConsoleUtil.printSubHeader("Performance Management");

            String[] options = {
                    "Initiate Review Cycle",
                    "View All Reviews",
                    "Submit Manager Feedback",
                    "View All Goals"
            };

            ConsoleUtil.printMenu(options);

            int choice = ConsoleUtil.readInt("Enter your choice (0-4)");

            if (choice == 0) return;

            switch (choice) {
                case 1: initiateReviewCycle(); break;
                case 2: viewAllReviews(); break;
                case 3: submitManagerFeedback(); break;
                case 4: viewAllGoals(); break;
                default: ConsoleUtil.printError("Invalid option");
            }
        }
    }

    private void initiateReviewCycle() {
        ConsoleUtil.printSubHeader("Initiate Performance Review Cycle");

        try {
            int year = ConsoleUtil.readInt("Enter Year");
            System.out.println("Select Period:");
            System.out.println("  1. H1 (First Half)");
            System.out.println("  2. H2 (Second Half)");
            int periodChoice = ConsoleUtil.readIntInRange("Period", 1, 2);
            String period = periodChoice == 1 ? "H1" : "H2";

            if (ConsoleUtil.confirm("Initiate review cycle for " + period + " " + year + "?")) {
                performanceService.initiateReviewCycle(year, period);
                ConsoleUtil.printSuccess("Review cycle initiated successfully!");
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void viewAllReviews() {
        ConsoleUtil.printSubHeader("All Performance Reviews");

        try {
            int year = ConsoleUtil.readInt("Enter Year");
            List<PerformanceReview> reviews = performanceService.getAllReviews(year);

            if (reviews.isEmpty()) {
                ConsoleUtil.printInfo("No reviews found for " + year);
            } else {
                System.out.printf("%-5s %-20s %-10s %-10s %-15s%n",
                        "ID", "Employee", "Period", "Status", "Final Rating");
                ConsoleUtil.printLine();

                for (PerformanceReview review : reviews)
                    System.out.printf("%-5d %-20s %-10s %-10s %-15s%n",
                            review.getReviewId(),
                            "Emp #" + review.getEmployeeId(),
                            review.getReviewPeriod() + " " + review.getReviewYear(),
                            ConsoleUtil.formatStatus(review.getStatus().name()),
                            review.getFinalRating() != null ?
                                    String.format("%.1f", review.getFinalRating()) : "N/A");
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void submitManagerFeedback() {
        ConsoleUtil.printSubHeader("Submit Manager Feedback");

        try {
            List<PerformanceReview> pending = performanceService.getPendingReviewsForManager();

            if (pending.isEmpty()) {
                ConsoleUtil.printInfo("No pending reviews to evaluate.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            System.out.println("Pending Reviews:");
            for (PerformanceReview review : pending) {
                System.out.printf("  ID: %d - Employee #%d - %s %d%n",
                        review.getReviewId(), review.getEmployeeId(),
                        review.getReviewPeriod(), review.getReviewYear());
            }

            int reviewId = ConsoleUtil.readInt("\nEnter Review ID");
            String feedback = ConsoleUtil.readRequiredString("Manager Feedback");
            int rating = ConsoleUtil.readIntInRange("Rating", 1, 5);

            performanceService.submitManagerFeedback(reviewId, feedback, rating);
            ConsoleUtil.printSuccess("Feedback submitted successfully!");

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void viewAllGoals() {
        ConsoleUtil.printSubHeader("All Goals");

        try {
            List<Goal> goals = performanceService.getTeamGoals();
            displayGoals(goals);
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    //   NOTIFICATIONS

    private void notificationsMenu() {
        while (true) {
            ConsoleUtil.printSubHeader("Notifications & Announcements");

            String[] options = {
                    "View My Notifications",
                    "Create Announcement",
                    "View All Announcements",
                    "Deactivate Announcement",
                    "View Audit Logs"
            };

            ConsoleUtil.printMenu(options);

            int choice = ConsoleUtil.readInt("Enter your choice (0-5)");

            if (choice == 0) return;

            switch (choice) {
                case 1: viewNotifications(); break;
                case 2: createAnnouncement(); break;
                case 3: viewAnnouncements(); break;
                case 4: deactivateAnnouncement(); break;
                case 5: viewAuditLogs(); break;
                default: ConsoleUtil.printError("Invalid option");
            }
        }
    }

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
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void createAnnouncement() {
        ConsoleUtil.printSubHeader("Create Announcement");

        try {
            Announcement announcement = new Announcement();
            announcement.setTitle(ConsoleUtil.readRequiredString("Title"));
            announcement.setContent(ConsoleUtil.readRequiredString("Content"));
            announcement.setPriority(Announcement.Priority.valueOf(ConsoleUtil.readString("Enter Priority").toUpperCase()));

            notificationService.createAnnouncement(announcement.getTitle(), announcement.getContent());
            ConsoleUtil.printSuccess("Announcement created successfully!");

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void viewAnnouncements() {
        ConsoleUtil.printSubHeader("All Announcements");

        try {
            List<Announcement> announcements = notificationService.getAllAnnouncements();

            for (Announcement ann : announcements) {
                System.out.println("[" + (ann.isActive() ? "ACTIVE" : "INACTIVE") + "] " +
                        ann.getTitle());
                System.out.println("  " + ann.getContent());
                System.out.println("  Created : " + ann.getCreatedAt());
                ConsoleUtil.printLine();
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void deactivateAnnouncement() {
        ConsoleUtil.printSubHeader("Deactivate Announcement");

        int announcementId = ConsoleUtil.readInt("Enter Announcement ID");

        try {
            notificationService.deactivateAnnouncement(announcementId);
            ConsoleUtil.printSuccess("Announcement deactivated!");
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void createNewAnnouncement() {
        String title = ConsoleUtil.readString("Enter Announcement Title : ");
        String content = ConsoleUtil.readString("Enter Announcement Content : ");

        try {
            notificationService.createAnnouncement(title, content);
            ConsoleUtil.printSuccess("Announcement posted successfully!");
        } catch (AppException e) {
            ConsoleUtil.printError("Failed to post : " + e.getMessage());
        }
    }

    private void viewAuditLogs() {
        ConsoleUtil.printSubHeader("Audit Logs");

        try {
            int limit = ConsoleUtil.readIntInRange("Number of records to show", 10, 100);
            List<AuditLog> logs = notificationService.getAuditLogs(limit);

            System.out.printf("%-20s %-15s %-10s %-30s%n", "Timestamp", "Table", "Action", "Description");
            ConsoleUtil.printLine();

            for (AuditLog log : logs) {
                System.out.printf("%-20s %-15s %-10s %-30s%n",
                        log.getChangedAt().toString().substring(0, 16),
                        log.getTableName(),
                        log.getAction(),
                        log.getNewValue() != null ?
                                (log.getNewValue().length() > 30 ?
                                        log.getNewValue().substring(0, 27) + "..." : log.getNewValue()) : "");
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    //   SYSTEM ADMIN

    private void systemAdminMenu() {
        while (true) {
            ConsoleUtil.printSubHeader("System Administration");

            String[] options = {
                    "View System Statistics",
                    "View Departments",
                    "View Designations",
                    "Post System Announcement"
            };

            ConsoleUtil.printMenu(options);
            int choice = ConsoleUtil.readInt("Enter your choice (0-4)");

            if (choice == 0) return;

            switch (choice) {
                case 1: viewSystemStats(); break;
                case 2: viewDepartments(); break;
                case 3: viewDesignations(); break;
                case 4: postAnnouncement(); break;
                default: ConsoleUtil.printError("Invalid option");
            }
        }
    }

    private void viewSystemStats() {
        ConsoleUtil.printSubHeader("System Statistics");

        try {
            List<Employee> employees = employeeService.getAllEmployees();
            long activeCount = employees.stream().filter(Employee::isActive).count();
            long adminCount = employees.stream().filter(e -> "ADMIN".equals(e.getRole())).count();
            long managerCount = employees.stream().filter(e -> "MANAGER".equals(e.getRole())).count();

            System.out.println("Total Employees : " + employees.size());
            System.out.println("Active Employees : " + activeCount);
            System.out.println("Administrators : " + adminCount);
            System.out.println("Managers : " + managerCount);
            System.out.println("Regular Employees : " + (employees.size() - adminCount - managerCount));

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    private void postAnnouncement() {
        ConsoleUtil.printSubHeader("Create New System Announcement");
        String title = ConsoleUtil.readString("Enter Announcement Title : ");
        String content = ConsoleUtil.readString("Enter Announcement Content : ");

        try {
            notificationService.createAnnouncement(title, content);

            ConsoleUtil.printSuccess("Success: Announcement has been posted to all employees!");
        } catch (AppException e) {
            ConsoleUtil.printError("Error : " + e.getMessage());
        }
    }

    private void viewDepartments() {
        ConsoleUtil.printSubHeader("Departments");
        try {
            List<Department> departments = employeeService.getAllDepartments();
            for (int i = 0; i < departments.size(); i++) {
                System.out.println((i + 1) + ". " + departments.get(i).getDepartmentName());
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void viewDesignations() {
        ConsoleUtil.printSubHeader("Designations");

        try {
            List<Designation> designations = employeeService.getAllDesignations();

            for (Designation desig : designations) {
                System.out.println(desig.getDesignationId() + ". " + desig.getDesignationName() +
                        " (Level: " + desig.getLevel() + ")");
            }
        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        ConsoleUtil.pressEnterToContinue();
    }

    //   PROFILE

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

    //   HELPER METHODS

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

        System.out.printf("%-5s %-10s %-25s %-12s %-10s %-10s%n",
                "ID", "Employee", "Title", "Target Date", "Progress", "Status");
        ConsoleUtil.printLine();

        for (Goal goal : goals) {
            System.out.printf("%-5d %-10s %-25s %-12s %-10s %-10s%n",
                    goal.getGoalId(),
                    "Emp #" + goal.getEmployeeId(),
                    goal.getTitle().length() > 25 ?
                            goal.getTitle().substring(0, 22) + "..." : goal.getTitle(),
                    ConsoleUtil.formatDate(goal.getTargetDate()),
                    goal.getProgressPercentage() + "%",
                    ConsoleUtil.formatStatus(goal.getStatus().name()));
        }
    }
}

