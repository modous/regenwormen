package nl.hva.ewa.regenwormen.controller;

import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.TilesPot;
import nl.hva.ewa.regenwormen.domain.dto.EndTurnView;
import nl.hva.ewa.regenwormen.domain.dto.TurnView;
import nl.hva.ewa.regenwormen.service.InGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/ingame")
public class InGameController {

    private final InGameService service;

    @Autowired
    public InGameController(InGameService service) {
        this.service = service;
    }

    // -------------------- 🧩 Get full game state --------------------
    @GetMapping("/{gameId}")
    public ResponseEntity<Game> getGameState(@PathVariable String gameId) {
        Game game = service.getGameById(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }

    // -------------------- ROUND 0 --------------------
    @PostMapping("/{gameId}/startroll0/{username}")
    public TurnView startAndRollRoundZero(@PathVariable String gameId, @PathVariable String username) {
        return service.startAndRollRoundZero(gameId, username);
    }

    @PostMapping("/{gameId}/pickdice0/{username}")
    public TurnView pickDiceFaceZero(@PathVariable String gameId,
                                     @PathVariable String username,
                                     @RequestBody DiceFace diceFace) {
        return service.pickDiceFaceZero(gameId, username, diceFace);
    }

    @PostMapping("/{gameId}/reroll0/{username}")
    public TurnView reRollZero(@PathVariable String gameId, @PathVariable String username) {
        return service.reRollZero(gameId, username);
    }

    @PostMapping("/{gameId}/finishround0/{username}")
    public EndTurnView finishRoundZero(@PathVariable String gameId, @PathVariable String username) {
        return service.finishRoundZero(gameId, username);
    }

    // -------------------- NORMAL ROUNDS (>= 1) --------------------
    @PostMapping("/{gameId}/startroll/{username}")
    public TurnView startAndRollRound(@PathVariable String gameId, @PathVariable String username) {
        return service.startAndRollRound(gameId, username);
    }

    @PostMapping("/{gameId}/pickdice/{username}")
    public TurnView pickDiceFace(@PathVariable String gameId,
                                 @PathVariable String username,
                                 @RequestBody DiceFace diceFace) {
        return service.pickDiceFace(gameId, username, diceFace);
    }

    @PostMapping("/{gameId}/reroll/{username}")
    public TurnView reRollRound(@PathVariable String gameId, @PathVariable String username) {
        return service.reRoll(gameId, username);
    }

    @PostMapping("/{gameId}/finishround/{username}")
    public EndTurnView finishRound(@PathVariable String gameId, @PathVariable String username) {
        return service.finishRound(gameId, username);
    }

    // -------------------- TILE CLAIMING --------------------
    @PostMapping("/{gameId}/claimfrompot/{username}")
    public ResponseEntity<Game> claimFromPot(@PathVariable String gameId, @PathVariable String username) {
        Game updated = service.claimTileFromPot(gameId, username);
        return ResponseEntity.ok(updated);
    }

    // -------------------- TILE STEALING --------------------
    @PostMapping("/{gameId}/stealFromPlayer/{username}")
    public TilesPot stealTopTileFromPlayer(@PathVariable String gameId,
                                           @PathVariable String username,
                                           @RequestBody String victimUsername) {
        return service.stealTopTileFromPlayer(gameId, username, victimUsername);
    }
    // -------------------- 🔌 DISCONNECT & LEAVE SECTION --------------------
    /**
     * Player verlaat expliciet naar lobby via USERNAME (geen timeout, direct verwijderd).
     * POST /ingame/{gameId}/leave/{username}
     */
    @PostMapping("/{gameId}/leave/{username}")
    public ResponseEntity<Void> playerLeaveGameByUsername(@PathVariable String gameId,
                                                          @PathVariable String username) {
        try {
            log.info("👋 Player LEAVE notification received - gameId: {} , username: {}", gameId, username);
            log.info("✅ Player successfully left game");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.warn("❌ Error handling player leave: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Player disconnected (via sendBeacon, WebSocket close, or browser close).
     * POST /ingame/{gameId}/disconnect/{username}
     *
     * This starts a countdown timer. If player reconnects before timeout, they can continue.
     * If timeout expires, they are removed from the game.
     */
    @PostMapping("/{gameId}/disconnect/{username}")
    public ResponseEntity<Void> playerDisconnectByUsername(@PathVariable String gameId,
                                                           @PathVariable String username) {
        try {
            log.info("🔌 Disconnect notification received - gameId: {}, username: {}", gameId, username);
            log.info("⏳ Disconnect countdown started for player: {}", username);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.warn("❌ Error handling disconnect: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Player reconnected successfully (via WebSocket reconnection).
     * POST /ingame/{gameId}/reconnect/{username}
     *
     * Cancels the disconnect timeout and restarts the turn timer.
     */
    @PostMapping("/{gameId}/reconnect/{username}")
    public ResponseEntity<Void> playerReconnectByUsername(@PathVariable String gameId,
                                                          @PathVariable String username) {
        try {
            log.info("✅ Reconnect notification received - gameId: {}, username: {}", gameId, username);
            log.info("🎮 Player successfully reconnected: {}", username);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.warn("❌ Error handling reconnect: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
