package nl.hva.ewa.regenwormen.api;

import nl.hva.ewa.regenwormen.entity.UserEntity;
import nl.hva.ewa.regenwormen.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            UserEntity saved = authService.register(
                    body.get("email"),
                    body.get("username"),
                    body.get("password")
            );
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            UserEntity user = authService.login(
                    body.get("identifier"),
                    body.get("password")
            );
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }
}
