package fr.utc.sr03.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "invitation") // TODO : trouver un moyen de spécifier l'unicité du couple (chat, user)
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Column(name = "invitation_date")
    private Timestamp invitationDate;

    // ===================
    // Getters and Setters
    // ===================
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Timestamp getInvitationDate() {
        return invitationDate;
    }

    public void setInvitationDate(Timestamp invitationDate) {
        this.invitationDate = invitationDate;
    }
}