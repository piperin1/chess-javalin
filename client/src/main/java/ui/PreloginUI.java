package ui;

import network.ServerFacade;

import java.util.Scanner;

public class PreloginUI {
    private final Scanner scanner;
    private final ServerFacade server;

    public PreloginUI(Scanner scanner, ServerFacade server) {
        this.scanner = scanner;
        this.server = server;
    }

    public String handleInput() {
       //ADD COMMANDS BEFORE LOGIN
    }

    public void printHelp() {
        //PRINT AVAIL COMMANDS
    }

    private String handleRegister() throws Exception {
        //COLLECT USERNAME + PASSWORD, RETURN AUTHTOKEN
    }

    private String handleLogin() throws Exception {
        //COLLECT USERNAME + PASSWORD, RETURN AUTHTOKEN
    }
}
