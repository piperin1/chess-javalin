package ui;

import chess.ChessGame;
import chess.ChessMove;
import network.WebsocketCommunicator;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;

import java.util.Scanner;

public class InGameUI {

    private final Scanner scanner;
    private final WebsocketCommunicator websocket;

    public InGameUI(Scanner scanner, WebsocketCommunicator websocket) {
        this.scanner = scanner;
        this.websocket = websocket;

        websocket.setOnGame(this::onGameUpdate);
        websocket.setOnMessage(this::onNotification);
    }

    public void run() {
        while (true) {
            System.out.print("[IN-GAME] >>> ");
            String input = scanner.nextLine().trim();
            handleInput(input);
        }
    }

    private void onGameUpdate(ChessGame game) {
       // BoardDrawer.draw(game); Change this in board drawer
    }

    private void onNotification(String message) {
        System.out.println("\n" + message);
    }

    private void handleInput(String input) {
 
    }

}

