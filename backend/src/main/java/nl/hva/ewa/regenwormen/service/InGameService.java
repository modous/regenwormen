package nl.hva.ewa.regenwormen.service;

import jakarta.transaction.Transactional;
import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Player;
import nl.hva.ewa.regenwormen.domain.TilesPot;
import nl.hva.ewa.regenwormen.domain.dto.EndTurnView;
import nl.hva.ewa.regenwormen.domain.dto.TurnView;
import nl.hva.ewa.regenwormen.repository.GameRepository;
import nl.hva.ewa.regenwormen.repository.PlayerRepository;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class InGameService {

    private final GameRepository gameRepo;
    private final PlayerRepository playerRepo;
    private final GameGuards guards;

    public InGameService(GameRepository gameRepo,
                         PlayerRepository playerRepo,
                         GameGuards guards) {
        this.gameRepo = gameRepo;
        this.playerRepo = playerRepo;
        this.guards = guards;
    }

    // ---------- helpers ----------
    private <T> T persistAndReturn(Game game, T payload) {
        // In JPA zou flush/dirty checking het meestal doen, maar bij jouw mock repo is save prima.
        gameRepo.save(game);
        return payload;
    }

    // ---------- round zero ----------
    public TurnView startAndRollRoundZero(String gameId, String playerId) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = guards.getPlayerOrThrow(playerId);
        guards.ensurePlayerInGame(game, player);

        TurnView view = game.startAndRollRoundZero(player);
        return persistAndReturn(game, view);
    }

    public TurnView pickDiceFaceZero(String gameId, String playerId, DiceFace diceFace) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = guards.getPlayerOrThrow(playerId);
        guards.ensurePlayerInGame(game, player);

        TurnView view = game.pickDiceFaceZero(player, diceFace);
        return persistAndReturn(game, view);
    }

    public TurnView reRollZero(String gameId, String playerId) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = guards.getPlayerOrThrow(playerId);
        guards.ensurePlayerInGame(game, player);

        TurnView view = game.reRollRoundZero(player);
        return persistAndReturn(game, view);
    }

    public EndTurnView finishRoundZero(String gameId, String playerId) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = guards.getPlayerOrThrow(playerId);
        guards.ensurePlayerInGame(game, player);

        EndTurnView view = game.finishRoundZero(player);
        return persistAndReturn(game, view);
    }

    // ---------- normale ronden ----------
    public TurnView startAndRollRound(String gameId, String playerId) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = guards.getPlayerOrThrow(playerId);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);

        TurnView view = game.startAndRollRound();
        return persistAndReturn(game, view);
    }

    public TurnView pickDiceFace(String gameId, String playerId, DiceFace diceFace) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = guards.getPlayerOrThrow(playerId);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);

        TurnView view = game.pickDiceFace(diceFace);
        return persistAndReturn(game, view);
    }

    public TurnView reRoll(String gameId, String playerId) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = guards.getPlayerOrThrow(playerId);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);

        TurnView view = game.reRollRound();
        return persistAndReturn(game, view);
    }

    public EndTurnView finishRound(String gameId, String playerId) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = guards.getPlayerOrThrow(playerId);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);

        EndTurnView view = game.finishRound();
        return persistAndReturn(game, view);
    }

    // ---------- tiles ----------
    public TilesPot claimTileFromPot(String gameId, String playerId) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = guards.getPlayerOrThrow(playerId);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);

        TilesPot result = game.claimFromPot();
        return persistAndReturn(game, result);
    }

    public TilesPot stealTopTileFromPlayer(String gameId, String currentPlayerId, String victimId) {
        Game game = guards.getGameOrThrow(gameId);
        Player current = guards.getPlayerOrThrow(currentPlayerId);
        String victimIdClean = victimId == null ? null : victimId.trim().replace("\"", "");
        Player victim = guards.getPlayerOrThrow(victimIdClean);
        guards.ensurePlayerInGame(game, current);
        guards.ensurePlayerInGame(game, victim);
        guards.ensureYourTurn(game, current);

        TilesPot result = game.stealTopTile(victim.getId());
        return persistAndReturn(game, result);
    }
}
