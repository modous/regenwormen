package nl.hva.ewa.regenwormen.service;

import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Player;
import nl.hva.ewa.regenwormen.domain.dto.EndTurnView;
import nl.hva.ewa.regenwormen.domain.dto.TurnView;
import nl.hva.ewa.regenwormen.repository.GameRepository;
import nl.hva.ewa.regenwormen.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InGameService {
    private final GameRepository gameRepo;
    private final PlayerRepository playerRepo;

    @Autowired
    public InGameService(GameRepository gameRepo, PlayerRepository playerRepo) {
        this.gameRepo = gameRepo;
        this.playerRepo = playerRepo;
    }

    public TurnView startAndRollRoundZero(String gameId, String playerId){
        Game game = gameRepo.findById(gameId);
        if (game == null) {throw new IllegalArgumentException("Game with id: " + gameId + " does not exist");}
        Player player = playerRepo.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Player not found"));

        TurnView turnviewFirstRoll = game.startAndRollRoundZero(player);
        gameRepo.save(game);

        return turnviewFirstRoll;
    }

    public TurnView pickDiceFaceZero(String gameId, String playerId, DiceFace diceface){
        Game game = gameRepo.findById(gameId);
        if (game == null) {throw new IllegalArgumentException("Game with id: " + gameId + " does not exist");}
        Player player = playerRepo.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Player not found"));

        TurnView turnviewChosenFace = game.pickDiceFaceZero(player, diceface);
        gameRepo.save(game);

        return turnviewChosenFace;
    }

    public TurnView reRollRoundZero(String gameId, String playerId){
        Game game = gameRepo.findById(gameId);
        if (game == null) {throw new IllegalArgumentException("Game with id: " + gameId + " does not exist");}
        Player player = playerRepo.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Player not found"));

        TurnView turnviewReroll = game.reRollRoundZero(player);
        gameRepo.save(game);

        return turnviewReroll;
    }

    public EndTurnView finishRoundZero(String gameId, String playerId) {
        Game game = gameRepo.findById(gameId);
        if (game == null) {throw new IllegalArgumentException("Game with id: " + gameId + " does not exist");}
        Player player = playerRepo.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Player not found"));

        EndTurnView endTurnView = game.finishRoundZero(player);
        gameRepo.save(game);

        return endTurnView;
    }
}
