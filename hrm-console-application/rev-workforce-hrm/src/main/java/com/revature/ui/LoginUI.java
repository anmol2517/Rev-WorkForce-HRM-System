package com.revature.ui;

import com.revature.exception.AppException;
import com.revature.model.Employee;
import com.revature.service.AuthService;
import com.revature.util.ConsoleUtil;


//   Login UI for authentication  ||  Handles user login, logout, and redirects to appropriate role-based menu.

public class LoginUI {

    private final AuthService authService;
    private final AdminUI adminUI;
    private final ManagerUI managerUI;
    private final EmployeeUI employeeUI;
    private final NotificationUI notificationUI;
    
    public LoginUI(AuthService authService, AdminUI adminUI, 
                   ManagerUI managerUI, EmployeeUI employeeUI,
                   NotificationUI notificationUI) {
        this.authService = authService;
        this.adminUI = adminUI;
        this.managerUI = managerUI;
        this.employeeUI = employeeUI;
        this.notificationUI = notificationUI;
    }

    public void start() {
        while (true) {
            System.out.println("\n--- Select Option ---");
            System.out.println("1. Login");
            System.out.println("2. Exit");

            int choice = ConsoleUtil.readIntInRange("Enter your choice", 1, 2);

            switch (choice) {
                case 1:
                    login();
                    break;
                case 2:
                    exitApplication();
                    return;
                default:
                    ConsoleUtil.printError("Invalid option");
            }
        }
    }

    private void displayWelcome() {
        System.out.println("==================================================");
        System.out.println("             🚀 REV WORKFORCE HRM 🚀               ");
        System.out.println("==================================================");
        System.out.println("   Welcome to the Human Resource Management System ");
        System.out.println("                Powered by Revature                ");
        System.out.println("==================================================");
        System.out.println();
    }


    private void login() {
        ConsoleUtil.printSubHeader("Login");

        String email = ConsoleUtil.readEmail("Email");
        String password = ConsoleUtil.readPassword("Password");

        try {
            Employee employee = authService.login(email, password);

            ConsoleUtil.printSuccess("Login successful! Welcome, " +
                    employee.getFirstName() + " " + employee.getLastName());

            notificationUI.showUnreadAlert();

            ConsoleUtil.pressEnterToContinue();

            redirectToRoleMenu(employee);

        } catch (AppException e) {
            ConsoleUtil.printError(e.getMessage());
            ConsoleUtil.pressEnterToContinue();
        }
    }

    private void redirectToRoleMenu(Employee employee) {
        String role = employee.getRole().name().toUpperCase();
        
        switch (role) {
            case "ADMIN":
                adminUI.showMenu();
                break;
            case "MANAGER":
                managerUI.showMenu();
                break;
            case "EMPLOYEE":
                employeeUI.showMenu();
                break;
            default:
                ConsoleUtil.printError("Unknown role : " + role);
        }

        try {
            authService.logout();
            ConsoleUtil.printInfo("You have been logged out.");
        } catch (AppException e) {
        }
    }

    private void exitApplication() {
        ConsoleUtil.printInfo("Thank you for using Rev Workforce HRM. Goodbye!");
        ConsoleUtil.close();
    }
}


