package ui;

import network.ServerFacade;

import java.util.Scanner;

public class ChessClient {
    private final ServerFacade server;
    private String authToken = null;

    public ChessClient(String serverURL) {
        this.server = new ServerFacade(serverURL);
    }

    public void run() {
        var scanner = new Scanner(System.in);
        var currentMenu = new PreloginUI(scanner,server);
        while (true) {
            if (authToken == null) {
                authToken = currentMenu.handleInput();
                if (authToken != null) {
                    currentMenu = new PostloginUI(scanner,server,authToken);
                }
            } else {
                String newAuth = currentMenu.handleInput();
                if (newAuth == null) {
                    currentMenu = new PreloginUI(scanner,server);
                    authToken = null;
                }
            }
        }
    }

    public static void main(String[] args) {
        new ChessClient("https://localhost:8080").run();
    }
}
