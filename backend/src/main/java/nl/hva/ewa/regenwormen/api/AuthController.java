package nl.hva.ewa.regenwormen.api;

import nl.hva.ewa.regenwormen.entity.UserEntity;
import nl.hva.ewa.regenwormen.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        UserEntity saved = authService.register(
                body.get("email"),
                body.get("username"),
                body.get("password")
        );
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        UserEntity user = authService.login(
                body.get("identifier"),
                body.get("password")
        );
        return ResponseEntity.ok(user);
    }
}
