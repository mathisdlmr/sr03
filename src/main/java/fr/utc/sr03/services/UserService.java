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
    public Users getUserByEmailAddress(String emailAddress) {return userRepository.findByEmailAddress(emailAddress);}

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    // DELETE
    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

}
