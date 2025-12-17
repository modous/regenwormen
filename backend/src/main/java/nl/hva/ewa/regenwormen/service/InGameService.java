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
    private final GameGuards guards;
    private final GameWebSocketController ws;

    // 🔥 Timer system
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();
    private final Map<String, Integer> remainingTimes = new ConcurrentHashMap<>();

//    private static final int TURN_SECONDS = 10;

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

    // ---------------------- 🔥 TURN TIMER LOGIC ----------------------
//    private void startTurnTimer(Game game, Player player) {
//        String gameId = game.getId();
//        cancelTurnTimer(gameId);
//
//        final int[] timeLeft = {TURN_SECONDS};
//        remainingTimes.put(gameId, timeLeft[0]);
//
//        // Immediately broadcast first tick so clients show correct player+10s
//        ws.broadcastTimer(gameId, player.getName(), timeLeft[0]);
//
//        ScheduledFuture<?> timer = scheduler.scheduleAtFixedRate(() -> {
//            try {
//                timeLeft[0]--;
//                if (timeLeft[0] <= 0) {
//                    cancelTurnTimer(gameId);
//                    handleTurnTimeout(game, player);
//                    return;
//                }
//                remainingTimes.put(gameId, timeLeft[0]);
//                ws.broadcastTimer(gameId, player.getName(), timeLeft[0]);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }, 1, 1, TimeUnit.SECONDS);
//
//        activeTimers.put(gameId, timer);
//    }

    private static final int TURN_SECONDS = 10;
    private static final int TIMER_DELAY_SECONDS = 5;

    private void startTurnTimer(Game game, Player player) {
        String gameId = game.getId();
        cancelTurnTimer(gameId);

        final int[] timeLeft = {TURN_SECONDS};

        // Timer wordt nog niet getoond → frontend laat hem zien zodra de eerste broadcast komt
        remainingTimes.put(gameId, timeLeft[0]);

        // Wacht 5 seconden vóór eerste broadcast + vóór start aftellen
        ScheduledFuture<?> delayTask = scheduler.schedule(() -> {

            // Eerste keer broadcast: timer verschijnt op 10
            ws.broadcastTimer(gameId, player.getName(), timeLeft[0]);

            // Start het echte aftellen
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

        // SLA DE DELAY OOK OP, anders kun je hem niet cancellen
        activeTimers.put(gameId, delayTask);
    }

    private void startNextPlayerTimerAndAnnounce(Game game) {
        String gameId = game.getId();
        Player next = game.getCurrentPlayer();
        if (next == null) return;

        // Broadcast updated game state (turn index changed) and fresh timer instantly
        ws.broadcastGameUpdate(gameId);
        remainingTimes.put(gameId, TURN_SECONDS);
        ws.broadcastTimer(gameId, next.getName(), TURN_SECONDS);

        // Begin ticking after a small delay so clients can render the new state
        scheduler.schedule(() -> startTurnTimer(game, next), 1, TimeUnit.SECONDS);
    }

    private void cancelTurnTimer(String gameId) {
        ScheduledFuture<?> old = activeTimers.remove(gameId);
        if (old != null) old.cancel(true);
        remainingTimes.remove(gameId);
    }

    private void handleTurnTimeout(Game game, Player player) {
        System.out.println("⏰ Turn expired for player " + player.getName());
        ws.broadcastTurnTimeout(game.getId(), player.getName());

        try {
            // Always safe-skip on timeout (treat as bust/skip)
            game.forceNextPlayer();
            persistAndReturn(game, game);
        } catch (Exception e) {
            System.out.println("⚠️ Could not force next player after timeout for "
                    + player.getName() + ": " + e.getMessage());
        }

        // Announce switch
        Player nextPlayer = game.getCurrentPlayer();
        if (nextPlayer != null) {
            String msg = "⚠️ " + player.getName()
                    + "'s turn expired — it's now " + nextPlayer.getName() + "'s turn!";
            ws.broadcastSystemMessage(game.getId(), msg);
        }

        // Start next player's timer (and push instant 10s)
        startNextPlayerTimerAndAnnounce(game);
    }

    // ---------------------- 🧮 Expose remaining time ----------------------
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

        // ✅ Check of het spel nu afgelopen is
        EndGameHandler endGameHandler = new EndGameHandler(game, ws);
        endGameHandler.checkAndHandleEndGame();

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

        TurnView view = game.pickDiceFace(diceFace);
        startTurnTimer(game, player);
        return persistAndReturn(game, view);
    }

    public TurnView reRoll(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);

        TurnView view = game.reRollRound();
        startTurnTimer(game, player);
        return persistAndReturn(game, view);
    }

    public EndTurnView finishRound(String gameId, String username) {
        Game game = guards.getGameOrThrow(gameId);
        Player player = getPlayerByUsername(game, username);
        guards.ensurePlayerInGame(game, player);
        guards.ensureYourTurn(game, player);

        // ✅ Check of het spel nu afgelopen is
        EndGameHandler endGameHandler = new EndGameHandler(game, ws);
        endGameHandler.checkAndHandleEndGame();

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

        try {
            game.claimFromPot();
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Claim not possible");
        }
        cancelTurnTimer(game.getId());
        Game updated = persistAndReturn(game, game);

        // start next player's timer immediately
        startNextPlayerTimerAndAnnounce(game);

        return updated;
    }

    // 🕒 Used by LobbyController to start the timer after game creation
    public void startInitialTurnTimer(Game game, Player player) {
        scheduler.schedule(() -> startTurnTimer(game, player), 1, TimeUnit.SECONDS);
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

        TilesPot result = game.stealTopTile(victim.getId()); // advances turn
        cancelTurnTimer(game.getId());
        persistAndReturn(game, result);

        // start next player's timer immediately
        startNextPlayerTimerAndAnnounce(game);

        return result;
    }
}
