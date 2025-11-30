package server;

import com.google.gson.Gson;
import jakarta.websocket.*;
import jakarta.websocket.server.*;
import model.*;
import service.GameService;
import service.UserService;
import websocket.commands.UserGameCommand;
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
    public void onMessage(String message, Session session){
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT -> handleConnect(command, session);
            case MAKE_MOVE -> handleMakeMove(command, session);
            case LEAVE -> handleLeave(command, session);
            case RESIGN -> handleResign(command, session);
        }
    }

    @OnClose
    public void onClose(Session session){}

    @OnError
    public void onError(Session session, Throwable error){}

    public void handleConnect(UserGameCommand command, Session session) {
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


    public void handleMakeMove(UserGameCommand command, Session session) {

    }

    public void handleLeave(UserGameCommand command, Session session) {

    }

    public void handleResign(UserGameCommand command, Session session) {

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
