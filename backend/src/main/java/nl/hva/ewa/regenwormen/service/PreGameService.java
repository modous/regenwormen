package nl.hva.ewa.regenwormen.service;

import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Player;
import nl.hva.ewa.regenwormen.repository.GameRepository;
import nl.hva.ewa.regenwormen.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PreGameService {

    private final GameRepository gameRepo;
    private final PlayerRepository playerRepo;

    @Autowired
    public PreGameService(GameRepository gameRepo, PlayerRepository playerRepo) {
        this.gameRepo = gameRepo;
        this.playerRepo = playerRepo;
    }

    public List<Player> findAllPlayers() {
        return playerRepo.findAll();
    }

    public List<Game> findAllPreGames() {
        return gameRepo.findAllPreGames();
    }

    public Game findGameByID(String id) {
        Game game = gameRepo.findById(id);
        if (game == null) {
            throw new IllegalArgumentException("Game with id: " + id + " does not exist");
        }
        return game;
    }

    public Game createGame(String roomName, int maxPlayers) {
        Game game = new Game(roomName, maxPlayers);
        return gameRepo.save(game);
    }

    public Game deleteGameByID(String id) {
        Game game = gameRepo.deleteById(id);
        if (game == null) {
            throw new IllegalArgumentException("Game with id: " + id + " does not exist");
        }
        return game;
    }

    public Game addPlayer(String gameId, String playerId) {
        Game game = gameRepo.findById(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game with id: " + gameId + " does not exist");
        }
        Player player = playerRepo.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Player not found"));


        game.addPlayer(player);
        return gameRepo.save(game);
    }

    public Game leavePlayer(String gameId, String playerId) {
        Game game = gameRepo.findById(gameId);
        if (game == null) {throw new IllegalArgumentException("Game with id: " + gameId + " does not exist");}

        game.leavePlayer(playerId);
        playerRepo.deleteByPlayerId(playerId);
        return gameRepo.save(game);
    }

    public Game startGame(String gameId){
        Game game = gameRepo.findById(gameId);
        if (game == null) {throw new IllegalArgumentException("Game with id: " + gameId + " does not exist");}

        game.startGame();
        return gameRepo.save(game);
    }
}
