package fr.utc.sr03.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "firstname", nullable = false)
    private String firstname;

    @Column(name = "lastname", nullable = false)
    private String lastname;

    @Column(name = "mail", nullable = false, unique = true)
    private String mail;

    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String password;

    @Column(name = "admin")
    private boolean admin;

    @Column(name = "active")
    private boolean active;

    @Column(name = "avatar", columnDefinition = "TEXT")
    @JsonIgnore
    private String avatar;

    @OneToMany(mappedBy = "creator")
    @JsonIgnore
    private List<Chat> createdChats;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Invitation> invitations;

    // ===================
    // Getters and Setters
    // ===================
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public List<Chat> getCreatedChats() {
        return createdChats;
    }

    public void setCreatedChats(List<Chat> createdChats) {
        this.createdChats = createdChats;
    }

    public List<Invitation> getInvitations() {
        return invitations;
    }

    public void setInvitations(List<Invitation> invitations) {
        this.invitations = invitations;
    }

    // On définit un DTO pour choisir expréssément les champs que l'on veut exposer de l'entité Users à travers l'API
    // (notamment pour ne pas exposer le mot de passe hashé, ou tout autre champ qui serait ajouté à postériori)
    public record UserDTO(int id, String firstname, String lastname, String mail, boolean admin, String avatar) {
        public static UserDTO from(Users user) {
            return new UserDTO(user.getId(), user.getFirstname(), user.getLastname(), user.getMail(), user.isAdmin(), user.getAvatar());
        }
    }
}