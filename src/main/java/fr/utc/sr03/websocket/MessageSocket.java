package fr.utc.sr03.websocket;
import java.sql.Timestamp;

public class MessageSocket {

    private String type;
    private Integer chat;
    private String user;
    private String message;
    private Timestamp msgtimestamp;

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public Integer getChat() { return chat; }

    public void setChat(Integer chat) { this.chat = chat; }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getMsgtimestamp() { return msgtimestamp; }

    public void setMsgtimestamp(Timestamp msgtimestamp) { this.msgtimestamp = msgtimestamp; }
}
