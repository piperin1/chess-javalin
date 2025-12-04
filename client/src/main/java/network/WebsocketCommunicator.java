package network;

import chess.ChessGame;
import com.google.gson.Gson;
import websocket.commands.ConnectCommand;
import websocket.messages.LoadGameMessage;
import websocket.messages.MessageText;
import websocket.messages.ServerMessage;
import jakarta.websocket.*;

import java.net.URI;
import java.util.function.Consumer;

@ClientEndpoint
public class WebsocketCommunicator {

    private Session session;
    private final Gson gson = new Gson();

    private Consumer<ChessGame> onGame;
    private Consumer<String> onMessage;

    private final String authToken;
    private final int gameID;

    public WebsocketCommunicator(
            String serverUrl,
            String authToken,
            int gameID
    ) throws Exception {

        this.authToken = authToken;
        this.gameID = gameID;

        URI uri = new URI(serverUrl);
        System.out.println("Attempting to connect to websocket using: " + uri);
        WebSocketContainer container =
                ContainerProvider.getWebSocketContainer();

        container.connectToServer(this, uri);
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("WebSocket connected");

        send(gson.toJson(
                new ConnectCommand(authToken, gameID)));
    }

    @OnMessage
    public void onMessage(String message) {
        ServerMessage base =
                gson.fromJson(message, ServerMessage.class);

        switch (base.getServerMessageType()) {
            case LOAD_GAME -> {
                var lg = gson.fromJson(message, LoadGameMessage.class);
                if (onGame != null) onGame.accept(lg.getGame());
            }
            case ERROR, NOTIFICATION -> {
                var msg = gson.fromJson(message, MessageText.class);
                if (onMessage != null) onMessage.accept(msg.getMessage());
            }
        }
    }
    @OnError
    public void onError(Session session, Throwable t) {
        System.out.println("WebSocket error: " + t.getMessage());
        t.printStackTrace();
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket closed: " + reason);
    }

    public void send(String json) {
        session.getAsyncRemote().sendText(json);
    }

    public void setOnGame(Consumer<ChessGame> handler) {
        this.onGame = handler;
    }

    public void setOnMessage(Consumer<String> handler) {
        this.onMessage = handler;
    }
}


