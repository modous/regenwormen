package nl.hva.ewa.regenwormen.api;

import nl.hva.ewa.regenwormen.domain.User;
import nl.hva.ewa.regenwormen.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<?> updateLocation(@PathVariable String id, @RequestBody User updatedUser) {
        userService.updateLocation(id, updatedUser.getLocation());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable String id, @RequestBody User updatedUser) {
        userService.updatePassword(id, updatedUser.getPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/photo")
    public ResponseEntity<String> uploadPhoto(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        String photoUrl = userService.saveProfilePhoto(id, file);
        return ResponseEntity.ok(photoUrl);
    }
}
