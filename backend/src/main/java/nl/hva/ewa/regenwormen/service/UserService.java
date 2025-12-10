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
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already in use");
        }

        UserEntity user = new UserEntity(
                java.util.UUID.randomUUID().toString(),
                email,
                passwordEncoder.encode(password),
                username,
                null,
                null
        );

        return userRepository.save(user);
    }

    // ✅ Login check
    public boolean login(String identifier, String password) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsername(identifier);
        }

        return userOpt.isPresent()
                && passwordEncoder.matches(password, userOpt.get().getPassword());
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

    // ✅ Profielfoto opslaan
    public String saveProfilePhoto(String id, MultipartFile file) {
        try {
            String folder = "uploads/";
            Files.createDirectories(Paths.get(folder));

            String fileName = id + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(folder + fileName);
            Files.write(filePath, file.getBytes());

            String fileUrl = "http://localhost:8080/uploads/" + fileName;

            UserEntity user = getById(id);
            user.setProfilePictureUrl(fileUrl);
            userRepository.save(user);

            return fileUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }
}
