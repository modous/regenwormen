package nl.hva.ewa.regenwormen.controller;

import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.TilesPot;
import nl.hva.ewa.regenwormen.domain.dto.EndTurnView;
import nl.hva.ewa.regenwormen.domain.dto.TurnView;
import nl.hva.ewa.regenwormen.service.InGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
