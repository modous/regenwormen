package nl.hva.ewa.regenwormen.api;

import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Player;
import nl.hva.ewa.regenwormen.domain.dto.PlayerActionDto;
import nl.hva.ewa.regenwormen.repository.GameRepository;
import nl.hva.ewa.regenwormen.service.InGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.context.annotation.Lazy;

import java.util.Map;

@Controller
public class GameWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameRepository gameRepo;
    private final InGameService inGameService;

    @Autowired
    public GameWebSocketController(
            SimpMessagingTemplate messagingTemplate,
            GameRepository gameRepo,
            @Lazy InGameService inGameService
    ) {
        this.messagingTemplate = messagingTemplate;
        this.gameRepo = gameRepo;
        this.inGameService = inGameService;
    }

    // -------------------- SYNC GAME STATE --------------------
    @MessageMapping("/sync")
    public void syncGame(String gameId) {
        gameRepo.findById(gameId).ifPresent(game ->
            messagingTemplate.convertAndSend("/topic/game/" + gameId, game)
        );
    }

    // -------------------- BROADCAST GAME UPDATE --------------------
    public void broadcastGameUpdate(String gameId) {
        gameRepo.findById(gameId).ifPresent(game ->
            messagingTemplate.convertAndSend("/topic/game/" + gameId, game)
        );
    }

    // -------------------- BROADCAST TIMER UPDATES --------------------
    public void broadcastTimer(String gameId, String player, int timeLeft) {
        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId + "/timer",
                Map.of("player", player, "timeLeft", timeLeft)
        );
    }

    // -------------------- BROADCAST DISCONNECT COUNTDOWN --------------------
    public void broadcastDisconnectCountdown(String gameId, String playerName, int secondsLeft) {
        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId + "/disconnect",
                Map.of("player", playerName, "secondsLeft", secondsLeft)
        );
    }

    // -------------------- BROADCAST SYSTEM MESSAGE --------------------
    public void broadcastSystemMessage(String gameId, String message) {
        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId + "/message",
                Map.of("text", message)
        );
    }

    // -------------------- SYNC TIMER ON RECONNECT --------------------
    @MessageMapping("/timerSync")
    public void sendTimerSync(String gameId) {
        Game game = gameRepo.findById(gameId).orElse(null);
        if (game == null) return;

        Player current = game.getCurrentPlayer();
        if (current == null) return;

        int remaining = inGameService.getRemainingTime(gameId);
        if (remaining <= 0) remaining = 10;

        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId + "/timer",
                Map.of("player", current.getName(), "timeLeft", remaining)
        );
    }

    // -------------------- PLAYER DISCONNECT HANDLER --------------------
    @MessageMapping("/disconnect")
    public void playerDisconnected(PlayerActionDto action) {
        if (action != null && action.getGameId() != null && action.getPlayerId() != null) {
            try {
                inGameService.handlePlayerDisconnectedByUsername(action.getGameId(), action.getPlayerId());
            } catch (Exception e) {
                System.err.println("Error handling disconnect: " + e.getMessage());
            }
        }
    }

    // -------------------- PLAYER RECONNECT HANDLER --------------------
    @MessageMapping("/reconnect")
    public void playerReconnected(PlayerActionDto action) {
        if (action != null && action.getGameId() != null && action.getPlayerId() != null) {
            try {
                inGameService.handlePlayerReconnectedByUsername(action.getGameId(), action.getPlayerId());
            } catch (Exception e) {
                System.err.println("Error handling reconnect: " + e.getMessage());
            }
        }
    }

    // -------------------- HEARTBEAT HANDLER --------------------
    @MessageMapping("/heartbeat")
    public void playerHeartbeat(PlayerActionDto action) {
        // Player is active - optional logging only
    }
}
