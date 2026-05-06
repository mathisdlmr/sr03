package fr.utc.sr03.services;

import fr.utc.sr03.model.PasswordResetToken;
import fr.utc.sr03.model.Users;
import fr.utc.sr03.repository.UserRepository;
import fr.utc.sr03.repository.PasswordResetTokenRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class UserService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private PasswordResetTokenRepository passwordResetTokenRepository;

    // CREATE or UPDATE
    public void saveUser(Users user) {
        userRepository.save(user);
    }

    // READ
    public Users getUserById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    public Users getUserByEmailAddress(String emailAddress) {
        return userRepository.findByEmailAddress(emailAddress);
    }

    public List<Users> getAdminUsersById(Integer userId, Boolean isAdmin) {
        return userRepository.findAdminUsersById(userId, isAdmin);
    }

    public List<Users> getAdminUsersByEmail(String emailAddress, Boolean isAdmin) {
        return userRepository.findAdminUsersByEmail(emailAddress, isAdmin);
    }

    public List<Users> getActiveUsersById(Integer userId, Boolean isActive) {
        return userRepository.findActiveUsersById(userId, isActive);
    }

    public List<Users> getActiveUsersByEmail(String emailAddress, Boolean isActive) {
        return userRepository.findActiveUsersByEmail(emailAddress, isActive);
    }

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    // DELETE
    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

    public void deleteUserByEmail(String emailAddress) {userRepository.deleteUserByEmail(emailAddress);}

    // OTHER METHODS
    public Users findByCredentials(String emailAddress, String password) {
        Users user = userRepository.findByEmailAddress(emailAddress);
        if(BCrypt.checkpw(password, user.getPassword())){ // Le plus adapté que j'ai trouvé : https://dzone.com/articles/hashing-passwords-in-java-with-bcrypt
            return user;
        } else {
            return null;
        }
    }

    public void createPasswordResetTokenForUser(Users user, String token) {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);

        Calendar date = Calendar.getInstance();
        long timeInSecs = date.getTimeInMillis();
        Date expiryDate = new Date(timeInSecs + (5 * 60 * 1000));
        resetToken.setExpiryDate(expiryDate);

        passwordResetTokenRepository.save(resetToken);
    }
}
