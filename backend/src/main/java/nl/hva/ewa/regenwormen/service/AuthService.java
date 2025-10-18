package nl.hva.ewa.regenwormen.service;

import nl.hva.ewa.regenwormen.domain.User;
import nl.hva.ewa.regenwormen.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ Register new user
    public ResponseEntity<?> register(Map<String, String> body) {
        String email = body.get("email");
        String username = body.get("username");
        String password = body.get("password");

        if (email == null || username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "email, username, and password are required"
            ));
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
        }

        User user = new User(email, username, passwordEncoder.encode(password));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "email", user.getEmail(),
                "username", user.getUsername()
        ));
    }

    // ✅ Login with either email or username
    public ResponseEntity<?> login(Map<String, String> body) {
        String identifier = body.get("identifier");
        String password = body.get("password");

        if (identifier == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "identifier and password are required"
            ));
        }

        Optional<User> userOpt = userRepository.findByEmail(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsername(identifier);
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "email", user.getEmail(),
                "username", user.getUsername()
        ));
    }
}
