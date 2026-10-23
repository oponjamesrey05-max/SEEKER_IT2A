package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner){
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display(User admin){
        boolean loggedIn = true;
        while (loggedIn){
            System.out.println("\n===========================================");
            System.out.println("              ADMIN DASHBOARD                ");
            System.out.println("=============================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role/ Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. Logout");
            System.out.print("Enter Choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice){
                case "1" -> viewAllUser();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();

                case "7" -> {
                    loggedIn = false;
                    System.out.println(" -- Logged out. Goodbye, " + admin.full_name() + "!");
                }
                default -> System.out.println(" -- [ERROR] Invalid Option.");
            }

        }
    }

    private void displayUserTable(List<User> users, String title){
        System.out.println("\n=====================" + title + "========================");
        if (users.isEmpty()){
            System.out.println("-- No Records Found");
            return;
        }

        // column widths: ID, NAME, EMAIL, ROLE, STATUS, DATE
        String border = "+" + "-".repeat(6) + "+" + "-".repeat(22) + "+" + "-".repeat(28)
                + "+" + "-".repeat(28) + "+" + "-".repeat(14) + "+" + "-".repeat(21) + "+";
        String format = "| %-4s | %-20s | %-26s | %-26s | %-12s | %-19s |%n";

        System.out.println(border);
        System.out.printf(format, "ID", "FULL NAME", "EMAIL", "ROLE", "STATUS", "DATE CREATED");
        System.out.println(border);

        for (User u : users){
            System.out.printf(format,
                    u.user_id(),
                    cut(u.full_name(), 20),
                    cut(u.email(), 26),
                    cut(u.role(), 26),
                    cut(u.status(), 12),
                    cut(u.date_created(), 19));
        }

        System.out.println(border);
        System.out.println("Total: " + users.size());
    }

    // shortens long text with "..." so it never breaks the table
    private String cut(String text, int max){
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }

    private void viewAllUser(){
        displayUserTable(userDao.getAllUser(), " ALL REGISTERED USERS ");
    }

    private void viewPendingUsers(){
        displayUserTable(userDao.getPendingUser(), "PENDING APPROVALS");

    }

    private void approveUser(){
        List<User> pendingUsers = userDao.getPendingUser();
        displayUserTable(pendingUsers, " PENDING APPROVALS ");

        if(pendingUsers.isEmpty()){
            return;
        }

        System.out.print("Enter User ID to approve (or 0 to cancel): ");

        int userId;
        try {
            userId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println(" -- [ERROR] Please enter a valid number.");
            return;
        }

        if(userId == 0){
            System.out.println(" -- Operation cancelled.");
            return;
        }

        boolean exists = pendingUsers.stream().anyMatch(user -> user.user_id() == userId);
        if(!exists){
            System.out.println(" -- [ERROR] User ID not found in the pending list.");
            return;
        }

        userDao.approveUser(userId);
    }

}