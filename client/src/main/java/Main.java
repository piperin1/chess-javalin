import chess.*;
import network.HttpCommunicator;
import network.ServerFacade;
import ui.PostloginUI;
import ui.PreloginUI;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        ServerFacade server = new ServerFacade("http://localhost:8080");
        HttpCommunicator http = new HttpCommunicator(server);

        PreloginUI preloginUI = new PreloginUI(scanner, http);

        while (true) {
            if (preloginUI.handleInput()) {
                new PostloginUI(scanner, http).run();
            }
        }
    }

}


