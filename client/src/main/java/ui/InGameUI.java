package ui;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import network.WebsocketCommunicator;
import websocket.commands.LeaveCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;

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
            String input = scanner.nextLine().trim();
            handleInput(input);
        }
    }

    private void onGameUpdate(ChessGame game) {
        clearScreen();
        boardDrawer.draw(game, pov);
        System.out.print("[IN-GAME] >>> ");
    }

    private void clearScreen() {
        System.out.flush();
    }

    private void onNotification(String message) {
        System.out.print("\n" + message + "\n[IN-GAME] >>> ");
    }

    private void handleInput(String input) {
        switch (input.split(" ")[0]) {
            case "move" -> {
                ChessMove move = parseMove(input); //Add parsemove
                MakeMoveCommand cmd =
                        new MakeMoveCommand(authToken, gameID, move);
                websocket.send(gson.toJson(cmd));
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

