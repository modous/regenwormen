package nl.hva.ewa.regenwormen.service;

import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Player;
import nl.hva.ewa.regenwormen.repository.GameRepository;
import nl.hva.ewa.regenwormen.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class GameGuards {
    private final GameRepository gameRepo;
    private final PlayerRepository playerRepo;

    public GameGuards(GameRepository gameRepo, PlayerRepository playerRepo) {
        this.gameRepo = gameRepo;
        this.playerRepo = playerRepo;
    }

    public Game getGameOrThrow(String gameId) {
        return gameRepo.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Game with id %s not found".formatted(gameId)));
    }

    public Player getPlayerOrThrow(String playerId) {
        return playerRepo.findById(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Player with id %s not found".formatted(playerId)));
    }

    public void ensurePlayerInGame(Game game, Player player) {
        if (!game.hasPlayer(player)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Player is not part of this game");
        }
    }

    public void ensureYourTurn(Game game, Player player) {
        if (!player.equals(game.getCurrentPlayer())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Not your turn");
        }
    }

    // 🔌 Check if any player is disconnected (game should be blocked)
    public void ensureNoPlayersDisconnected(Game game) {
        boolean anyDisconnected = game.getPlayers().stream()
                .anyMatch(p -> p.getStatus() == Player.PlayerStatus.DISCONNECTED);

        if (anyDisconnected) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Game is paused - waiting for disconnected player to reconnect or timeout");
        }
    }

}