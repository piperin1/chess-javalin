package ui;

import network.HttpCommunicator;
import java.util.Scanner;

public class PreloginUI {
    private final Scanner scanner;
    private final HttpCommunicator http;

    public PreloginUI(Scanner scanner, HttpCommunicator http) {
        this.scanner = scanner;
        this.http = http;
    }

    public boolean handleInput() {
        System.out.print(" REGISTER | LOGIN | HELP | QUIT ");
        var input = scanner.nextLine().trim().toLowerCase();
        try {
            return switch (input) {
                case "register" -> handleRegister();
                case "login" -> handleLogin();
                case "help" -> {
                    printHelp();
                    yield false; }
                case "quit" -> {
                    System.exit(0);
                    yield false; }
                default -> {
                    System.out.println("Invalid command. Type HELP.");
                    yield false; }
            };
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public void printHelp() {
        System.out.println("""
            Available commands:
             - REGISTER: Create a new account
             - LOGIN: Log into an existing account
             - HELP: Show this message
             - QUIT: Exit the program
        """);
    }

    private boolean handleRegister() {
        System.out.print("Username: ");
        var username = scanner.nextLine();
        System.out.print("Password: ");
        var password = scanner.nextLine();
        System.out.print("Email: ");
        var email = scanner.nextLine();
        if (http.register(username, password, email)) {
            System.out.println("Registered and logged in.");
            return true;
        }
        System.out.println("Register failed.");
        return false;
    }

    private boolean handleLogin() {
        System.out.print("Username: ");
        var username = scanner.nextLine();
        System.out.print("Password: ");
        var password = scanner.nextLine();
        if (http.login(username, password)) {
            System.out.println("Logged in.");
            return true;
        }
        System.out.println("Login failed.");
        return false;
    }
}
