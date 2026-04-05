package fr.utc.sr03.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "Chat")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatid")
    private int chatId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "creationdate")
    private Timestamp creationdate;

    @Column(name = "validityend")
    private Timestamp validityend;

    @ManyToOne
    @JoinColumn(name = "creator")
    private Users creator;

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(Timestamp creationdate) {
        this.creationdate = creationdate;
    }

    public Timestamp getValidityend() {
        return validityend;
    }

    public void setValidityend(Timestamp validityend) {
        this.validityend = validityend;
    }

    public Users getCreator() {
        return creator;
    }

    public void setCreator(Users creator) {
        this.creator = creator;
    }
}