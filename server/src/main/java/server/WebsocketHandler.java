package server;

import com.google.gson.Gson;
import jakarta.websocket.*;
import jakarta.websocket.server.*;
import model.*;
import service.GameService;
import service.UserService;
import websocket.commands.*;
import websocket.messages.*;
import java.awt.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import dataaccess.*;


@ServerEndpoint("/ws")
public class WebsocketHandler {

    private final UserService userService;
    private final GameService gameService;

    private final Map<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();
    private final Map<Session, String> sessionToUsername = new ConcurrentHashMap<>();
    private final Map<Session, Integer> sessionToGameID = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public WebsocketHandler(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    @OnOpen
    public void onOpen(Session session){

    }

    @OnMessage
    public void onMessage(String message, Session session) {
        UserGameCommand base = gson.fromJson(message, UserGameCommand.class);

        switch (base.getCommandType()) {
            case CONNECT -> {
                ConnectCommand cmd =
                        gson.fromJson(message, ConnectCommand.class);
                        handleConnect(cmd, session);
            }
            case MAKE_MOVE -> {
                MakeMoveCommand cmd =
                        gson.fromJson(message, MakeMoveCommand.class);
                        handleMakeMove(cmd, session);
            }
            case LEAVE -> {
                LeaveCommand cmd =
                        gson.fromJson(message, LeaveCommand.class);
                        handleLeave(cmd, session);
            }
            case RESIGN -> {
                ResignCommand cmd =
                        gson.fromJson(message, ResignCommand.class);
                        handleResign(cmd, session);
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        Integer gameID = sessionToGameID.remove(session);
        String username = sessionToUsername.remove(session);
        if (gameID != null) {
            Set<Session> sessions = gameSessions.get(gameID);
            if (sessions != null) {
                sessions.remove(session);
                if (username != null) {
                    broadcast(
                            gameID,
                            new NotificationMessage(username + " disconnected"),
                            session
                    );
                }
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error){}

    public void handleConnect(ConnectCommand command, Session session) {
        try {
            AuthData auth = userService.authenticate(command.getAuthToken());
            String username = auth.username();

            GameData game = gameService.getGame(command.getAuthToken(), command.getGameID());
            if (game == null) {
                send(session, new ErrorMessage("Error: Game not found"));
                return;
            }
            sessionToUsername.put(session, username);
            sessionToGameID.put(session, command.getGameID());
            gameSessions
                    .computeIfAbsent(command.getGameID(), k -> ConcurrentHashMap.newKeySet())
                    .add(session);
            send(session, new LoadGameMessage(game.game()));
            broadcast(
                    command.getGameID(),
                    new NotificationMessage(buildJoinMessage(username, game)),
                    session
            );

        } catch (UnauthorizedException e) {
            send(session, new ErrorMessage("Error: " + e.getMessage()));
        } catch (Exception e) {
            send(session, new ErrorMessage("Error: Internal server error"));
        }
    }


    public void handleMakeMove(MakeMoveCommand command, Session session) {
        try {
            Integer gameID = sessionToGameID.get(session);
            if (gameID == null) {
                send(session, new ErrorMessage("Error: Not connected to a game"));
                return;
            }

            String username = sessionToUsername.get(session);
            userService.authenticate(command.getAuthToken());
            gameService.makeMove(command.getAuthToken(), gameID, command.getMove());
            GameData updatedGame =
                    gameService.getGame(command.getAuthToken(), gameID);
            broadcast(gameID, new LoadGameMessage(updatedGame.game()), null);
            broadcast(
                    gameID,
                    new NotificationMessage(username + " made a move"),
                    session
            );

        } catch (UnauthorizedException e) {
            send(session, new ErrorMessage("Error: " + e.getMessage()));
        } catch (Exception e) {
            send(session, new ErrorMessage("Error: Invalid move"));
        }
    }

    public void handleLeave(LeaveCommand command, Session session) {
        Integer gameID = sessionToGameID.remove(session);
        String username = sessionToUsername.remove(session);
        if (gameID == null) {
            return;
        }
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(session);
        }
        broadcast(
                gameID,
                new NotificationMessage(username + " left the game"),
                session
        );
    }

    public void handleResign(ResignCommand command, Session session) {
        try {
            Integer gameID = sessionToGameID.get(session);
            String username = sessionToUsername.get(session);

            if (gameID == null) {
                send(session, new ErrorMessage("Error: Not connected to a game"));
                return;
            }
            userService.authenticate(command.getAuthToken());
            gameService.resignGame(command.getAuthToken(), gameID);
            broadcast(
                    gameID,
                    new NotificationMessage(username + " resigned the game"),
                    null
            );

        } catch (UnauthorizedException | DataAccessException e ) {
            send(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private void send(Session session, ServerMessage message) {
        try {
            session.getBasicRemote().sendText(gson.toJson(message));
        } catch (Exception ignored) {}
    }

    private void broadcast(int gameID, ServerMessage message, Session exclude) {
        for (Session s : gameSessions.getOrDefault(gameID, Set.of())) {
            if (!s.equals(exclude)) {
                send(s, message);
            }
        }
    }

    private String buildJoinMessage(String username, GameData game) {
        if (username.equals(game.whiteUsername())) {
            return username + " joined as WHITE";
        } else if (username.equals(game.blackUsername())) {
            return username + " joined as BLACK";
        }
        return username + " is observing the game";
    }

}
