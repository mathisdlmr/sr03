package fr.utc.sr03.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "chat")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "starts_at")
    private Timestamp startsAt;

    @Column(name = "ends_at")
    private Timestamp endsAt;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnore
    private Users creator;

    @JsonProperty("creator_id")
    public int getCreatorId() {
        return creator != null ? creator.getId() : 0;
    }

    @OneToMany(mappedBy = "chat")
    @JsonIgnore
    private List<Invitation> invitations;

    // --------------------------------//
    // ----- Getters and Setters ----- //
    // --------------------------------//

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getStartsAt() { return startsAt; }

    public void setStartsAt(Timestamp startsAt) { this.startsAt = startsAt; }

    public Timestamp getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Timestamp endsAt) {
        this.endsAt = endsAt;
    }

    public Users getCreator() {
        return creator;
    }

    public void setCreator(Users creator) {
        this.creator = creator;
    }

    public List<Invitation> getInvitations() {
        return invitations;
    }

    public void setInvitations(List<Invitation> invitations) {
        this.invitations = invitations;
    }
}