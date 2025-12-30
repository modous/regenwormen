package nl.hva.ewa.regenwormen.api;

import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Lobby;
import nl.hva.ewa.regenwormen.domain.Player;
import nl.hva.ewa.regenwormen.domain.dto.LobbyPlayer;
import nl.hva.ewa.regenwormen.repository.GameRepository;
import nl.hva.ewa.regenwormen.repository.LobbyRepository;
import nl.hva.ewa.regenwormen.repository.PlayerRepository;
import nl.hva.ewa.regenwormen.service.InGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lobbies")
@CrossOrigin(origins = "http://localhost:5173")
public class LobbyController {

    private final LobbyRepository lobbyRepo;
    private final GameRepository gameRepo;
    private final PlayerRepository playerRepo;
    private final LobbyWebSocketController lobbyWs;
    private final InGameService inGameService;

    @Autowired
    public LobbyController(
            LobbyRepository lobbyRepo,
            GameRepository gameRepo,
            PlayerRepository playerRepo,
            LobbyWebSocketController lobbyWs,
            InGameService inGameService
    ) {
        this.lobbyRepo = lobbyRepo;
        this.gameRepo = gameRepo;
        this.playerRepo = playerRepo;
        this.lobbyWs = lobbyWs;
        this.inGameService = inGameService;
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

        // 🚫 CHECK: Is user already in another lobby?
        List<Lobby> allLobbies = lobbyRepo.findAll();
        for (Lobby otherLobby : allLobbies) {
            boolean userInOtherLobby = otherLobby.getPlayers().stream()
                    .anyMatch(p -> p.getUsername().equals(lobbyPlayer.getUsername()));

            if (userInOtherLobby && otherLobby.getId() != id) {
                // Remove user from the other lobby
                otherLobby.getPlayers().removeIf(p -> p.getUsername().equals(lobbyPlayer.getUsername()));
                lobbyRepo.save(otherLobby);
                lobbyWs.broadcastLobbyUpdate(otherLobby.getId());
                System.out.println("👋 Player " + lobbyPlayer.getUsername() + " automatically removed from lobby " + otherLobby.getId() + " to join lobby " + id);
            }
        }

        boolean alreadyIn = lobby.getPlayers().stream()
                .anyMatch(p -> p.getUsername().equals(lobbyPlayer.getUsername()));

        if (!alreadyIn && !lobby.isFull()) {
            lobby.getPlayers().add(lobbyPlayer);
            // 💾 Save the lobby to persist the changes
            lobbyRepo.save(lobby);
            // 🔔 Notify all lobby members about the join
            lobbyWs.broadcastLobbyUpdate(id);
            System.out.println("✅ Player " + lobbyPlayer.getUsername() + " joined lobby " + id);
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

        // 💾 Save the lobby to persist the changes
        lobbyRepo.save(lobby);

        // 🔔 Notify others that ready status changed
        lobbyWs.broadcastLobbyUpdate(id);
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
                    found.resetPlayer();
                    game.addPlayer(found);
                } catch (Exception ignored) {}
            }
        });

        // ✅ Start the game
        game.startGame();

        // ✅ Save it
        gameRepo.save(game);

        lobby.setGameId(game.getId());
        lobby.setGameStarted(true);

        System.out.println("🚀 Game manually started for lobby " + lobby.getId()
                + " → " + game.getId()
                + " with " + game.getPlayers().size() + " players");

        // 🕒 Start timer for first player (after a short delay to allow clients to connect)
        if (!game.getPlayers().isEmpty()) {
            Player firstPlayer = game.getPlayers().get(0);
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // wait 1s to let clients connect
                    inGameService.startInitialTurnTimer(game, firstPlayer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // 🔔 Broadcast that the game started so clients redirect
        lobbyWs.broadcastLobbyUpdate(id);

        return lobby;
    }

    // ----------- LEAVE / INVITE -----------

    @PostMapping("/{id}/leave")
    public Lobby leaveLobby(@PathVariable int id, @RequestBody LobbyPlayer player) {
        Lobby lobby = lobbyRepo.findById(id);
        if (lobby == null) return null;

        lobby.getPlayers().removeIf(p -> p.getUsername().equals(player.getUsername()));

        // 💾 Save the lobby to persist the changes
        lobbyRepo.save(lobby);

        // 🔔 Notify all clients that someone left (specific lobby channel)
        lobbyWs.broadcastLobbyUpdate(id);

        System.out.println("👋 Player " + player.getUsername() + " left lobby " + id + " - players now: " + lobby.getPlayers().size());

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
