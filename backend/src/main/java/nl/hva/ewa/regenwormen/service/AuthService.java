package nl.hva.ewa.regenwormen.service;

import nl.hva.ewa.regenwormen.domain.User;
import nl.hva.ewa.regenwormen.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<?> register(Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email and password are required"));
        }
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }
        User user = new User(email, passwordEncoder.encode(password));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User registered", "email", user.getEmail()));
    }

    public ResponseEntity<?> login(Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email and password are required"));
        }
        return userRepository.findByEmail(email)
                .map(u -> passwordEncoder.matches(password, u.getPassword())
                        ? ResponseEntity.ok(Map.of("message", "Login successful"))
                        : ResponseEntity.status(401).body(Map.of("error", "Invalid email or password")))
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Invalid email or password")));
    }
}
