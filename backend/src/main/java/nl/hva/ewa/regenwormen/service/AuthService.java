package nl.hva.ewa.regenwormen.service;

import nl.hva.ewa.regenwormen.entity.UserEntity;
import nl.hva.ewa.regenwormen.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity register(String email, String username, String password) {
        if (email == null || username == null || password == null) {
            throw new IllegalArgumentException("email, username, and password are required");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }

        UserEntity user = new UserEntity(
                UUID.randomUUID().toString(),
                email,
                passwordEncoder.encode(password),
                username,
                null,
                null
        );

        return userRepository.save(user);
    }


    public UserEntity login(String identifier, String password) {
        if (identifier == null || password == null) {
            throw new IllegalArgumentException("identifier and password are required");
        }

        Optional<UserEntity> userOpt = userRepository.findByEmail(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsername(identifier);
        }

        UserEntity user = userOpt
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return user;
    }
}
