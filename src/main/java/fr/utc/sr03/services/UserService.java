package fr.utc.sr03.services;

import fr.utc.sr03.model.Users;
import fr.utc.sr03.repository.UserRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Resource
    private UserRepository userRepository;

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

}
