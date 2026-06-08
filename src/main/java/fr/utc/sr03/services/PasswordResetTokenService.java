package fr.utc.sr03.services;

import fr.utc.sr03.model.PasswordResetToken;
import fr.utc.sr03.model.Users;
import fr.utc.sr03.repository.PasswordResetTokenRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

// Code récupéré et adapté de : https://www.baeldung.com/spring-security-registration-i-forgot-my-password
// Chaque fois qu'un utilisateur demande une réinitialisation de mot de passe, on crée un token associé à cet utilisateur et on l'enregistre en base
// Ce token a un TTL de 5 minutes (après quoi il expire et ne peut plus être utilisé)

@Service
public class PasswordResetTokenService {

    @Resource
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    public void createPasswordResetTokenForUser(Users user, String token) {
        PasswordResetToken existing = passwordResetTokenRepository.findByUser(user);
        if (existing != null) {
            passwordResetTokenRepository.delete(existing);
        }
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(new Date(System.currentTimeMillis() + 5 * 60 * 1000L));
        passwordResetTokenRepository.save(resetToken);
    }

    public boolean validatePasswordResetToken(String token) {
        PasswordResetToken passToken = passwordResetTokenRepository.findByToken(token);
        return passToken != null && !passToken.getExpiryDate().before(new Date());
    }

    public Users getUserByToken(String token) {
        PasswordResetToken passToken = passwordResetTokenRepository.findByToken(token);
        return passToken != null ? passToken.getUser() : null;
    }

    public void deletePasswordResetToken(String token) {
        PasswordResetToken passToken = passwordResetTokenRepository.findByToken(token);
        if (passToken != null) {
            passwordResetTokenRepository.delete(passToken);
        }
    }
}
