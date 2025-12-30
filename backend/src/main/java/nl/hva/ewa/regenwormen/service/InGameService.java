package nl.hva.ewa.regenwormen.service;

import jakarta.transaction.Transactional;
import nl.hva.ewa.regenwormen.controller.GameWebSocketController;
import nl.hva.ewa.regenwormen.domain.EndGameHandler;
import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Player;
import nl.hva.ewa.regenwormen.domain.TilesPot;
import nl.hva.ewa.regenwormen.domain.dto.EndTurnView;
import nl.hva.ewa.regenwormen.domain.dto.TurnView;
import nl.hva.ewa.regenwormen.repository.GameRepository;
import nl.hva.ewa.regenwormen.repository.PlayerRepository;
import nl.hva.ewa.regenwormen.repository.GameResultRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.*;

@Service
@Transactional
public class InGameService {

    private final GameRepository gameRepo;
    private final PlayerRepository playerRepo;
    private final GameResultRepository gameResultRepository;
    private final GameGuards guards;
    private final GameWebSocketController ws;

    // 🔥 Timer system
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();
    private final Map<String, Integer> remainingTimes = new ConcurrentHashMap<>();

    private static final int TURN_SECONDS = 10;
    private static final int TIMER_DELAY_SECONDS = 5;

    public InGameService(GameRepository gameRepo,
                         PlayerRepository playerRepo,
                         GameResultRepository gameResultRepository,
                         GameGuards guards,
                         GameWebSocketController ws) {
        this.gameRepo = gameRepo;
        this.playerRepo = playerRepo;
        this.gameResultRepository = gameResultRepository;
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
        ws.broadcastGameUpdate(game.getId());
        return payload;
    }

    private Player getPlayerByUsername(Game game, String username) {
        return game.getPlayers().stream()
                .filter(p -> p.getName().equals(username))
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Player '" + username + "' not found in game " + game.getId()
                        ));
    }

    // ---------------------- 🔥 TURN TIMER LOGIC ----------------------
    private void startTurnTimer(Game game, Player player) {
        String gameId = game.getId();
        cancelTurnTimer(gameId);

        final int[] timeLeft = {TURN_SECONDS};
        remainingTimes.put(gameId, timeLeft[0]);

        ScheduledFuture<?> delayTask = scheduler.schedule(() -> {

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
                    e.printStackTrace();
                }
            }, 1, 1, TimeUnit.SECONDS);

            activeTimers.put(gameId, timer);

        }, TIMER_DELAY_SECONDS, TimeUnit.SECONDS);

        activeTimers.put(gameId, delayTask);
    }

    private void startNextPlayerTimerAndAnnounce(Game game) {
        Player next = game.getCurrentPlayer();
        if (next == null) return;

        ws.broadcastGameUpdate(game.getId());
        remainingTimes.put(game.getId(), TURN_SECONDS);
        ws.broadcastTimer(game.getId(), next.getName(), TURN_SECONDS);

        scheduler.schedule(() -> startTurnTimer(game, next), 1, TimeUnit.SECONDS);
    }

    private void cancelTurnTimer(String gameId) {
        ScheduledFuture<?> old = activeTimers.remove(gameId);
        if (old != null) old.cancel(true);
        remainingTimes.remove(gameId);
    }

    private void handleTurnTimeout(Game game, Player player) {
        ws.broadcastTurnTimeout(game.getId(), player.getName());

        try {
            game.forceNextPlayer();
            handleEndGameIfNeeded(game);
            persistAndReturn(game, game);
        } catch (Exception ignored) {}

        if (game.getGameState() == nl.hva.ewa.regenwormen.domain.Enum.GameState.ENDED) {
            // ❌ game voorbij → GEEN nieuwe timers
            return;
        }

        Player next = game.getCurrentPlayer();
        if (next != null) {
            ws.broadcastSystemMessage(
                    game.getId(),
                    "⚠️ " + player.getName() + "'s turn expired — it's now " + next.getName() + "'s turn!"
            );
        }

        startNextPlayerTimerAndAnnounce(game);
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

        EndGameHandler handler =
                new EndGameHandler(game, ws, gameResultRepository);
        handler.checkAndHandleEndGame();

        cancelTurnTimer(game.getId());
        EndTurnView view = game.finishRoundZero(player);
        return persistAndReturn(game, view);
    }

    // ---------------------- NORMAL ROUNDS ----------------------
    public TurnView startAndRollRound(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensureYourTurn(game, player);

        TurnView view = game.startAndRollRound();
        persistAndReturn(game, view);
        startTurnTimer(game, player);
        return view;
    }

    public TurnView pickDiceFace(String gameId, String username, DiceFace diceFace) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensureYourTurn(game, player);

        TurnView view = game.pickDiceFace(diceFace);
        startTurnTimer(game, player);
        return persistAndReturn(game, view);
    }

    public TurnView reRoll(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensureYourTurn(game, player);

        TurnView view = game.reRollRound();
        startTurnTimer(game, player);
        return persistAndReturn(game, view);
    }

    public EndTurnView finishRound(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensureYourTurn(game, player);

        EndGameHandler handler =
                new EndGameHandler(game, ws, gameResultRepository);
        handler.checkAndHandleEndGame();

        cancelTurnTimer(game.getId());
        EndTurnView view = game.finishRound();
        return persistAndReturn(game, view);
    }

    // ---------------------- TILE CLAIMING ----------------------
    public Game claimTileFromPot(String gameId, String username, int tileValue) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensureYourTurn(game, player);

        game.claimFromPot(tileValue);

        cancelTurnTimer(game.getId());
        persistAndReturn(game, game);
        startNextPlayerTimerAndAnnounce(game);
        return game;
    }

    // ---------------------- TILE STEALING ----------------------
    public TilesPot stealTopTileFromPlayer(String gameId, String currentUsername, String victimUsername) {
        Game game = guards.getGameOrThrow(gameId);
        Player current = getPlayerByUsername(game, currentUsername);
        Player victim = getPlayerByUsername(game, victimUsername.trim().replace("\"", ""));

        guards.ensureYourTurn(game, current);

        TilesPot result = game.stealTopTile(victim.getName());
        handleEndGameIfNeeded(game);
        cancelTurnTimer(game.getId());
        persistAndReturn(game, result);
        startNextPlayerTimerAndAnnounce(game);
        return result;
    }
    // 🕒 Used by LobbyController to start the timer after game creation
    public void startInitialTurnTimer(Game game, Player player) {
        scheduler.schedule(
                () -> startTurnTimer(game, player),
                1,
                TimeUnit.SECONDS
        );
    }

    private void handleEndGameIfNeeded(Game game) {
        System.out.println("🧪 handleEndGameIfNeeded called for game " + game.getId()
                + " | state=" + game.getGameState()
                + " | tilesLeft=" + game.getTilesPot().amountAvailableTiles());
        EndGameHandler handler =
                new EndGameHandler(game, ws, gameResultRepository);
        handler.checkAndHandleEndGame();
    }
}
