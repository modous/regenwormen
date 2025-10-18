package nl.hva.ewa.regenwormen.api;

import nl.hva.ewa.regenwormen.domain.Lobby;
import nl.hva.ewa.regenwormen.domain.dto.LobbyPlayer;
import nl.hva.ewa.regenwormen.repository.LobbyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/lobbies")
@CrossOrigin(origins = "http://localhost:5173")
public class LobbyController {

    private final LobbyRepository repo;

    public LobbyController(LobbyRepository repo) {
        this.repo = repo;
    }

    // Get all lobbies
    @GetMapping
    public List<Lobby> getAllLobbies() {
        return repo.findAll();
    }

    // Get a single lobby by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getLobby(@PathVariable int id) {
        Lobby lobby = repo.findById(id);
        if (lobby == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lobby not found");
        }
        return ResponseEntity.ok(lobby);
    }

    // Join a lobby
    @PostMapping("/join/{id}")
    public ResponseEntity<?> joinLobby(@PathVariable int id, @RequestBody LobbyPlayer player) {
        Lobby lobby = repo.findById(id);
        if (lobby == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lobby not found");
        }

        // Make sure player list exists
        if (lobby.getPlayers() == null) {
            lobby.setPlayers(new ArrayList<>());
        }

        // Check if player already joined
        boolean alreadyIn = lobby.getPlayers().stream()
                .anyMatch(p -> p.getUsername().equals(player.getUsername()));

        if (!alreadyIn && !lobby.isFull()) {
            lobby.getPlayers().add(player);
        }

        return ResponseEntity.ok(lobby);
    }

    // Toggle a player's ready state
    @PostMapping("/{id}/ready")
    public ResponseEntity<?> toggleReady(@PathVariable int id, @RequestBody LobbyPlayer player) {
        Lobby lobby = repo.findById(id);
        if (lobby == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lobby not found");
        }

        // Toggle ready state for the given player
        lobby.getPlayers().forEach(p -> {
            if (p.getUsername().equals(player.getUsername())) {
                p.setReady(!p.isReady());
            }
        });

        // Activate countdown if everyone is ready
        if (!lobby.getPlayers().isEmpty() && lobby.allReady()) {
            lobby.setCountdownActive(true);
        } else {
            lobby.setCountdownActive(false);
        }

        return ResponseEntity.ok(lobby);
    }
}
