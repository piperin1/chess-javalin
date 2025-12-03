package network;
import model.AuthData;
import model.GameData;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

public class HttpCommunicator {
    private final ServerFacade facade;
    private String authToken;

    public HttpCommunicator(ServerFacade facade) {
        this.facade = facade;
    }

    public boolean register(String username, String password, String email) {
        try {
            AuthData auth = facade.register(username, password, email);
            authToken = auth.authToken();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean login(String username, String password) {
        try {
            AuthData auth = facade.login(username, password);
            authToken = auth.authToken();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean logout() {
        try {
            facade.logout(authToken);
            authToken = null;
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public int createGame(String gameName) {
        try {
            return facade.createGame(authToken, gameName);
        } catch (IOException e) {
            return -1;
        }
    }

    public HashSet<GameData> listGames() {
        try {
            List<GameData> games = facade.listGames(authToken);
            return new HashSet<>(games);
        } catch (IOException e) {
            return new HashSet<>();
        }
    }

    public boolean joinGame(int gameID, String playerColor) {
        try {
            facade.joinGame(authToken, gameID, playerColor);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String getAuthToken() {
        return authToken;
    }

}