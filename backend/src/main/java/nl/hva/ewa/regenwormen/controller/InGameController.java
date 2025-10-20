package nl.hva.ewa.regenwormen.controller;

import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
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

    @PostMapping("/{gameId}/startroll/{playerId}")
    public TurnView startAndRollRoundZero(@PathVariable String gameId, @PathVariable String playerId){
        return service.startAndRollRoundZero(gameId, playerId);
    }

    @PostMapping("/{gameId}/pickdice/{playerId}")
    public TurnView pickDiceFaceZero(@PathVariable String gameId, @PathVariable String playerId, @RequestBody DiceFace diceface){
        return service.pickDiceFaceZero(gameId, playerId, diceface);
    }

    @PostMapping("/{gameId}/reroll/{playerId}")
    public TurnView reRollRoundZero(@PathVariable String gameId, @PathVariable String playerId){
        return service.reRollRoundZero(gameId, playerId);
    }

    @PostMapping("/{gameId}/finishroundzero/{playerId}")
    public EndTurnView finishRoundZero(@PathVariable String gameId, @PathVariable String playerId){
        return service.finishRoundZero(gameId, playerId);
    }

}
