package network;

import chess.ChessGame;
import com.google.gson.Gson;
import websocket.messages.LoadGameMessage;
import websocket.messages.MessageText;
import websocket.messages.ServerMessage;
import jakarta.websocket.*;

import java.net.URI;
import java.util.function.Consumer;

@ClientEndpoint
public class WebsocketCommunicator extends Endpoint {

    private Session session;
    private final Gson gson = new Gson();

    private Consumer<ChessGame> onGame;
    private Consumer<String> onMessage;

    public WebsocketCommunicator(String serverUrl) throws Exception {
        URI uri = new URI("ws://" + serverUrl + "/connect");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, uri);
    }

    public void setOnGame(Consumer<ChessGame> handler) {
        this.onGame = handler;
    }

    public void setOnMessage(Consumer<String> handler) {
        this.onMessage = handler;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;

        session.addMessageHandler(String.class, this::handleMessage);
    }

    private void handleMessage(String message) {
        ServerMessage base = gson.fromJson(message, ServerMessage.class);

        switch (base.getServerMessageType()) {

            case LOAD_GAME -> {
                var loadGame = gson.fromJson(message, LoadGameMessage.class);
                if (onGame != null) {
                    onGame.accept(loadGame.getGame());
                }
            }
            case ERROR, NOTIFICATION -> {
                var msg = gson.fromJson(message, MessageText.class);
                if (onMessage != null) {
                    onMessage.accept(msg.getMessage());
                }
            }
        }
    }

    public void send(String json) {
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(json);
        }
    }
}


