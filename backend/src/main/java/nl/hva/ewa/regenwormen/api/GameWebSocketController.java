package nl.hva.ewa.regenwormen.controller;

import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class GameWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameRepository gameRepo;

    @Autowired
    public GameWebSocketController(SimpMessagingTemplate messagingTemplate, GameRepository gameRepo) {
        this.messagingTemplate = messagingTemplate;
        this.gameRepo = gameRepo;
    }

    @MessageMapping("/sync") // frontend sends to /app/sync
    public void syncGame(String gameId) {
        Game game = gameRepo.findById(gameId).orElse(null);
        if (game != null) {
            messagingTemplate.convertAndSend("/topic/game/" + gameId, game);
        }
    }

    // helper to broadcast updates manually from InGameService
    public void broadcastGameUpdate(String gameId) {
        Game game = gameRepo.findById(gameId).orElse(null);
        if (game != null) {
            messagingTemplate.convertAndSend("/topic/game/" + gameId, game);
        }
    }
}
