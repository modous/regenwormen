package nl.hva.ewa.regenwormen.service;

import nl.hva.ewa.regenwormen.domain.User;
import nl.hva.ewa.regenwormen.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getById(String id) {
        return userRepository.findById(id).orElseThrow();
    }

    public User register(String email, String username, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already in use");
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, username, encodedPassword);
        return userRepository.save(user);
    }

    public boolean login(String identifier, String password) {
        Optional<User> userOpt = userRepository.findByEmail(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsername(identifier);
        }
        return userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword());
    }
}
