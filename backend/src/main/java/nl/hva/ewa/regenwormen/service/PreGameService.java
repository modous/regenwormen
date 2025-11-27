package nl.hva.ewa.regenwormen.service;

import jakarta.transaction.Transactional;
import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Player;
import nl.hva.ewa.regenwormen.repository.GameRepository;
import nl.hva.ewa.regenwormen.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import nl.hva.ewa.regenwormen.domain.Enum.GameState;

import java.util.List;

@Service
@Transactional
public class PreGameService {

    private final GameRepository gameRepo;
    private final PlayerRepository playerRepo;
    private final GameGuards guards;

    public PreGameService(GameRepository gameRepo,
                          PlayerRepository playerRepo,
                          GameGuards guards) {
        this.gameRepo = gameRepo;
        this.playerRepo = playerRepo;
        this.guards = guards;
    }

    // ---------- READ ----------
    public List<Player> findAllPlayers() {
        return playerRepo.findAll();
    }

    public List<Game> findAllPreGames() {
        return gameRepo.findAllPreGames();
    }

    public Game findGameByID(String id) {
        return guards.getGameOrThrow(id);
    }

    // ---------- COMMANDS ----------
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

    /**
     * ✅ Start a game using only the actual backend gameId (short hex).
     */
    public Game startGame(String gameId) {
        System.out.println("🎯 Starting game: " + gameId +
                " | repo currently holds " + gameRepo.findAll().size() + " games");

        Game game = guards.getGameOrThrow(gameId);

        if (game.getGameState() == GameState.PLAYING) {
            System.out.println("⚠️ Game " + gameId + " already in PLAYING state, skipping re-start");
            return game;
        }

        game.startGame();
        Game saved = gameRepo.save(game);
        System.out.println("✅ Game " + saved.getId() + " started successfully (state=" + saved.getGameState() + ")");
        return saved;
    }

    /**
     * ✅ Direct start — frontend always sends real gameId (hex).
     * No numeric or fallback logic anymore.
     */
    public Game startGameByIdOrLobby(String gameId) {
        System.out.println("🚀 [Simplified] Starting game with ID: " + gameId);
        return startGame(gameId);
    }

    /**
     * ✅ Ensure all players exist in the game before starting.
     */
    private void syncLobbyPlayersIfNeeded(Game game) {
        List<Player> allPlayers = playerRepo.findAll();
        for (Player p : allPlayers) {
            if (!game.getPlayers().contains(p)) {
                try {
                    game.addPlayer(p);
                } catch (Exception ignored) {}
            }
        }
        gameRepo.save(game);
    }

    // ---------- TEST ----------
    public int testGame() {
        List<Player> allPlayers = playerRepo.findAll();
        List<Game> allGames = gameRepo.findAllPreGames();

        if (allGames.isEmpty() || allPlayers.size() < 2) return 0;

        Game game = allGames.get(0);
        game.addPlayer(allPlayers.get(0));
        game.addPlayer(allPlayers.get(1));
        gameRepo.save(game);
        return 1;
    }
}
