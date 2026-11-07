package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;
    private final ActivityLogDao logDao = new ActivityLogDao();

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
            System.out.println("7. View System Audit Logs");
            System.out.println("8. Logout");
            System.out.print("Enter Choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice){
                case "1" -> viewAllUser();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser(admin);
                case "5" -> updateUser(admin);
                case "7" -> auditLogMenu(admin);

                case "8" -> {
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

    private void approveUser(User admin){
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

        if(userDao.approveUser(userId)){
            logDao.logAction(admin.user_id(), "USER_APPROVED",
                    "Admin [" + admin.user_id() + "] approved account for User ID " + userId);
        }
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

        if(userDao.updateUserAndStatus(user_id, newRole, newStatus)){
            logDao.logAction(currentAdmin.user_id(), "USER_UPDATED",
                    "Admin [" + currentAdmin.user_id() + "] changed role of User " + user_id
                            + " to " + newRole + " and status to " + newStatus);
        }

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

    // ------------------------------------------------------------------
    // SYSTEM AUDIT LOGS
    // ------------------------------------------------------------------

    private void auditLogMenu(User admin){
        boolean back = false;

        while (!back){
            System.out.println("\n--- SYSTEM AUDIT LOGS ---");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear All Logs");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");

            switch (scanner.nextLine().trim()){
                case "1" -> displayLogTable(logDao.getAllLogs(), " ALL SYSTEM LOGS ");
                case "2" -> filterLogsByUser();
                case "3" -> clearLogs(admin);
                case "0" -> back = true;
                default -> System.out.println(" -> [ERROR] Invalid Option.");
            }
        }
    }

    private void filterLogsByUser(){
        int userId = readIntInput("Enter User ID to filter (or 0 to cancel): ");

        if(userId == 0){
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        displayLogTable(logDao.getLogsByUserId(userId), " LOGS FOR USER ID " + userId + " ");
    }

    private void clearLogs(User admin){
        System.out.print("This will permanently delete ALL logs. Type YES to confirm: ");

        if(!scanner.nextLine().trim().equals("YES")){
            System.out.println(" -> Operation Cancelled. No logs were deleted.");
            return;
        }

        int deleted = logDao.clearLogs();

        if(deleted >= 0){
            System.out.println(" -> " + deleted + " log entries cleared.");
            // the clearing itself is recorded, so the audit trail is never silently empty
            logDao.logAction(admin.user_id(), "LOGS_CLEARED",
                    "Admin [" + admin.user_id() + "] cleared " + deleted + " log entries");
        }
    }

    private void displayLogTable(List<ActivityLog> logs, String title){
        System.out.println("\n=====================" + title + "========================");
        if (logs.isEmpty()){
            System.out.println("-- No Records Found");
            return;
        }

        // column widths: ID, USER ID, ACTION TYPE, DESCRIPTION, TIMESTAMP
        String border = "+" + "-".repeat(7) + "+" + "-".repeat(10) + "+" + "-".repeat(20)
                + "+" + "-".repeat(37) + "+" + "-".repeat(22) + "+";
        String format = "| %-5s | %-8s | %-18s | %-35s | %-20s |%n";

        System.out.println(border);
        System.out.printf(format, "ID", "USER ID", "ACTION TYPE", "DESCRIPTION", "TIMESTAMP");
        System.out.println(border);

        for (ActivityLog log : logs){
            // long descriptions continue on extra lines instead of being cut off
            List<String> lines = wrap(log.description(), 35);

            for (int i = 0; i < lines.size(); i++){
                if (i == 0){
                    System.out.printf(format,
                            log.log_id(),
                            log.user_id(),
                            cut(log.action_type(), 18),
                            lines.get(i),
                            cut(log.timestamp(), 20));
                } else {
                    System.out.printf(format, "", "", "", lines.get(i), "");
                }
            }
        }

        System.out.println(border);
        System.out.println("Total: " + logs.size());
    }

    // splits text into lines of at most `width` characters, breaking at spaces
    private List<String> wrap(String text, int width){
        List<String> lines = new ArrayList<>();

        if (text == null || text.isEmpty()){
            lines.add("");
            return lines;
        }

        StringBuilder line = new StringBuilder();

        for (String word : text.split(" ")){
            while (word.length() > width){
                if (line.length() > 0){
                    lines.add(line.toString());
                    line.setLength(0);
                }
                lines.add(word.substring(0, width));
                word = word.substring(width);
            }

            if (line.length() > 0 && line.length() + 1 + word.length() > width){
                lines.add(line.toString());
                line.setLength(0);
            }

            if (line.length() > 0){
                line.append(' ');
            }
            line.append(word);
        }

        if (line.length() > 0){
            lines.add(line.toString());
        }

        return lines;
    }

}