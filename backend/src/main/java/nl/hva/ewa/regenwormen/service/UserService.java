package nl.hva.ewa.regenwormen.service;

import nl.hva.ewa.regenwormen.domain.User;
import nl.hva.ewa.regenwormen.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ Alle users ophalen
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ User ophalen via ID
    public User getById(String id) {
        return userRepository.findById(id).orElseThrow();
    }

    // ✅ Registreren van nieuwe gebruiker
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

    // ✅ Login check
    public boolean login(String identifier, String password) {
        Optional<User> userOpt = userRepository.findByEmail(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsername(identifier);
        }
        return userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword());
    }

    // ✅ Locatie bijwerken
    public void updateLocation(String id, String location) {
        User user = getById(id);
        user.setLocation(location);
        userRepository.save(user);
    }

    // ✅ Wachtwoord wijzigen
    public void updatePassword(String id, String rawPassword) {
        User user = getById(id);
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }

    // ✅ Profielfoto opslaan en URL genereren
    public String saveProfilePhoto(String id, MultipartFile file) {
        try {
            // Map aanmaken indien nog niet bestaat
            String folder = "uploads/";
            Files.createDirectories(Paths.get(folder));

            // Bestandsnaam opbouwen met user-id prefix
            String fileName = id + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(folder + fileName);
            Files.write(filePath, file.getBytes());

            // URL relatief opslaan, zodat WebConfig hem kan serveren
             String fileUrl = "http://localhost:8080/uploads/" + fileName;


            User user = getById(id);
            user.setProfilePictureUrl(fileUrl);
            userRepository.save(user);

            return fileUrl;
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to save file: " + e.getMessage(), e);
        }
    }
}
