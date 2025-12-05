package ui;

import chess.*;
import com.google.gson.Gson;
import network.WebsocketCommunicator;
import websocket.commands.*;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class InGameUI {

    private final Scanner scanner;
    private final WebsocketCommunicator websocket;
    private final BoardDrawer boardDrawer = new BoardDrawer();
    private final ChessGame.TeamColor pov;
    private final Gson gson = new Gson();
    private final int gameID;
    private final String authToken;
    private boolean running;
    private ChessGame currentGame;


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
        this.currentGame = game;
        clearScreen();
        boardDrawer.draw(game, pov);
        System.out.print("[IN-GAME] >>> \n");
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void onNotification(String message) {
        System.out.print("\n" + message + "\n[IN-GAME] >>>\n ");
    }

    private void handleInput(String input) {
        switch (input.split(" ")[0]) {
            case "move" -> {
                try {
                    ChessMove move = parseMove(input);
                    websocket.send(gson.toJson(new MakeMoveCommand(authToken, gameID, move)));
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }
            case "resign" -> websocket.send(gson.toJson(new ResignCommand(authToken, gameID)));
            case "leave" -> {
                websocket.send(gson.toJson(new LeaveCommand(authToken, gameID)));
                running = false;
            }
            case "redraw" -> {
                clearScreen();
                boardDrawer.draw(currentGame, pov);
            }
            case "highlight" -> {
                try {
                    String[] parts = input.split("\\s+");
                    if (parts.length != 2) {
                        System.out.println("Usage: highlight <square>");
                        break;
                    }

                    ChessPosition pos = parsePosition(parts[1]);
                    highlight(pos);
                } catch (Exception e) {
                    System.out.println("Invalid square.");
                }
            }

            case "help" -> printHelp();
            default -> System.out.println("Unknown command. Type HELP.");
        }
    }

    private void highlight(ChessPosition pos) {
        if (currentGame == null) {
            System.out.println("No board to highlight.");
            return;
        }
        ChessPiece piece = currentGame.getBoard().getPiece(pos);
        if (piece == null) {
            System.out.println("No piece at that square.");
            return;
        }
        Set<ChessPosition> highlights = new HashSet<>();
        highlights.add(pos);
        for (ChessMove move : currentGame.validMoves(pos)) {
            highlights.add(move.getEndPosition());
        }
        clearScreen();
        boardDrawer.printBoard(currentGame, pov, highlights);
        System.out.print("[IN-GAME] >>> \n" );
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
          redraw
          highlight <piece>
          resign
          leave
          help
    """);
    }
}

