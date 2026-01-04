package nl.hva.ewa.regenwormen.service;

import nl.hva.ewa.regenwormen.entity.UserEntity;
import nl.hva.ewa.regenwormen.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ Alle users ophalen
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ User ophalen via ID
    public UserEntity getById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    // ✅ Registreren van nieuwe gebruiker
    public UserEntity register(String email, String username, String password) {
        if (email == null || username == null || password == null) {
            throw new IllegalArgumentException("Email, username and password are required");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already in use");
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

    // ✅ Login check
    public UserEntity login(String identifier, String password) {
        if (identifier == null || password == null) {
            throw new IllegalArgumentException("Identifier and password are required");
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

    // ✅ Locatie bijwerken
    public void updateLocation(String id, String location) {
        UserEntity user = getById(id);
        user.setLocation(location);
        userRepository.save(user);
    }

    // ✅ Wachtwoord wijzigen
    public void updatePassword(String id, String rawPassword) {
        UserEntity user = getById(id);
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }

    // ✅ Profielfoto opslaan (GEFIXT: geen localhost hardcoded)
    public String saveProfilePhoto(String id, MultipartFile file) {
        try {
            String folder = "uploads/";
            Files.createDirectories(Paths.get(folder));

            String fileName = id + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(folder + fileName);
            Files.write(filePath, file.getBytes());

            // 🔥 BASE_URL uit environment (Render) of fallback lokaal
            String baseUrl = System.getenv("BASE_URL");
            if (baseUrl == null || baseUrl.isBlank()) {
                baseUrl = "http://localhost:8080";
            }

            String fileUrl = baseUrl + "/uploads/" + fileName;

            UserEntity user = getById(id);
            user.setProfilePictureUrl(fileUrl);
            userRepository.save(user);

            return fileUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }
}
