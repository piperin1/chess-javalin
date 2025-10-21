package server;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import io.javalin.http.Context;
import model.AuthData;
import model.UserData;
import service.UserService;

import java.util.Map;

public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx) {
        try {
            UserData user = gson.fromJson(ctx.body(), UserData.class);

            if (user.username() == null || user.password() == null || user.email() == null) {
                ctx.status(400)
                        .result(gson.toJson(Map.of("message", "Error: Missing required fields")));
                return;
            }

            AuthData auth = userService.register(user);
            ctx.status(200)
                    .result(gson.toJson(auth));

        } catch (AlreadyTakenException e) {
            ctx.status(403)
                    .result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (DataAccessException e) {
            ctx.status(500)
                    .result(gson.toJson(Map.of("message", "Error: Internal server error")));
        }
    }

    public void login(Context ctx) {
        try {
            UserData user = gson.fromJson(ctx.body(), UserData.class);

            if (user.username() == null || user.password() == null) {
                ctx.status(400)
                        .result(gson.toJson(Map.of("message", "Error: Missing username or password")));
                return;
            }

            AuthData auth = userService.login(user);
            ctx.status(200)
                    .result(gson.toJson(auth));

        } catch (UnauthorizedException e) {
            ctx.status(401)
                    .result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (DataAccessException e) {
            ctx.status(500)
                    .result(gson.toJson(Map.of("message", "Error: Internal server error")));
        }
    }

    public void logout(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            if (authToken == null || authToken.isEmpty()) {
                ctx.status(400)
                        .result(gson.toJson(Map.of("message", "Error: Missing Authorization header")));
                return;
            }

            userService.logout(authToken);
            ctx.status(200)
                    .result(gson.toJson(Map.of()));

        } catch (UnauthorizedException e) {
            ctx.status(401)
                    .result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (DataAccessException e) {
            ctx.status(500)
                    .result(gson.toJson(Map.of("message", "Error: Internal server error")));
        }
    }
}

