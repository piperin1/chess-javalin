package server;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import io.javalin.http.Context;
import model.GameData;
import service.GameService;

import java.util.Collection;
import java.util.Map;

public class GameHandler {
    private final GameService gameService;
    private static final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void listGames(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            if (authToken == null || authToken.isEmpty()) {
                ctx.status(400)
                        .result(gson.toJson(Map.of("message", "Error: Missing Authorization header")));
                return;
            }

            Collection<GameData> games = gameService.listGames(authToken).values();
            ctx.status(200)
                    .result(gson.toJson(Map.of("games", games)));

        } catch (UnauthorizedException e) {
            ctx.status(401)
                    .result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (DataAccessException e) {
            ctx.status(500)
                    .result(gson.toJson(Map.of("message", "Error: Internal server error")));
        }
    }

    public void createGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            if (authToken == null || authToken.isEmpty()) {
                ctx.status(400)
                        .result(gson.toJson(Map.of("message", "Error: Missing Authorization header")));
                return;
            }
            Map<String, String> body = gson.fromJson(ctx.body(), Map.class);
            String gameName = body.get("gameName");
            if (gameName == null || gameName.isEmpty()) {
                ctx.status(400)
                        .result(gson.toJson(Map.of("message", "Error: Missing game name")));
                return;
            }
            int gameID = gameService.createGame(authToken, gameName);
            ctx.status(200)
                    .result(gson.toJson(Map.of("gameID", gameID)));

        } catch (UnauthorizedException e) {
            ctx.status(401)
                    .result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (DataAccessException e) {
            ctx.status(500)
                    .result(gson.toJson(Map.of("message", "Error: Internal server error")));
        }
    }

    public void joinGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            if (authToken == null || authToken.isEmpty()) {
                ctx.status(401)
                        .result(gson.toJson(Map.of("message", "Error: Missing Authorization header")));
                return;
            }

            JoinGameRequest joinReq = gson.fromJson(ctx.body(), JoinGameRequest.class);
            if (joinReq == null || joinReq.playerColor == null || joinReq.gameID == 0) {
                ctx.status(400)
                        .result(gson.toJson(Map.of("message", "Error: Missing required fields")));
                return;
            }

            gameService.joinGame(authToken, joinReq.playerColor.toUpperCase(), joinReq.gameID);
            ctx.status(200)
                    .result(gson.toJson(Map.of()));

        } catch (UnauthorizedException e) {
            ctx.status(401)
                    .result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (AlreadyTakenException e) {
            ctx.status(403)
                    .result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (DataAccessException e) {
            ctx.status(500)
                    .result(gson.toJson(Map.of("message", "Error: Internal server error")));
        } catch (IllegalArgumentException e) {
            ctx.status(400)
                    .result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private static class JoinGameRequest {
        String playerColor;
        int gameID;
    }
}


