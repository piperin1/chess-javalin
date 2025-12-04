package server;

import dataaccess.*;
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
            ws.onMessage((ctx) -> {
                String message = ctx.message(); // Extract raw text
                UserGameCommand base = gson.fromJson(message, UserGameCommand.class);

                try {
                    switch (base.getCommandType()) {
                        case CONNECT -> handleConnect(gson.fromJson(message, ConnectCommand.class), ctx);
                        case MAKE_MOVE -> handleMakeMove(gson.fromJson(message, MakeMoveCommand.class), ctx);
                        case LEAVE -> handleLeave(gson.fromJson(message, LeaveCommand.class), ctx);
                        case RESIGN -> handleResign(gson.fromJson(message, ResignCommand.class), ctx);
                    }
                } catch (Exception e) {
                    send(ctx, new ErrorMessage("Error: " + e.getMessage()));
                }
            });

            ws.onClose((ctx) -> {
                Integer gameID = sessionToGameID.remove(ctx);
                String username = sessionToUsername.remove(ctx);
                if (gameID != null) {
                    Set<WsContext> sessions = gameSessions.get(gameID);
                    if (sessions != null) {
                        sessions.remove(ctx);
                        if (username != null) {
                            broadcast(gameID, new NotificationMessage(username + " disconnected"), ctx);
                        }
                    }
                }
            });

            ws.onError((throwable) -> {
                System.out.println("WebSocket error: " + throwable.getMessage());
            });

        });
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
            gameSessions.computeIfAbsent(command.getGameID(), k -> ConcurrentHashMap.newKeySet())
                    .add(ctx);

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

    private void handleLeave(LeaveCommand command, WsContext ctx) {
        Integer gameID = sessionToGameID.remove(ctx);
        String username = sessionToUsername.remove(ctx);

        if (gameID == null) return;

        Set<WsContext> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(ctx);
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
        if (username.equals(game.whiteUsername())) return username + " joined as WHITE";
        if (username.equals(game.blackUsername())) return username + " joined as BLACK";
        return username + " is observing the game";
    }
}