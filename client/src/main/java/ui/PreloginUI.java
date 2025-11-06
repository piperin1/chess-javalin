package ui;

import network.ServerFacade;

import java.util.Scanner;

public class PreloginUI {
    private final Scanner scanner;
    private final ServerFacade server;

    public PreloginUI(Scanner scanner, ServerFacade server) {
        this.scanner = scanner;
        this.server = server;
    }

    public String handleInput() {
        System.out.print(" REGISTER | LOGIN | HELP | QUIT ");
        var input = scanner.nextLine().trim().toLowerCase();
        try {
            return switch (input) {
                case "register" -> handleRegister();
                case "login" -> handleLogin();
                case "help" -> {
                    printHelp();
                    yield null; }
                case "quit" -> {
                    System.exit(0);
                    yield null; }
                default -> {
                    System.out.println("Invalid command. Type HELP.");
                    yield null; }
            };
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
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

    private String handleRegister() throws Exception {
        System.out.print("Username: ");
        var username = scanner.nextLine();
        System.out.print("Password: ");
        var password = scanner.nextLine();
        System.out.print("Email: ");
        var email = scanner.nextLine();
        var auth = server.register(username, password, email);
        System.out.println("Registered and logged in as " + username);
        return auth.authToken();
    }

    private String handleLogin() throws Exception {
        System.out.print("Username: ");
        var username = scanner.nextLine();
        System.out.print("Password: ");
        var password = scanner.nextLine();
        var auth = server.login(username, password);
        System.out.println("Logged in as " + username);
        return auth.authToken();
    }
}
