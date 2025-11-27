package nl.hva.ewa.regenwormen.controller;

import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Player;
import nl.hva.ewa.regenwormen.domain.dto.CreateGameRequest;
import nl.hva.ewa.regenwormen.service.PreGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pregame")
@CrossOrigin(origins = "*")
public class PreGameController {

    private final PreGameService service;

    @Autowired
    public PreGameController(PreGameService service) {
        this.service = service;
    }

    @GetMapping("/games")
    public List<Game> list() {
        return service.findAllPreGames();
    }

    @PostMapping("/create")
    public Game create(@RequestBody CreateGameRequest request) {
        return service.createGame(request.roomName(), request.maxPlayers());
    }

    @GetMapping("/{gameId}")
    public Map<String, Object> get(@PathVariable String gameId) {
        Game g = service.findGameByID(gameId);
        return Map.of(
                "id", g.getId(),
                "gameName", g.getGameName(),
                "maxPlayers", g.getMaxPlayers(),
                "players", g.getPlayers(),
                "gameStarted", g.getGameState().toString().equals("PLAYING")
        );
    }


    @PostMapping("/{id}/start")
    public ResponseEntity<?> start(@PathVariable String id) {
        try {
            Game started = service.startGameByIdOrLobby(id);
            return ResponseEntity.ok(started);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ NEW ENDPOINTS
    @PostMapping("/{gameId}/join/{playerId}")
    public ResponseEntity<Game> join(@PathVariable String gameId, @PathVariable String playerId) {
        Game updated = service.addPlayer(gameId, playerId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{gameId}/leave/{playerId}")
    public ResponseEntity<Game> leave(@PathVariable String gameId, @PathVariable String playerId) {
        Game updated = service.leavePlayer(gameId, playerId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/players")
    public List<Player> getPlayers() {
        return service.findAllPlayers();
    }
}
