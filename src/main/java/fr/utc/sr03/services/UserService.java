package fr.utc.sr03.services;

import fr.utc.sr03.model.Users;
import fr.utc.sr03.repository.PasswordResetTokenRepository;
import fr.utc.sr03.repository.UserRepository;
import jakarta.annotation.Resource;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Resource
    private UserRepository userRepository;

    // CREATE or UPDATE
    public void saveUser(Users user) {
        userRepository.save(user);
    }

    public void changePassword(Users user, String newPassword) {
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        userRepository.save(user);
    }

    // READ
    public Users getUserById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    public Users getUserByEmailAddress(String emailAddress) {
        return userRepository.findByEmailAddress(emailAddress);
    }

    public Page<Users> getUsers(boolean active, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastname").ascending());
        String s = "%" + (search == null ? "" : search.trim()) + "%"; // Si on cherche un nom/prenom particulier, on trim la chaîne de caractères et on wrap avec des % pour faire un "LIKE" en SQL
        return userRepository.findByActiveAndSearch(active, s, pageable);
    }

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    // SEARCH
    public List<Users> searchUsers(String query) {
        String s = "%" + query + "%";
        return userRepository.searchUsers(s);
    }

    public Users findByCredentials(String emailAddress, String password) {
        Users user = userRepository.findByEmailAddress(emailAddress);
        if (user == null) {
            return null;
        }
        try {
            if (BCrypt.checkpw(password, user.getPassword())) {
                return user;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification du mdp de " + emailAddress + " : " + e.getMessage());
        }
        return null;
    }

    // DELETE
    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void deleteUserByEmail(String emailAddress) {
        userRepository.deleteUserByEmail(emailAddress);
    }
}
