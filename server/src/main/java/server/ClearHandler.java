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

    public void handle(Context ctx) throws DataAccessException {
        userService.clear();
        gameService.clear();
        ctx.status(200);
        ctx.result(""); // Empty body
    }
}

