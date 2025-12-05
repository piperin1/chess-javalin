package server;

import dataaccess.*;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsMessageContext;
import model.*;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import com.google.gson.Gson;
import model.GameData;
import service.*;
import websocket.commands.*;
import websocket.messages.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketHandler {

    private final UserService userService;
    private final GameService gameService;
    private final Gson gson = new Gson();
    private final Map<Integer, Set<WsContext>> gameSessions = new ConcurrentHashMap<>();
    private final Map<WsContext, String> sessionToUsername = new ConcurrentHashMap<>();
    private final Map<WsContext, Integer> sessionToGameID = new ConcurrentHashMap<>();

    public WebsocketHandler(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    public void register(Javalin javalin) {
        javalin.ws("/ws", ws -> {

            ws.onMessage(this::handleMessage);

            ws.onClose(this::handleClose);

            ws.onError(ctx -> {
                Throwable t = ctx.error();
                System.out.println("WebSocket error: " +
                        (t == null ? "unknown" : t.getMessage()));
            });
        });
    }


    private void handleMessage(WsMessageContext ctx) {
        String message = ctx.message();
        UserGameCommand base = gson.fromJson(message, UserGameCommand.class);

        try {
            switch (base.getCommandType()) {
                case CONNECT ->
                        handleConnect(gson.fromJson(message, ConnectCommand.class), ctx);
                case MAKE_MOVE ->
                        handleMakeMove(gson.fromJson(message, MakeMoveCommand.class), ctx);
                case LEAVE ->
                        handleLeave(gson.fromJson(message, LeaveCommand.class), ctx);
                case RESIGN ->
                        handleResign(gson.fromJson(message, ResignCommand.class), ctx);
            }
        } catch (UnauthorizedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            send(ctx, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private void handleClose(WsCloseContext ctx) {
        Integer gameID = sessionToGameID.remove(ctx);
        String username = sessionToUsername.remove(ctx);

        if (gameID == null) return;

        Set<WsContext> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(ctx);
            if (username != null) {
                broadcast(gameID,
                        new NotificationMessage(username + " disconnected"),
                        ctx);
            }
        }
    }


    private void handleConnect(ConnectCommand command, WsContext ctx) {
        try {
            AuthData auth = userService.authenticate(command.getAuthToken());
            String username = auth.username();
            GameData game = gameService.getGame(command.getAuthToken(), command.getGameID());
            if (game == null) {
                send(ctx, new ErrorMessage("Error: Game not found"));
                return;
            }
            sessionToUsername.put(ctx, username);
            sessionToGameID.put(ctx, command.getGameID());
            gameSessions.computeIfAbsent(command.getGameID(), k -> ConcurrentHashMap.newKeySet()).add(ctx);
            send(ctx, new LoadGameMessage(game.game()));
            broadcast(command.getGameID(), new NotificationMessage(buildJoinMessage(username, game)), ctx);
        } catch (UnauthorizedException e) {
            send(ctx, new ErrorMessage("Error: " + e.getMessage()));
        } catch (Exception e) {
            send(ctx, new ErrorMessage("Error: Internal server error"));
        }
    }

    private void handleMakeMove(MakeMoveCommand command, WsContext ctx) {
        try {
            Integer gameID = sessionToGameID.get(ctx);
            if (gameID == null) {
                send(ctx, new ErrorMessage("Error: Not connected to a game"));
                return;
            }
            String username = sessionToUsername.get(ctx);
            userService.authenticate(command.getAuthToken());
            gameService.makeMove(command.getAuthToken(), gameID, command.getMove());
            GameData updatedGame = gameService.getGame(command.getAuthToken(), gameID);
            broadcast(gameID, new LoadGameMessage(updatedGame.game()), null);
            broadcast(gameID, new NotificationMessage(username + " made a move"), ctx);
        } catch (UnauthorizedException e) {
            send(ctx, new ErrorMessage("Error: " + e.getMessage()));
        } catch (Exception e) {
            send(ctx, new ErrorMessage("Error: Invalid move"));
        }
    }

    private void handleLeave(LeaveCommand command, WsContext ctx) throws UnauthorizedException, DataAccessException {
        Integer gameID = sessionToGameID.remove(ctx);
        String username = sessionToUsername.remove(ctx);
        if (gameID == null) {
            return;
        }
        Set<WsContext> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(ctx);
        }
        GameData game = gameService.getGameWithoutAuth(gameID);
        boolean updated = false;
        if (game.whiteUsername() != null && game.whiteUsername().equals(username)) {
            game = new GameData(gameID, null, game.blackUsername(), game.gameName(), game.game());
            updated = true;
        }
        if (game.blackUsername() != null && game.blackUsername().equals(username)) {
            game = new GameData(gameID, game.whiteUsername(), null, game.gameName(), game.game());
            updated = true;
        }
        if (updated) {
            gameService.updateGameWithoutAuth(gameID, game);
        }
        broadcast(gameID, new NotificationMessage(username + " left the game"), ctx);
    }

    private void handleResign(ResignCommand command, WsContext ctx) {
        try {
            Integer gameID = sessionToGameID.get(ctx);
            String username = sessionToUsername.get(ctx);
            if (gameID == null) {
                send(ctx, new ErrorMessage("Error: Not connected to a game"));
                return;
            }
            userService.authenticate(command.getAuthToken());
            gameService.resignGame(command.getAuthToken(), gameID);
            broadcast(gameID, new NotificationMessage(username + " resigned the game"), null);
        } catch (UnauthorizedException | DataAccessException e) {
            send(ctx, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private void send(WsContext ctx, ServerMessage msg) {
        ctx.send(gson.toJson(msg));
    }

    private void broadcast(int gameID, ServerMessage msg, WsContext exclude) {
        for (WsContext s : gameSessions.getOrDefault(gameID, Set.of())) {
            if (exclude == null || !s.equals(exclude)) {
                send(s, msg);
            }
        }
    }

    private String buildJoinMessage(String username, GameData game) {
        if (username.equals(game.whiteUsername())) {
            return username + " joined as WHITE";
        }
        if (username.equals(game.blackUsername())) {
            return username + " joined as BLACK";
        }
        return username + " is observing the game";
    }
}