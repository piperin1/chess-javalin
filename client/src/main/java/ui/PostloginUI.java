package ui;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import network.HttpCommunicator;
import network.WebsocketCommunicator;
import websocket.commands.ConnectCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PostloginUI {
    private final Scanner scanner;
    private final HttpCommunicator http;
    private List<GameData> games = List.of();

    public PostloginUI(Scanner scanner, HttpCommunicator http) {
        this.scanner = scanner;
        this.http = http;
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

            switch (choice) {
                case "1" -> listGames();
                case "2" -> createGame();
                case "3" -> joinGame(false);
                case "4" -> joinGame(true);
                case "5" -> {
                    http.logout();
                    System.out.println("You have been logged out.");
                    running = false;
                }
                case "6" -> printHelp();
                default -> System.out.println("Invalid option. Type '6' for help.");
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
             6. Help
        """);
    }

    private void listGames() {
        games = new ArrayList<>(http.listGames());
        System.out.println("\n--- Current Games ---");
        if (games.isEmpty()) {
            System.out.println("No games available.");
            return;
        }

        for (int i = 0; i < games.size(); i++) {
            GameData g = games.get(i);
            System.out.printf("%d. %s | White: %s | Black: %s%n",
                    i + 1,
                    g.gameName(),
                    g.whiteUsername() == null ? "(open)" : g.whiteUsername(),
                    g.blackUsername() == null ? "(open)" : g.blackUsername());
        }
    }

    private void createGame() {
        System.out.print("Enter game name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Game name cannot be empty.");
            return;
        }

        int gameID = http.createGame(name);
        if (gameID < 0) {
            System.out.println("Failed to create game.");
            return;
        }
        System.out.println("Game created successfully.");
    }

    private void joinGame(boolean observer) {
        if (games.isEmpty()) {
            System.out.println("You must list games first.");
            return;
        }
        System.out.print("Enter game number: ");
        int index;
        try {
            index = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (Exception e) {
            System.out.println("Invalid number.");
            return;
        }
        if (index < 0 || index >= games.size()) {
            System.out.println("Invalid game number.");
            return;
        }
        GameData game = games.get(index);
        int gameID = game.gameID();
        ChessGame.TeamColor pov;

        if (!observer) {
            System.out.print("Join as (WHITE/BLACK): ");
            String colorStr = scanner.nextLine().trim().toUpperCase();
            if (!colorStr.equals("WHITE") && !colorStr.equals("BLACK")) {
                System.out.println("Invalid color.");
                return;
            }
            pov = ChessGame.TeamColor.valueOf(colorStr);
            if (!http.joinGame(gameID, colorStr)) {
                System.out.println("Failed to join game.");
                return;
            }
        } else {
            http.joinGame(gameID, "EMPTY");
            pov = ChessGame.TeamColor.WHITE;
        }
        System.out.println("Joined game successfully.");

        try {
            WebsocketCommunicator websocket =
                    new WebsocketCommunicator("ws://localhost:8080/connect", http.getAuthToken(), gameID);
            InGameUI inGameUI =
                    new InGameUI(scanner, websocket, gameID, http.getAuthToken(), pov);

            inGameUI.run();
        } catch (Exception e) {
            System.out.println("Failed to connect to game.");
        }
    }
}
