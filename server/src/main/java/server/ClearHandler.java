package server;

import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.GameService;
import service.UserService;

public class ClearHandler {
    private final UserService userService;
    private final GameService gameService;

    public ClearHandler(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    public void handle(Context ctx) {
        try {
            userService.clear();
            gameService.clear();
            ctx.status(200);
            ctx.result("");
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result("Error clearing database: " + e.getMessage());
        }
    }

}

