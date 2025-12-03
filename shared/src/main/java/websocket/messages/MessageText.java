package websocket.messages;

public class MessageText extends ServerMessage {
    private String message;

    public MessageText(ServerMessageType type, String message) {
        super(type);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
