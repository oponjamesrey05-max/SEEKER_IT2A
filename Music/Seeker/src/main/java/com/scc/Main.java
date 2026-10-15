package com.scc;

import java.util.Scanner;
import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.AdminMenu;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        UserDao userDao = new UserDao();

        boolean running = true;

        System.out.println("========================================");
        System.out.println("                SEEKER                  ");
        System.out.println("========================================");

        while (running) {

            System.out.println("\nMAIN MENU");
            System.out.println("1. Log In");
            System.out.println("2. Registration");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1":
                    handleLogin(scanner, userDao);
                    break;

                case "2":
                    System.out.println("\n--- REGISTRATION ---");

                    System.out.print("Enter full name: ");
                    String fullName = scanner.nextLine();

                    System.out.print("Enter email: ");
                    String email = scanner.nextLine();

                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();

                    System.out.println("\nSelect your role:");
                    System.out.println("1. Student Freelancer");
                    System.out.println("2. Employer");
                    System.out.println("3. Student Employment Liaison");
                    System.out.print("Enter your choice: ");

                    String roleChoice = scanner.nextLine();
                    String role;

                    switch (roleChoice) {
                        case "1":
                            role = "Student Freelancer";
                            break;
                        case "2":
                            role = "Employer";
                            break;
                        case "3":
                            role = "Student Employment Liaison";
                            break;
                        default:
                            System.out.println("Invalid role selection.");
                            continue;
                    }

                    userDao.registerUser(fullName, email, password, role);
                    break;

                case "3":
                    running = false;
                    System.out.println("\nThank you for using Student Job Matching System!");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        scanner.close();
    }

    private static void handleLogin(Scanner scanner, UserDao userDao){
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);

        if(user != null){
            System.out.println("\n -- WELCOME --" +user.full_name()+" | ROLE: "+user.role()+"");
            switch (user.role()){
                case "Administrator" ->  new AdminMenu(userDao, scanner).display(user);
                default -> System.out.println(" -- Account role not found! -- ");
            }

        }

    }
}