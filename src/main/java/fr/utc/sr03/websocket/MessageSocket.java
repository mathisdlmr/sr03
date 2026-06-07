package fr.utc.sr03.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageSocket {

    private String type; // "text", "image", "system", "user_list"
    private String user;
    private int userId;
    private String avatar;
    private String message;
    private String imageData; // Si le type envoyé est "image"
    private long timestamp;
    private List<ConnectedUser> connectedUsers;

    public MessageSocket() {}

    public static MessageSocket system(String message) {
        MessageSocket msg = new MessageSocket();
        msg.setType("system");
        msg.setMessage(message);
        msg.setTimestamp(System.currentTimeMillis());
        return msg;
    }

    public static MessageSocket userList(List<ConnectedUser> users) {
        MessageSocket msg = new MessageSocket();
        msg.setType("user_list");
        msg.setConnectedUsers(users);
        msg.setTimestamp(System.currentTimeMillis());
        return msg;
    }

    // --- Getters & Setters ---

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<ConnectedUser> getConnectedUsers() {
        return connectedUsers;
    }

    public void setConnectedUsers(List<ConnectedUser> connectedUsers) {
        this.connectedUsers = connectedUsers;
    }

    public record ConnectedUser(int id, String firstname, String lastname, String avatar) {}
}
