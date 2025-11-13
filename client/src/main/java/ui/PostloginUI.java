package ui;

import model.GameData;
import network.ServerFacade;

import java.util.List;
import java.util.Scanner;

public class PostloginUI {
    private final ServerFacade server;
    private final Scanner scanner;
    private final String authToken;
    private List<GameData> games = List.of();

    public PostloginUI(Scanner scanner, ServerFacade server, String authToken) {
        this.server = server;
        this.authToken = authToken;
        this.scanner = scanner;
    }

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("\n-- Options --");
            System.out.println("1. List Games");
            System.out.println("2. Create Game");
            System.out.println("3. Play Game");
            System.out.println("4. Observe Game");
            System.out.println("5. Logout");
            System.out.println("6. Help");
            System.out.print("> ");
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> listGames();
                    case "2" -> createGame();
                    case "3" -> joinGame(false);
                    case "4" -> joinGame(true);
                    case "5" -> {
                        server.logout(authToken);
                        System.out.println("You have been logged out.");
                        running = false;
                    }
                    case "6" -> printHelp();
                    default -> System.out.println("Invalid option. Type '6' for help.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void printHelp() {
        System.out.println("""
            Available commands:
             1. List games
             2. Create a new game
             3. Join a game as a player
             4. Observe a game
             5. Logout
             6. Help - show this menu
        """);
    }

    private void listGames() throws Exception {
        games = server.listGames(authToken);
        System.out.println("\n--- Current Games ---");
        if (games.isEmpty()) {
            System.out.println("No games available.");
        } else {
            for (int i = 0; i < games.size(); i++) {
                GameData g = games.get(i);
                System.out.printf("%d. %s | White: %s | Black: %s%n",
                        i + 1,
                        g.gameName(),
                        g.whiteUsername() == null ? "(open)" : g.whiteUsername(),
                        g.blackUsername() == null ? "(open)" : g.blackUsername());
            }
        }
    }

    private void createGame() throws Exception {
        System.out.print("Enter game name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Game name cannot be empty.");
            return;
        }
        int gameID = server.createGame(authToken, name);
        System.out.println("Game created successfully: " + name);
    }

    private void joinGame(boolean observer) throws Exception {
        if (games.isEmpty()) {
            System.out.println("You must list games first.");
            return;
        }

        System.out.print("Enter game number: ");
        int index;
        try {
            index = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (Exception e) {
            System.out.println("Please enter a valid number.");
            return;
        }

        if (index < 0 || index >= games.size()) {
            System.out.println("Invalid game number.");
            return;
        }

        int gameID = games.get(index).gameID();
        if (!observer) {
            System.out.print("Join as (WHITE/BLACK): ");
            String color = scanner.nextLine().trim().toUpperCase();
            server.joinGame(authToken, gameID, color);
        } else {
            server.joinGame(authToken, gameID, null);
        }
        System.out.println("Joined game successfully.");
        GameData gameData = games.get(index);
        var chessGame = gameData.game();
        new BoardDrawer(chessGame).printBoard(null, null);
    }
}
