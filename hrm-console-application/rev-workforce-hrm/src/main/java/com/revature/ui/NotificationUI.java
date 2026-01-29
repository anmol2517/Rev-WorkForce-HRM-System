package com.revature.ui;

import com.revature.model.Notification;
import com.revature.model.Announcement;
import com.revature.service.NotificationService;
import com.revature.service.AuthService;
import com.revature.exception.AppException;
import com.revature.util.ConsoleUtil;

import java.util.List;
import java.util.Scanner;

public class NotificationUI {
    private final NotificationService notificationService;
    private final AuthService authService;
    private final Scanner scanner;

    public NotificationUI(NotificationService notificationService, AuthService authService) {
        this.notificationService = notificationService;
        this.authService = authService;
        this.scanner = new Scanner(System.in);
    }

    public void showUnreadAlert() {
        try {
            int count = notificationService.getUnreadCount();
            if (count > 0) {
                System.out.println("\n\u001B[33m" + " [!] Alert: You have " + count + " unread notifications." + "\u001B[0m");
            }
        } catch (AppException ignored) {}
    }

    public void handleMenu() {
        boolean back = false;

        String role = authService.getLoggedInUser().getRole().name();

        while (!back) {
            System.out.println("\n========== NOTIFICATION CENTER ==========");
            System.out.println("1. View My Notifications");
            System.out.println("2. View Company Announcements");
            System.out.println("3. Mark All as Read");

            if ("ADMIN".equalsIgnoreCase(role)) {
                System.out.println("4. Post New Announcement (Admin Only)");
            }

            System.out.println("0. Back to Main Menu");
            System.out.print("Select an option : ");

            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1" -> viewNotifications();
                    case "2" -> viewAnnouncements();
                    case "3" -> {
                        notificationService.markAllAsRead();
                        System.out.println("All notifications marked as read!");
                    }
                    case "4" -> {
                        if ("ADMIN".equalsIgnoreCase(role)) {
                            createNewAnnouncement();
                        } else {
                            System.out.println("Invalid choice!");
                        }
                    }
                    case "0" -> back = true;
                    default -> System.out.println("Invalid choice!");
                }
            } catch (AppException e) {
                System.out.println("Error : " + e.getMessage());
            }
        }
    }

    private void createNewAnnouncement() throws AppException {
        System.out.print("Enter Announcement Title : ");
        String title = scanner.nextLine();
        System.out.print("Enter Content : ");
        String content = scanner.nextLine();

        notificationService.createAnnouncement(title, content);
        System.out.println("Announcement posted successfully to all employees!");
    }

    private void viewNotifications() throws AppException {
        List<Notification> notifications = notificationService.getMyNotifications();
        if (notifications.isEmpty()) {
            System.out.println("No notifications found.");
            return;
        }

        System.out.println("\n--- Your Notifications ---");
        System.out.printf("%-5s | %-10s | %-40s | %-20s%n", "ID", "Type", "Message", "Time");
        System.out.println("-".repeat(80));
        for (Notification n : notifications) {
            String status = n.isRead() ? "" : "[NEW] ";
            System.out.printf("%-5d | %-10s | %-40s | %-20s%n",
                    n.getNotificationId(), n.getNotificationType(), status + n.getMessage(), n.getCreatedAt());
        }
    }

    private void viewAnnouncements() throws AppException {
        List<Announcement> announcements = notificationService.getActiveAnnouncements();
        if (announcements.isEmpty()) {
            System.out.println("No active company announcements.");
            return;
        }

        System.out.println("\n--- COMPANY ANNOUNCEMENTS ---");
        for (Announcement a : announcements) {
            System.out.println("TITLE : " + a.getTitle());
            System.out.println("CONTENT : " + a.getContent());
            System.out.println("POSTED : " + a.getCreatedAt());
            System.out.println("-".repeat(30));
        }
    }
}

