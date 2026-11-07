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
                case "5" -> updateUser(admin);

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

    private void updateUser(User currentAdmin){
        List<User> users = userDao.getAllUser();
        displayUserTable(users, "SELECT USER TO UPDATE");

        if(users.isEmpty()){
            return;
        }



        int user_id = readIntInput("Enter selected ID to update (or 0 to cancel): ");
        if(user_id == 0){
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        if(user_id == currentAdmin.user_id()){
            System.out.println(" -> [WARNING] Modifying current active Admin is restricted");
            return;
        }

        String newRole = readValidRole();
        if(newRole == null){
            System.out.println(" -> Operation Cancelled!");
            return;
        }

        String newStatus = readValidStatus();

        if(newStatus == null){
            System.out.println(" -> Operation Cancelled!");
            return;
        }

        userDao.updateUserAndStatus(user_id, newRole, newStatus);

    }

    private String readValidRole(){
        while(true){
            System.out.print("Enter Role (ADMINISTRATOR/EMPLOYER/STUDENT FREELANCER/STUDENT EMPLOYMENT LIAISON) [0 to cancel]: ");
            String role = scanner.nextLine().trim().toUpperCase();
            if (role.equals("0") || role.equals("CANCEL")){
                return null;

            }

            if(role.equals("ADMINISTRATOR") || role.equals("EMPLOYER") || role.equals("STUDENT FREELANCER") || role.equals("STUDENT EMPLOYMENT LIAISON")){
                return role;
            }
            System.out.println(" -> [ERROR] Invalid Role!");
        }

    }

    private String readValidStatus (){
        while (true){
            System.out.print("Enter Status (ACTIVE/INACTIVE/DEACTIVATED/PENDING) [0 to cancel]: ");
            String status = scanner.nextLine().trim().toUpperCase();
            if (status.equals("0") || status.equals("CANCEL")){
                return null;
            }

            if (status.equals("ACTIVE") || status.equals("INACTIVE") || status.equals("DEACTIVATED") || status.equals("PENDING")){
                return status;
            }
            System.out.println(" -> [ERROR] Invalid status. Must be ACTIVE, INACTIVE, DEACTIVATED or PENDING.");
        }
    }

    private int readIntInput(String prompt){
        while (true){
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try{
                return Integer.parseInt(input);
            }catch (NumberFormatException e){
                System.out.println(" -> [ERROR] Please enter a valid numeric ID.");
            }

        }


    }


}