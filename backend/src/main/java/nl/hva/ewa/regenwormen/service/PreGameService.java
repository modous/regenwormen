package nl.hva.ewa.regenwormen.service;

import jakarta.transaction.Transactional;
import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Player;
import nl.hva.ewa.regenwormen.repository.GameRepository;
import nl.hva.ewa.regenwormen.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import nl.hva.ewa.regenwormen.domain.Enum.GameState;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
public class PreGameService {

    private final GameRepository gameRepo;
    private final PlayerRepository playerRepo;
    private final GameGuards guards;

    // Disconnect tracking for lobby
    private final ScheduledExecutorService disconnectScheduler = Executors.newScheduledThreadPool(2);
    private final ConcurrentHashMap<String, ScheduledFuture<?>> pendingDisconnects = new ConcurrentHashMap<>();
    private static final int DISCONNECT_TIMEOUT_SECONDS = 60;

    public PreGameService(GameRepository gameRepo,
                          PlayerRepository playerRepo,
                          GameGuards guards) {
        this.gameRepo = gameRepo;
        this.playerRepo = playerRepo;
        this.guards = guards;
    }

    // ---------------------- READ ----------------------
    public List<Game> findAllPreGames() {
        return gameRepo.findAllPreGames();
    }

    public Game findGameByID(String id) {
        return guards.getGameOrThrow(id);
    }

    // ---------------------- COMMANDS ----------------------
    public Game createGame(String roomName, int maxPlayers) {
        Game game = new Game(roomName, maxPlayers);
        return gameRepo.save(game);
    }

    public Game deleteGameByID(String id) {
        Game game = guards.getGameOrThrow(id);
        gameRepo.deleteById(id);
        return game;
    }

    public Game addPlayer(String gameId, String playerId) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = guards.getPlayerOrThrow(playerId);

        game.addPlayer(player);
        return gameRepo.save(game);
    }

    public Game leavePlayer(String gameId, String playerId) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = guards.getPlayerOrThrow(playerId);

        guards.ensurePlayerInGame(game, player);
        game.leavePlayer(playerId);

        return gameRepo.save(game);
    }

    // ---------------------- DISCONNECT HANDLING IN LOBBY ----------------------
    public void handlePlayerDisconnectedInLobby(String gameId, String username) {
        log.info("Player disconnected from lobby - gameId: {}, username: {}", gameId, username);

        Game game = gameRepo.findById(gameId).orElse(null);
        if (game == null) {
            log.warn("Game not found for disconnect: {}", gameId);
            return;
        }

        Player player = game.getPlayers().stream()
                .filter(p -> p.getName().equals(username))
                .findFirst()
                .orElse(null);

        if (player == null) {
            log.warn("Player not found in lobby: {}", username);
            return;
        }

        String key = gameId + ":" + player.getId();
        if (pendingDisconnects.containsKey(key)) {
            log.debug("Disconnect already pending for player: {}", username);
            return;
        }

        log.info("Starting disconnect timeout for lobby player: {}", username);

        ScheduledFuture<?> removalTask = disconnectScheduler.schedule(() -> {
            log.info("Timeout expired - removing lobby player: {}", username);
            Game g = gameRepo.findById(gameId).orElse(null);
            if (g != null) {
                Player p = g.getPlayers().stream()
                        .filter(pl -> pl.getName().equals(username))
                        .findFirst()
                        .orElse(null);
                if (p != null) {
                    g.leavePlayer(p.getId());
                    gameRepo.save(g);
                    log.info("Lobby player removed from game: {}", username);
                }
            }
            pendingDisconnects.remove(key);
        }, DISCONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        pendingDisconnects.put(key, removalTask);
    }

    public void handlePlayerReconnectedInLobby(String gameId, String username) {
        log.info("Player reconnected to lobby - gameId: {}, username: {}", gameId, username);

        Game game = gameRepo.findById(gameId).orElse(null);
        if (game == null) {
            log.warn("Game not found for reconnect: {}", gameId);
            return;
        }

        Player player = game.getPlayers().stream()
                .filter(p -> p.getName().equals(username))
                .findFirst()
                .orElse(null);

        if (player == null) {
            log.warn("Player not found in lobby: {}", username);
            return;
        }

        String key = gameId + ":" + player.getId();
        ScheduledFuture<?> removalTask = pendingDisconnects.remove(key);

        if (removalTask != null) {
            removalTask.cancel(true);
            log.info("Cancelled removal timeout for player: {}", username);
        }
    }

    // ---------------------- GAME START ----------------------
    public Game startGame(String gameId) {
        log.info("Starting game: {}", gameId);

        Game game = guards.getGameOrThrow(gameId);

        if (game.getGameState() == GameState.PLAYING) {
            log.warn("Game already in PLAYING state: {}", gameId);
            return game;
        }

        game.startGame();
        return gameRepo.save(game);
    }

    public Game startGameByIdOrLobby(String gameId) {
        return startGame(gameId);
    }
}
