package network;

import com.google.gson.Gson;
import model.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;

public class ServerFacade {
    private final String baseURL;
    private final Gson gson = new Gson();

    public ServerFacade(String baseURL) {
        this.baseURL = baseURL;
    }

    public AuthData register(String username, String password, String email) throws IOException {
        var body = gson.toJson(new UserData(username, password, email));
        return makeRequest("POST", "/user", body, AuthData.class, null);
    }

    public AuthData login(String username, String password) throws IOException {
        var body = gson.toJson(Map.of("username", username, "password", password));
        return makeRequest("POST", "/session", body, AuthData.class, null);
    }

    public void logout(String authToken) throws IOException {
        makeRequest("DELETE", "/session", null, null, authToken);
    }

    public int createGame(String authToken, String gameName) throws IOException {
        var body = gson.toJson(Map.of("gameName", gameName));
        var response = makeRequest("POST", "/game", body, Map.class, authToken);
        return ((Double) response.get("gameID")).intValue();
    }

    public List<GameData> listGames(String authToken) throws IOException {
        var response = makeRequest("GET", "/game", null, Map.class, authToken);
        var gamesJson = gson.toJson(response.get("games"));
        GameData[] games = gson.fromJson(gamesJson, GameData[].class);
        return List.of(games);
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws IOException {
        var body = gson.toJson(Map.of(
                "playerColor", playerColor, // can be "WHITE", "BLACK", or null for observer
                "gameID", gameID
        ));
        makeRequest("PUT", "/game", body, null, authToken);
    }

    public void clear() throws IOException {
        makeRequest("DELETE", "/db", null, null, null);
    }

    private <T> T makeRequest(String method, String path, String body, Class<T> responseType, String authToken) throws IOException {
        var url = new URL(baseURL + path);
        var conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        if (authToken != null) {
            conn.setRequestProperty("Authorization", authToken);
        }
        if (body != null && (method.equals("POST") || method.equals("PUT"))) {
            conn.setDoOutput(true);
            try (var out = new OutputStreamWriter(conn.getOutputStream())) {
                out.write(body);
            }
        }
        int status = conn.getResponseCode();
        if (status != 200) {
            InputStream errorStream = conn.getErrorStream();
            String errorMessage = "Unknown error";

            if (errorStream != null) {
                try (var reader = new InputStreamReader(errorStream)) {
                    Map errJson = gson.fromJson(reader, Map.class);
                    if (errJson != null && errJson.get("message") != null) {
                        errorMessage = (String) errJson.get("message");
                    }
                }
            }
            if (errorMessage.startsWith("Error:")) {
                errorMessage = errorMessage.substring("Error:".length()).trim();
            }
            throw new IOException(errorMessage);
        }
        if (responseType == null) {
            return null;
        }
        try (var reader = new InputStreamReader(conn.getInputStream())) {
            return gson.fromJson(reader, responseType);
        }
    }
}
