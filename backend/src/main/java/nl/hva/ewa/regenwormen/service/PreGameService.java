package nl.hva.ewa.regenwormen.service;

import jakarta.transaction.Transactional;
import nl.hva.ewa.regenwormen.domain.Enum.GameState;
import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Player;
import nl.hva.ewa.regenwormen.repository.GameRepository;
import nl.hva.ewa.regenwormen.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public Game startGame(String gameId) {
        Game game = guards.getGameOrThrow(gameId);

        game.startGame();
        return gameRepo.save(game);
    }

    // ---------- TEST (optioneel houden) ----------
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
