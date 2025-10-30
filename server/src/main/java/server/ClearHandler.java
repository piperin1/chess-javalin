package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.GameService;
import service.UserService;

import java.util.Map;

public class ClearHandler {
    private final UserService userService;
    private final GameService gameService;
    private final Gson gson = new Gson();
    public ClearHandler(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    public void handle(Context ctx) {
        try {
            userService.clear();
            gameService.clear();
            ctx.status(200);
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: Internal server error")));
        }
    }

}

