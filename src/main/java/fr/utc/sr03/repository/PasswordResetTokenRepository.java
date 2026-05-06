package fr.utc.sr03.repository;

import fr.utc.sr03.model.PasswordResetToken;
import fr.utc.sr03.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    PasswordResetToken findByToken(String token);
    PasswordResetToken findByUser(Users user);
}
