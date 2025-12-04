package ui;

import chess.*;
import com.google.gson.Gson;
import network.WebsocketCommunicator;
import websocket.commands.*;

import java.util.Scanner;

public class InGameUI {

    private final Scanner scanner;
    private final WebsocketCommunicator websocket;
    private final BoardDrawer boardDrawer = new BoardDrawer();
    private final ChessGame.TeamColor pov;
    private final Gson gson = new Gson();
    private final int gameID;
    private final String authToken;
    private boolean running;

    public InGameUI(
            Scanner scanner,
            WebsocketCommunicator websocket,
            int gameID,
            String authToken,
            ChessGame.TeamColor pov
    ) {
        this.scanner = scanner;
        this.websocket = websocket;
        this.gameID = gameID;
        this.authToken = authToken;
        this.pov = pov;
        running = false;
        websocket.setOnGame(this::onGameUpdate);
        websocket.setOnMessage(this::onNotification);
    }

    public void run() {
        running = true;
        while (running) {
            System.out.print("[IN-GAME] >>> ");
            String input = scanner.nextLine().trim().toLowerCase();
            handleInput(input);
        }
    }

    private void onGameUpdate(ChessGame game) {
        clearScreen();
        boardDrawer.draw(game, pov);
        System.out.print("[IN-GAME] >>> ");
    }

    private void clearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    private void onNotification(String message) {
        System.out.print("\n" + message + "\n[IN-GAME] >>> ");
    }

    private void handleInput(String input) {
        switch (input.split(" ")[0]) {
            case "move" -> {
                try {
                    ChessMove move = parseMove(input);
                    websocket.send(gson.toJson(
                            new MakeMoveCommand(authToken, gameID, move)));
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }
            case "resign" -> websocket.send(
                    gson.toJson(new ResignCommand(authToken, gameID))
            );
            case "leave" -> {
                websocket.send(
                        gson.toJson(new LeaveCommand(authToken, gameID)));
                running = false;
            }
            case "help" -> printHelp();
            default -> System.out.println("Unknown command. Type HELP.");
        }
    }

    private ChessMove parseMove(String input) {
        String[] parts = input.trim().toLowerCase().split("\\s+");
        if (parts.length < 3 || parts.length > 4) {
            throw new IllegalArgumentException(
                    "Usage: move <from> <to> [promotion]");
        }
        ChessPosition from = parsePosition(parts[1]);
        ChessPosition to = parsePosition(parts[2]);
        ChessPiece.PieceType promo = null;
        if (parts.length == 4) {
            promo = parsePromotion(parts[3]);
        }
        return new ChessMove(from, to, promo);
    }

    private ChessPosition parsePosition(String pos) {
        if (pos.length() != 2) {
            throw new IllegalArgumentException("Invalid square: " + pos);
        }
        char file = pos.charAt(0);
        char rank = pos.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            throw new IllegalArgumentException("Invalid square: " + pos);
        }
        int col = file - 'a' + 1;
        int row = rank - '0';
        return new ChessPosition(row, col);
    }

    private ChessPiece.PieceType parsePromotion(String p) {
        return switch (p) {
            case "q" -> ChessPiece.PieceType.QUEEN;
            case "r" -> ChessPiece.PieceType.ROOK;
            case "b" -> ChessPiece.PieceType.BISHOP;
            case "n" -> ChessPiece.PieceType.KNIGHT;
            default -> throw new IllegalArgumentException(
                    "Invalid promotion piece: " + p);
        };
    }

    public void printHelp(){
        System.out.println("""
        Commands:
          move <from> <to> [promotion]
          resign
          leave
          help
    """);
    }
}

