package nl.hva.ewa.regenwormen.service;

import jakarta.transaction.Transactional;
import nl.hva.ewa.regenwormen.controller.GameWebSocketController;
import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Player;
import nl.hva.ewa.regenwormen.domain.TilesPot;
import nl.hva.ewa.regenwormen.domain.dto.EndTurnView;
import nl.hva.ewa.regenwormen.domain.dto.TurnView;
import nl.hva.ewa.regenwormen.repository.GameRepository;
import nl.hva.ewa.regenwormen.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class InGameService {

    private final GameRepository gameRepo;
    private final PlayerRepository playerRepo;
    private final GameGuards guards;
    private final GameWebSocketController ws;

    public InGameService(GameRepository gameRepo,
                         PlayerRepository playerRepo,
                         GameGuards guards,
                         GameWebSocketController ws) {
        this.gameRepo = gameRepo;
        this.playerRepo = playerRepo;
        this.guards = guards;
        this.ws = ws;
    }

    // ---------------------- 🧩 Get full game by ID ----------------------
    public Game getGameById(String gameId) {
        return gameRepo.findById(gameId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found: " + gameId));
    }

    // ---------------------- Helpers ----------------------
    private <T> T persistAndReturn(Game game, T payload) {
        gameRepo.save(game);
        ws.broadcastGameUpdate(game.getId()); // Push live game state to all connected clients
        return payload;
    }

    private Player getPlayerByUsername(Game game, String username) {
        return game.getPlayers().stream()
                .filter(p -> p.getName().equals(username))
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Player '" + username + "' not found in game " + game.getId()));
    }

    // ---------------------- ROUND 0 ----------------------
    public TurnView startAndRollRoundZero(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);
        TurnView view = game.startAndRollRoundZero(player);
        return persistAndReturn(game, view);
    }

    public TurnView pickDiceFaceZero(String gameId, String username, DiceFace diceFace) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);
        TurnView view = game.pickDiceFaceZero(player, diceFace);
        return persistAndReturn(game, view);
    }

    public TurnView reRollZero(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);
        TurnView view = game.reRollRoundZero(player);
        return persistAndReturn(game, view);
    }

    public EndTurnView finishRoundZero(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);
        EndTurnView view = game.finishRoundZero(player);
        return persistAndReturn(game, view);
    }

    // ---------------------- NORMAL ROUNDS ----------------------
    public TurnView startAndRollRound(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);
        TurnView view = game.startAndRollRound();
        return persistAndReturn(game, view);
    }

    public TurnView pickDiceFace(String gameId, String username, DiceFace diceFace) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);
        TurnView view = game.pickDiceFace(diceFace);
        return persistAndReturn(game, view);
    }

    public TurnView reRoll(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);
        TurnView view = game.reRollRound();
        return persistAndReturn(game, view);
    }

    public EndTurnView finishRound(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);
        EndTurnView view = game.finishRound();
        return persistAndReturn(game, view);
    }

    // ---------------------- TILE CLAIMING ----------------------
    public Game claimTileFromPot(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);

        // Claim and persist
        game.claimFromPot();
        return persistAndReturn(game, game);
    }

    // ---------------------- TILE STEALING ----------------------
    public TilesPot stealTopTileFromPlayer(String gameId, String currentUsername, String victimUsername) {
        Game game = guards.getGameOrThrow(gameId);
        Player current = getPlayerByUsername(game, currentUsername);

        String victimClean = (victimUsername == null)
                ? null
                : victimUsername.trim().replace("\"", "");
        Player victim = getPlayerByUsername(game, victimClean);

        guards.ensurePlayerInGame(game, current);
        guards.ensurePlayerInGame(game, victim);
        guards.ensureYourTurn(game, current);

        TilesPot result = game.stealTopTile(victim.getId());
        return persistAndReturn(game, result);
    }
}
