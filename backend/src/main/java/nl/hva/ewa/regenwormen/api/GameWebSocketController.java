package nl.hva.ewa.regenwormen.controller;

import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Player;
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
    private final InGameService inGameService; // ✅ for reading timer data

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

    // -------------------- 🧩 Sync full game state --------------------
    @MessageMapping("/sync") // frontend sends to /app/sync
    public void syncGame(String gameId) {
        Game game = gameRepo.findById(gameId).orElse(null);
        if (game != null) {
            messagingTemplate.convertAndSend("/topic/game/" + gameId, game);
        }
    }

    // -------------------- 🔁 Broadcast game update --------------------
    public void broadcastGameUpdate(String gameId) {
        Game game = gameRepo.findById(gameId).orElse(null);
        if (game != null) {
            messagingTemplate.convertAndSend("/topic/game/" + gameId, game);
        }
    }

    // -------------------- ⏳ Broadcast live timer updates --------------------
    public void broadcastTimer(String gameId, String player, int timeLeft) {
        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId + "/timer",
                Map.of(
                        "player", player,
                        "timeLeft", timeLeft
                )
        );
    }

    // -------------------- 💬 Broadcast short system message --------------------
    public void broadcastSystemMessage(String gameId, String message) {
        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId + "/message",
                Map.of("text", message)
        );
    }

    // -------------------- 🔁 Force timer sync on reconnect --------------------
    @MessageMapping("/timerSync")
    public void sendTimerSync(String gameId) {
        Game game = gameRepo.findById(gameId).orElse(null);
        if (game == null) return;

        Player current = game.getCurrentPlayer();
        if (current == null) return;

        // ✅ Get actual remaining time from InGameService
        int remaining = inGameService.getRemainingTime(gameId);
        if (remaining <= 0) remaining = 10; // fallback default

        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId + "/timer",
                Map.of(
                        "player", current.getName(),
                        "timeLeft", remaining
                )
        );
    }

    // -------------------- 🚨 Broadcast turn timeout event --------------------
    public void broadcastTurnTimeout(String gameId, String player) {
        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId + "/turnTimeout",
                Map.of("player", player, "reset", true)
        );
    }

}
