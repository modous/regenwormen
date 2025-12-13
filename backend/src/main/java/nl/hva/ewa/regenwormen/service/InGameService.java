package nl.hva.ewa.regenwormen.service;

import jakarta.transaction.Transactional;
import nl.hva.ewa.regenwormen.api.GameWebSocketController;
import nl.hva.ewa.regenwormen.api.LobbyWebSocketController;
import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import nl.hva.ewa.regenwormen.domain.Enum.GameState;
import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Player;
import nl.hva.ewa.regenwormen.domain.TilesPot;
import nl.hva.ewa.regenwormen.domain.dto.EndTurnView;
import nl.hva.ewa.regenwormen.domain.dto.TurnView;
import nl.hva.ewa.regenwormen.repository.GameRepository;
import nl.hva.ewa.regenwormen.repository.LobbyRepository;
import nl.hva.ewa.regenwormen.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class InGameService {

    private final GameRepository gameRepo;
    private final PlayerRepository playerRepo;
    private final LobbyRepository lobbyRepo;
    private final GameGuards guards;
    private final GameWebSocketController ws;
    private final LobbyWebSocketController lobbyWs;

    // Timer system
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();
    private final Map<String, Integer> remainingTimes = new ConcurrentHashMap<>();

    // Disconnect/reconnect tracking
    private final ScheduledExecutorService disconnectScheduler = Executors.newScheduledThreadPool(2);
    private final ConcurrentHashMap<String, ScheduledFuture<?>> pendingDisconnects = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ScheduledFuture<?>> pendingCountdowns = new ConcurrentHashMap<>();
    private static final int DISCONNECT_TIMEOUT_SECONDS = 60;
    private static final int TURN_SECONDS = 10;

    public InGameService(GameRepository gameRepo,
                         PlayerRepository playerRepo,
                         LobbyRepository lobbyRepo,
                         GameGuards guards,
                         GameWebSocketController ws,
                         LobbyWebSocketController lobbyWs) {
        this.gameRepo = gameRepo;
        this.playerRepo = playerRepo;
        this.lobbyRepo = lobbyRepo;
        this.guards = guards;
        this.ws = ws;
        this.lobbyWs = lobbyWs;
    }

    // ---------------------- GET FULL GAME STATE ----------------------
    public Game getGameById(String gameId) {
        return gameRepo.findById(gameId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found: " + gameId));
    }

    // ---------------------- HELPERS ----------------------
    private <T> T persistAndReturn(Game game, T payload) {
        gameRepo.save(game);
        ws.broadcastGameUpdate(game.getId());
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

    // ---------------------- TURN TIMER LOGIC ----------------------
    public void startTurnTimer(Game game, Player player) {
        String gameId = game.getId();
        cancelTurnTimer(gameId);

        final int[] timeLeft = {TURN_SECONDS};
        remainingTimes.put(gameId, timeLeft[0]);
        ws.broadcastTimer(gameId, player.getName(), timeLeft[0]);

        ScheduledFuture<?> timer = scheduler.scheduleAtFixedRate(() -> {
            try {
                timeLeft[0]--;
                if (timeLeft[0] <= 0) {
                    cancelTurnTimer(gameId);
                    handleTurnTimeout(game, player);
                    return;
                }
                remainingTimes.put(gameId, timeLeft[0]);
                ws.broadcastTimer(gameId, player.getName(), timeLeft[0]);
            } catch (Exception e) {
                log.error("Error in turn timer: {}", e.getMessage());
            }
        }, 1, 1, TimeUnit.SECONDS);

        activeTimers.put(gameId, timer);
    }

    private void cancelTurnTimer(String gameId) {
        ScheduledFuture<?> old = activeTimers.remove(gameId);
        if (old != null) old.cancel(true);
        remainingTimes.remove(gameId);
    }

    private void handleTurnTimeout(Game game, Player player) {
        log.info("Turn timeout for player: {}", player.getName());

        try {
            game.forceNextPlayer();
            persistAndReturn(game, game);
        } catch (Exception e) {
            log.warn("Could not force next player after timeout: {}", e.getMessage());
        }

        Player nextPlayer = game.getCurrentPlayer();
        if (nextPlayer != null) {
            String msg = player.getName() + "'s turn expired. Now it's " + nextPlayer.getName() + "'s turn!";
            ws.broadcastSystemMessage(game.getId(), msg);
        }

        // Broadcast updated game state and start timer for next player
        ws.broadcastGameUpdate(game.getId());
        if (nextPlayer != null) {
            scheduler.schedule(() -> startTurnTimer(game, nextPlayer), 1, TimeUnit.SECONDS);
        }
    }

    public int getRemainingTime(String gameId) {
        return remainingTimes.getOrDefault(gameId, 0);
    }

    // ---------------------- ROUND 0 ----------------------
    public TurnView startAndRollRoundZero(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);

        TurnView view = game.startAndRollRoundZero(player);
        persistAndReturn(game, view);
        startTurnTimer(game, player);
        return view;
    }

    public TurnView pickDiceFaceZero(String gameId, String username, DiceFace diceFace) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);

        TurnView view = game.pickDiceFaceZero(player, diceFace);
        startTurnTimer(game, player);
        return persistAndReturn(game, view);
    }

    public TurnView reRollZero(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);

        TurnView view = game.reRollRoundZero(player);
        startTurnTimer(game, player);
        return persistAndReturn(game, view);
    }

    public EndTurnView finishRoundZero(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);

        cancelTurnTimer(game.getId());
        EndTurnView view = game.finishRoundZero(player);
        return persistAndReturn(game, view);
    }

    // ---------------------- NORMAL ROUNDS ----------------------
    public TurnView startAndRollRound(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);
        guards.ensureNoPlayersDisconnected(game);

        TurnView view = game.startAndRollRound();
        persistAndReturn(game, view);
        startTurnTimer(game, player);
        return view;
    }

    public TurnView pickDiceFace(String gameId, String username, DiceFace diceFace) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);
        guards.ensureNoPlayersDisconnected(game);

        TurnView view = game.pickDiceFace(diceFace);
        startTurnTimer(game, player);
        return persistAndReturn(game, view);
    }

    public TurnView reRoll(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);
        guards.ensureNoPlayersDisconnected(game);

        TurnView view = game.reRollRound();
        startTurnTimer(game, player);
        return persistAndReturn(game, view);
    }

    public EndTurnView finishRound(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);
        guards.ensureNoPlayersDisconnected(game);

        cancelTurnTimer(game.getId());
        EndTurnView view = game.finishRound();
        return persistAndReturn(game, view);
    }

    // ---------------------- TILE CLAIMING ----------------------
    public Game claimTileFromPot(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);
        guards.ensureNoPlayersDisconnected(game);

        try {
            game.claimFromPot();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Claim not possible");
        }
        cancelTurnTimer(game.getId());
        Game updated = persistAndReturn(game, game);

        // Start next player's timer
        Player nextPlayer = game.getCurrentPlayer();
        if (nextPlayer != null) {
            ws.broadcastGameUpdate(gameId);
            scheduler.schedule(() -> startTurnTimer(game, nextPlayer), 1, TimeUnit.SECONDS);
        }

        return updated;
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
        guards.ensureNoPlayersDisconnected(game);

        TilesPot result = game.stealTopTile(victim.getId());
        cancelTurnTimer(game.getId());
        persistAndReturn(game, result);

        // Start next player's timer
        Player nextPlayer = game.getCurrentPlayer();
        if (nextPlayer != null) {
            ws.broadcastGameUpdate(gameId);
            scheduler.schedule(() -> startTurnTimer(game, nextPlayer), 1, TimeUnit.SECONDS);
        }

        return result;
    }

    // ---------------------- DISCONNECT HANDLING ----------------------
    public void handlePlayerDisconnectedByUsername(String gameId, String username) {
        log.info("Player disconnected - gameId: {}, username: {}", gameId, username);

        Game game = getGameById(gameId);
        Player player = game.getPlayers().stream()
                .filter(p -> p.getName().equals(username))
                .findFirst()
                .orElse(null);

        if (player == null) {
            log.warn("Player not found for disconnect: {}", username);
            return;
        }

        String key = gameId + ":" + player.getId();
        if (pendingDisconnects.containsKey(key)) {
            log.debug("Disconnect already pending for player: {}", username);
            return;
        }

        player.setStatus(Player.PlayerStatus.DISCONNECTED);
        log.info("Player marked as DISCONNECTED: {}", player.getName());

        cancelTurnTimer(gameId);

        ws.broadcastSystemMessage(gameId, player.getName() + " is disconnected. Waiting " + DISCONNECT_TIMEOUT_SECONDS + "s for reconnect...");
        ws.broadcastGameUpdate(gameId);

        // Start countdown timer
        final int[] timeLeft = {DISCONNECT_TIMEOUT_SECONDS};

        ScheduledFuture<?> countdownTask = disconnectScheduler.scheduleAtFixedRate(() -> {
            try {
                timeLeft[0]--;
                ws.broadcastDisconnectCountdown(gameId, player.getName(), timeLeft[0]);
            } catch (Exception e) {
                log.error("Error in disconnect countdown: {}", e.getMessage());
            }
        }, 1, 1, TimeUnit.SECONDS);

        ScheduledFuture<?> removalTask = disconnectScheduler.schedule(() -> {
            log.info("Disconnect timeout expired - removing player: {}", player.getName());
            removePlayerAfterTimeout(gameId, player.getId());
            pendingDisconnects.remove(key);
            pendingCountdowns.remove(key);
        }, DISCONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        pendingDisconnects.put(key, removalTask);
        pendingCountdowns.put(key, countdownTask);
    }

    public void handlePlayerReconnectedByUsername(String gameId, String username) {
        log.info("Player reconnected - gameId: {}, username: {}", gameId, username);

        Game game = getGameById(gameId);
        Player player = game.getPlayers().stream()
                .filter(p -> p.getName().equals(username))
                .findFirst()
                .orElse(null);

        if (player == null) {
            log.warn("Player not found for reconnect: {}", username);
            return;
        }

        String key = gameId + ":" + player.getId();
        ScheduledFuture<?> removalTask = pendingDisconnects.remove(key);
        ScheduledFuture<?> countdownTask = pendingCountdowns.remove(key);

        boolean wasDisconnected = player.getStatus() == Player.PlayerStatus.DISCONNECTED;
        player.setStatus(Player.PlayerStatus.CONNECTED);
        log.info("Player reconnected successfully: {}", player.getName());

        if (removalTask != null) removalTask.cancel(true);
        if (countdownTask != null) countdownTask.cancel(true);

        gameRepo.save(game);

        // Unblock frontend by sending 0 seconds
        ws.broadcastDisconnectCountdown(gameId, player.getName(), 0);
        ws.broadcastSystemMessage(gameId, player.getName() + " reconnected! Game resumed.");
        ws.broadcastGameUpdate(gameId);

        // Restart turn timer if player was disconnected
        if (wasDisconnected) {
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer != null) {
                log.info("Restarting turn timer for player: {}", currentPlayer.getName());
                scheduler.schedule(() -> {
                    Game freshGame = getGameById(gameId);
                    startTurnTimer(freshGame, freshGame.getCurrentPlayer());
                }, 500, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void handlePlayerLeaveGameByUsername(String gameId, String username) {
        log.info("Player leaving game - gameId: {}, username: {}", gameId, username);

        Game game = getGameById(gameId);
        Player player = game.getPlayers().stream()
                .filter(p -> p.getName().equals(username))
                .findFirst()
                .orElse(null);

        if (player == null) {
            log.warn("Player not found in game: {}", username);
            return;
        }

        game.leavePlayer(player.getId());
        gameRepo.save(game);

        ws.broadcastSystemMessage(gameId, player.getName() + " left the game.");
        ws.broadcastGameUpdate(gameId);

        // Update lobby when player leaves
        notifyLobbyOfPlayerRemoval(gameId);

        if (game.getPlayers().size() < 2) {
            log.info("Game ending - fewer than 2 players remaining");
            endAndResetGame(game);
        }
    }

    private void removePlayerAfterTimeout(String gameId, String playerId) {
        Game game = getGameById(gameId);
        Player player = game.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElse(null);

        if (player == null) return;

        ws.broadcastSystemMessage(gameId, player.getName() + " did not reconnect and is removed from the game.");

        game.leavePlayer(playerId);
        gameRepo.save(game);
        ws.broadcastGameUpdate(gameId);

        // Update lobby to reflect player count change
        notifyLobbyOfPlayerRemoval(gameId);

        if (game.getPlayers().size() < 2) {
            endAndResetGame(game);
        }
    }

    private void endAndResetGame(Game game) {
        game.setGameState(GameState.ENDED);
        gameRepo.save(game);
        
        ws.broadcastSystemMessage(game.getId(), "Game ended - not enough players remaining. Returning to lobby...");
        ws.broadcastGameUpdate(game.getId());
        
        log.info("Game ended - resetting after delay: {}", game.getId());

        scheduler.schedule(() -> {
            try {
                Game freshGame = getGameById(game.getId());
                freshGame.resetGame();
                gameRepo.save(freshGame);
                log.info("Game reset to PRE_GAME state: {}", freshGame.getId());

                ws.broadcastSystemMessage(freshGame.getId(), "Lobby has been reset and is ready for new players.");
                ws.broadcastGameUpdate(freshGame.getId());

                // Update lobby when game is reset
                resetLobbyForGame(freshGame.getId());
                clearDisconnectTrackingForGame(freshGame.getId());
            } catch (Exception e) {
                log.error("Error resetting game: {}", e.getMessage());
            }
        }, 3, TimeUnit.SECONDS);
    }

    private void clearDisconnectTrackingForGame(String gameId) {
        log.debug("Clearing disconnect tracking for game: {}", gameId);
        pendingDisconnects.keySet().removeIf(key -> key.startsWith(gameId + ":"));
        pendingCountdowns.keySet().removeIf(key -> key.startsWith(gameId + ":"));
        cancelTurnTimer(gameId);
    }

    // ===================== LOBBY UPDATE METHODS =====================

    /**
     * Notify lobby that player count has changed (someone left/was removed).
     * This broadcasts the updated lobby to all connected clients.
     */
    private void notifyLobbyOfPlayerRemoval(String gameId) {
        try {
            var allLobbies = lobbyRepo.findAll();
            for (var lobby : allLobbies) {
                if (gameId.equals(lobby.getGameId())) {
                    log.info("Notifying lobby {} of player removal from game {}", lobby.getId(), gameId);
                    // Broadcast update to all lobby subscribers so they see updated player count
                    lobbyWs.broadcastLobbyUpdate(lobby.getId());
                    log.debug("Lobby {} notified of player removal", lobby.getId());
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error notifying lobby of player removal for game {}: {}", gameId, e.getMessage());
        }
    }

    /**
     * Reset the lobby when game ends/resets.
     * Clears gameStarted flag, gameId, and notifies subscribers.
     */
    private void resetLobbyForGame(String gameId) {
        try {
            var allLobbies = lobbyRepo.findAll();
            for (var lobby : allLobbies) {
                if (gameId.equals(lobby.getGameId())) {
                    log.info("Resetting lobby {} for game {}", lobby.getId(), gameId);

                    // Reset game-related flags
                    lobby.setGameStarted(false);
                    lobby.setGameId(null);

                    // Clear all players from the lobby
                    lobby.getPlayers().clear();

                    // Save and broadcast update to all lobby subscribers
                    lobbyRepo.save(lobby);
                    lobbyWs.broadcastLobbyUpdate(lobby.getId());

                    log.info("Lobby {} reset and notified - players cleared", lobby.getId());
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error resetting lobby for game {}: {}", gameId, e.getMessage());
        }
    }

    public void markAllPlayersConnected(String gameId) {
        Game game = getGameById(gameId);
        for (Player p : game.getPlayers()) {
            p.setStatus(Player.PlayerStatus.CONNECTED);
        }
        gameRepo.save(game);
        ws.broadcastGameUpdate(gameId);
    }
}
