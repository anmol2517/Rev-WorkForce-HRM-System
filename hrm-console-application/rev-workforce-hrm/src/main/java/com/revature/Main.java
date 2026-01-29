package com.revature;

import com.revature.service.*;
import com.revature.ui.*;
import com.revature.util.DBConnection;
import com.revature.dao.*;
import java.util.Scanner;

public class Main {
    private static final String APP_NAME = "Rev Workforce HRM";
    private static final String VERSION = "1.0.0";

    public static void main(String[] args) {
        printBanner();

        if (!testDatabaseConnection()) {
            System.err.println("\n[ERROR] Database connection failed!");
            System.exit(1);
        }

        AuthService authService = new AuthService();
        NotificationService notificationService = new NotificationService();
        notificationService.setAuthService(authService);

        NotificationUI notificationUI = new NotificationUI(notificationService, authService);
        EmployeeService employeeService = new EmployeeService(authService);
        LeaveService leaveService = new LeaveService(authService, notificationService);
        PerformanceService performanceService = new PerformanceService(authService, notificationService);


//      Initialize UI Classes with all services

        AdminUI adminUI = new AdminUI(authService, employeeService, leaveService, performanceService, notificationService, notificationUI);
        ManagerUI managerUI = new ManagerUI(authService, employeeService, leaveService, performanceService, notificationService, notificationUI);
        EmployeeUI employeeUI = new EmployeeUI(authService, employeeService, leaveService, performanceService, notificationService, notificationUI);
        LoginUI loginUI = new LoginUI(authService, adminUI, managerUI, employeeUI, notificationUI);


//      Robust logging printStackTrace

        try {
            loginUI.start();
        } catch (Exception e) {
            System.err.println("\n[ERROR] Fatal error: " + e.getMessage());
        } finally {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Thank you for using " + APP_NAME);
            System.out.println("=".repeat(60));
        }
    }


    private static void printBanner() {
        System.out.println("                                                 ");
        System.out.println("                                                 ");
        System.out.println("╠════════════════════════════════════════════════╣");
        System.out.println("                                                 ");
        System.out.println("               Rev WorkForce : HRM               ");
        System.out.println("                                                 ");
        System.out.println("╠════════════════════════════════════════════════╣");
        System.out.println("          Manage | Empower | Transform          ");
        System.out.println("             System Version : " + VERSION + "  ");
        System.out.println("              Powered by Revature               ");
        System.out.println("╠════════════════════════════════════════════════╣");
        System.out.println("                                                 ");
    }

    private static boolean testDatabaseConnection() {
        try (java.sql.Connection conn = DBConnection.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }
}


