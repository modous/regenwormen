package nl.hva.ewa.regenwormen.api;

import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Lobby;
import nl.hva.ewa.regenwormen.domain.Player;
import nl.hva.ewa.regenwormen.domain.dto.LobbyPlayer;
import nl.hva.ewa.regenwormen.repository.GameRepository;
import nl.hva.ewa.regenwormen.repository.LobbyRepository;
import nl.hva.ewa.regenwormen.repository.PlayerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lobbies")
@CrossOrigin(origins = "http://localhost:5173")
public class LobbyController {

    private final LobbyRepository lobbyRepo;
    private final GameRepository gameRepo;
    private final PlayerRepository playerRepo;

    public LobbyController(LobbyRepository lobbyRepo,
                           GameRepository gameRepo,
                           PlayerRepository playerRepo) {
        this.lobbyRepo = lobbyRepo;
        this.gameRepo = gameRepo;
        this.playerRepo = playerRepo;
    }

    // ----------- BASIC LOBBY ACTIONS -----------

    @GetMapping
    public List<Lobby> getAllLobbies() {
        return lobbyRepo.findAll();
    }

    @GetMapping("/{id}")
    public Lobby getLobby(@PathVariable int id) {
        return lobbyRepo.findById(id);
    }

    @PostMapping("/join/{id}")
    public Lobby joinLobby(@PathVariable int id, @RequestBody LobbyPlayer lobbyPlayer) {
        Lobby lobby = lobbyRepo.findById(id);
        if (lobby == null) return null;

        boolean alreadyIn = lobby.getPlayers().stream()
                .anyMatch(p -> p.getUsername().equals(lobbyPlayer.getUsername()));

        if (!alreadyIn && !lobby.isFull()) {
            lobby.getPlayers().add(lobbyPlayer);
        }

        // ✅ Ensure backend Player exists in repository (for /ingame lookups)
        Player backendPlayer = playerRepo.findAll().stream()
                .filter(p -> p.getName().equals(lobbyPlayer.getUsername()))
                .findFirst()
                .orElse(null);

        if (backendPlayer == null) {
            backendPlayer = new Player(lobbyPlayer.getUsername());
            playerRepo.save(backendPlayer);
            System.out.println("✅ Created backend player: " + backendPlayer.getName());
        }

        return lobby;
    }

    @PostMapping("/{id}/ready")
    public Lobby toggleReady(@PathVariable int id, @RequestBody LobbyPlayer player) {
        Lobby lobby = lobbyRepo.findById(id);
        if (lobby == null) return null;

        lobby.getPlayers().forEach(p -> {
            if (p.getUsername().equals(player.getUsername())) {
                p.setReady(!p.isReady());
            }
        });

        // no auto-start
        return lobby;
    }

    // ----------- MANUAL GAME START -----------

    @PostMapping("/{id}/start")
    public Lobby startGame(@PathVariable int id) {
        Lobby lobby = lobbyRepo.findById(id);
        if (lobby == null) throw new IllegalArgumentException("Lobby not found");
        if (!lobby.allReady()) throw new IllegalStateException("Not all players are ready");
        if (lobby.isGameStarted()) throw new IllegalStateException("Game already started");

        // ✅ Create a new Game instance and copy all lobby players
        Game game = new Game(lobby.getName(), lobby.getMaxPlayers());

        lobby.getPlayers().forEach(lp -> {
            Player found = playerRepo.findAll().stream()
                    .filter(p -> p.getName().equals(lp.getUsername()))
                    .findFirst()
                    .orElse(null);

            if (found != null) {
                try {
                    game.addPlayer(found);
                } catch (Exception ignored) {}
            }
        });

        // ✅ Crucial step: actually start the game state
        game.startGame();

        // ✅ Save it after starting (so state & turnIndex persist)
        gameRepo.save(game);

        lobby.setGameId(game.getId());
        lobby.setGameStarted(true);

        System.out.println("🚀 Game manually started for lobby " + lobby.getId()
                + " → " + game.getId()
                + " with " + game.getPlayers().size() + " players");

        return lobby;
    }

    // ----------- LEAVE / INVITE -----------

    @PostMapping("/{id}/leave")
    public Lobby leaveLobby(@PathVariable int id, @RequestBody LobbyPlayer player) {
        Lobby lobby = lobbyRepo.findById(id);
        if (lobby == null) return null;

        lobby.getPlayers().removeIf(p -> p.getUsername().equals(player.getUsername()));
        return lobby;
    }

    @PostMapping("/{id}/invite")
    public String invitePlayer(@PathVariable int id,
                               @RequestBody LobbyPlayer inviter,
                               @RequestParam String inviteeEmail) {
        Lobby lobby = lobbyRepo.findById(id);
        if (lobby == null) return "Lobby not found";

        System.out.println("📨 " + inviter.getUsername()
                + " invited " + inviteeEmail
                + " to lobby " + lobby.getName());
        return "Invitation sent to " + inviteeEmail;
    }
}
