package nl.hva.ewa.regenwormen.controller;

import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import nl.hva.ewa.regenwormen.domain.TilesPot;
import nl.hva.ewa.regenwormen.domain.dto.EndTurnView;
import nl.hva.ewa.regenwormen.domain.dto.TurnView;
import nl.hva.ewa.regenwormen.service.InGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ingame")
public class InGameController {
    private final InGameService service;

    @Autowired
    public InGameController(InGameService service) { this.service = service; }

    //round 0
    @PostMapping("/{gameId}/startroll0/{playerId}")
    public TurnView startAndRollRoundZero(@PathVariable String gameId, @PathVariable String playerId){
        return service.startAndRollRoundZero(gameId, playerId);
    }

    @PostMapping("/{gameId}/pickdice0/{playerId}")
    public TurnView pickDiceFaceZero(@PathVariable String gameId, @PathVariable String playerId, @RequestBody DiceFace diceface){
        return service.pickDiceFaceZero(gameId, playerId, diceface);
    }

    @PostMapping("/{gameId}/reroll0/{playerId}")
    public TurnView reRollZero(@PathVariable String gameId, @PathVariable String playerId){
        return service.reRollZero(gameId, playerId);
    }

    @PostMapping("/{gameId}/finishround0/{playerId}")
    public EndTurnView finishRoundZero(@PathVariable String gameId, @PathVariable String playerId){
        return service.finishRoundZero(gameId, playerId);
    }

    //Round >= 1
    @PostMapping("/{gameId}/startroll/{playerId}")
    public TurnView startAndRollRound(@PathVariable String gameId, @PathVariable String playerId){
        return service.startAndRollRound(gameId, playerId);
    }

    @PostMapping("/{gameId}/pickdice/{playerId}")
    public TurnView pickDiceFace(@PathVariable String gameId, @PathVariable String playerId, @RequestBody DiceFace diceface){
        return service.pickDiceFace(gameId, playerId, diceface);
    }

    @PostMapping("/{gameId}/reroll/{playerId}")
    public TurnView reRollRound(@PathVariable String gameId, @PathVariable String playerId){
        return service.reRoll(gameId, playerId);
    }

    @PostMapping("/{gameId}/finishround/{playerId}")
    public EndTurnView finishRound(@PathVariable String gameId, @PathVariable String playerId){
        return service.finishRound(gameId, playerId);
    }

    @PostMapping("/{gameId}/claimfrompot/{playerId}")
    public TilesPot claimTileFromPot(@PathVariable String gameId, @PathVariable String playerId){
        return service.claimTileFromPot(gameId, playerId);
    }

    @PostMapping("/{gameId}/stealFromPlayer/{playerId}")
    public TilesPot claimTileFromPot(@PathVariable String gameId, @PathVariable String playerId, @RequestBody String victimId){
        return service.stealTopTileFromPlayer(gameId, playerId, victimId);
    }

}
