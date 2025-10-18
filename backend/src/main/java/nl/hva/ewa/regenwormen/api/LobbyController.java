package nl.hva.ewa.regenwormen.api;

import nl.hva.ewa.regenwormen.domain.Lobby;
import nl.hva.ewa.regenwormen.domain.dto.LobbyPlayer;
import nl.hva.ewa.regenwormen.repository.LobbyRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lobbies")
@CrossOrigin(origins = "http://localhost:5173")
public class LobbyController {

    private final LobbyRepository repo;

    public LobbyController(LobbyRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Lobby> getAllLobbies() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Lobby getLobby(@PathVariable int id) {
        return repo.findById(id);
    }

    @PostMapping("/join/{id}")
    public Lobby joinLobby(@PathVariable int id, @RequestBody LobbyPlayer player) {
        Lobby lobby = repo.findById(id);
        if (lobby == null) return null;

        boolean alreadyIn = lobby.getPlayers().stream()
                .anyMatch(p -> p.getUsername().equals(player.getUsername()));

        if (!alreadyIn && !lobby.isFull()) {
            lobby.getPlayers().add(player);
        }

        return lobby;
    }

    @PostMapping("/{id}/ready")
    public Lobby toggleReady(@PathVariable int id, @RequestBody LobbyPlayer player) {
        Lobby lobby = repo.findById(id);
        if (lobby == null) return null;

        lobby.getPlayers().forEach(p -> {
            if (p.getUsername().equals(player.getUsername())) {
                p.setReady(!p.isReady());
            }
        });

        return lobby;
    }

    @PostMapping("/{id}/invite")
    public String invitePlayer(@PathVariable int id, @RequestBody LobbyPlayer inviter, @RequestParam String inviteeEmail) {
        Lobby lobby = repo.findById(id);
        if (lobby == null) return "Lobby not found";

        System.out.println("📨 " + inviter.getUsername() + " invited " + inviteeEmail + " to lobby " + lobby.getName());

        return "Invitation sent to " + inviteeEmail;
    }

    @PostMapping("/{id}/leave")
    public Lobby leaveLobby(@PathVariable int id, @RequestBody LobbyPlayer player) {
        Lobby lobby = repo.findById(id);
        if (lobby == null) return null;

        // ✅ Remove player safely by username
        lobby.getPlayers().removeIf(p -> p.getUsername().equals(player.getUsername()));



        return lobby;
    }
}
