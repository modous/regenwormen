package nl.hva.ewa.regenwormen.api;

import nl.hva.ewa.regenwormen.domain.Lobby;
import nl.hva.ewa.regenwormen.domain.dto.ChatMessage;
import nl.hva.ewa.regenwormen.repository.LobbyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class LobbyWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final LobbyRepository lobbyRepo;

    @Autowired
    public LobbyWebSocketController(SimpMessagingTemplate messagingTemplate, LobbyRepository lobbyRepo) {
        this.messagingTemplate = messagingTemplate;
        this.lobbyRepo = lobbyRepo;
    }

    // 🧩 Called when frontend publishes to /app/lobbySync
    @MessageMapping("/lobbySync")
    public void syncLobby(String lobbyId) {
        Lobby lobby = lobbyRepo.findById(Integer.parseInt(lobbyId));
        if (lobby != null) {
            messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId, lobby);
        }
    }

    // 💬 Chat functionality
    @MessageMapping("/chat")
    public void handleChat(@Payload ChatMessage chatMessage) {
        // Broadcast the message to everyone in the specific lobby topic
        messagingTemplate.convertAndSend("/topic/lobby/" + chatMessage.getLobbyId() + "/chat", chatMessage);
    }

    // 🧠 Helper for broadcasting updates programmatically
    public void broadcastLobbyUpdate(int lobbyId) {
        Lobby lobby = lobbyRepo.findById(lobbyId);
        if (lobby != null) {
            messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId, lobby);
        }
    }
}
