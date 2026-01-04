package nl.hva.ewa.regenwormen.api;

import nl.hva.ewa.regenwormen.entity.UserEntity;
import nl.hva.ewa.regenwormen.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ✅ Alle users
    @GetMapping
    public List<UserEntity> getAllUsers() {
        return userService.getAllUsers();
    }

    // ✅ User by id
    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    // ✅ Update location
    @PutMapping("/{id}/location")
    public ResponseEntity<Void> updateLocation(
            @PathVariable String id,
            @RequestBody UserEntity body
    ) {
        userService.updateLocation(id, body.getLocation());
        return ResponseEntity.ok().build();
    }

    // ✅ Update password
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable String id,
            @RequestBody UserEntity body
    ) {
        userService.updatePassword(id, body.getPassword());
        return ResponseEntity.ok().build();
    }

    // ✅ Upload profile photo
    @PostMapping("/{id}/photo")
    public ResponseEntity<String> uploadPhoto(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(userService.saveProfilePhoto(id, file));
    }
}
