import chess.*;
import network.ServerFacade;
import ui.PostloginUI;
import ui.PreloginUI;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var server = new ServerFacade("http://localhost:8080");
        var scanner = new Scanner(System.in);
        var preloginUI = new PreloginUI(scanner, server);

        while (true) {
            String authToken = preloginUI.handleInput();
            if (authToken != null) {
                new PostloginUI(scanner, server, authToken).run();
            }
        }
    }
}
