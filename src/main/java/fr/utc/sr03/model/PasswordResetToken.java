package fr.utc.sr03.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class PasswordResetToken { // Code récupéré et adapté de : https://www.baeldung.com/spring-security-registration-i-forgot-my-password

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String token;

    @OneToOne
    @JoinColumn(nullable = false, name = "user_id")
    private Users user;

    private Date expiryDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}
