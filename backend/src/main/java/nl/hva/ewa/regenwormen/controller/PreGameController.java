package nl.hva.ewa.regenwormen.controller;

import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Player;
import nl.hva.ewa.regenwormen.domain.dto.CreateGameRequest;
import nl.hva.ewa.regenwormen.service.PreGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pregame")
public class PreGameController {

    private final PreGameService service;

    @Autowired
    public PreGameController(PreGameService service) { this.service = service; }

    @GetMapping("/games/test2")
    public int testGame() {
        return service.testGame();
    }

    @GetMapping("/games")
    public List<Game> list() { return service.findAllPreGames(); }

    @GetMapping("/{gameId}")
    public Game get(@PathVariable String gameId) {
        return service.findGameByID(gameId);
    }

    @PostMapping("/create")
    public Game create(@RequestBody CreateGameRequest request) {
        return service.createGame(request.roomName(), request.maxPlayers());
    }

    @DeleteMapping("/{gameId}/delete")
    public Game delete(@PathVariable String gameId){
        return service.deleteGameByID(gameId);
    }

    @GetMapping("/players")
    public List<Player> getPlayers(){
        return service.findAllPlayers();
    }

    @PostMapping("/{gameId}/join/{playerId}")
    public Game join(@PathVariable String gameId, @PathVariable String playerId) {
        return service.addPlayer(gameId, playerId);
    }

    @PostMapping("/{gameId}/leave/{playerId}")
    public Game leave(@PathVariable String gameId, @PathVariable String playerId) {
        return service.leavePlayer(gameId, playerId);
    }

    @PostMapping("/{gameId}/start")
    public Game start(@PathVariable String gameId) {
        return service.startGame(gameId);
    }

}
