package ui;

import network.ServerFacade;

import java.util.Scanner;

public class PostloginUI {
    private final ServerFacade server;
    private final Scanner scanner;
    private final String authToken;

    public PostloginUI(Scanner scanner, ServerFacade server, String authToken) {
        this.server = server;
        this.authToken = authToken;
        this.scanner = scanner;
    }

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("\n-- Options --");
            System.out.println("1. List games");
            System.out.println("2. Create new game");
            System.out.println("3. Join game as player");
            System.out.println("4. Join game as observer");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1" -> listGames();
                    case "2" -> createGame();
                    case "3" -> joinGame(false);
                    case "4" -> joinGame(true);
                    case "5" -> {
                        server.logout(authToken);
                        System.out.println("You have been logged out");
                        running = false;
                    }
                    default -> System.out.println("Invalid option");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void listGames() throws Exception {

    }

    private void createGame() throws Exception {

    }

    private void joinGame(boolean observer) throws Exception {

    }

}
