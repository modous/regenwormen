package nl.hva.ewa.regenwormen.domain;

import nl.hva.ewa.regenwormen.domain.dto.PlayersLeaderboardView;
import nl.hva.ewa.regenwormen.controller.GameWebSocketController;

import java.util.List;

public class EndGameHandler {

    private final Game game;
    private final GameWebSocketController wsController;

    public EndGameHandler(Game game, GameWebSocketController wsController) {
        this.game = game;
        this.wsController = wsController;
    }

    /**
     * Controleer of het spel is afgelopen.
     * Als ja, bereken de winnaar, leaderboard en stuur een event naar de frontend.
     */
    public void checkAndHandleEndGame() {
        if (game.getTilesPot().amountAvailableTiles() == 0
                && game.getGameState() == nl.hva.ewa.regenwormen.domain.Enum.GameState.PLAYING) {
            game.endGame(); // berekent leaderboard etc.

            List<PlayersLeaderboardView> leaderboard = game.getLeaderboard();
            String winnerId = leaderboard.isEmpty() ? null : leaderboard.get(0).playerId();

            // Stuur event via WebSocket
            wsController.sendGameEnded(game, winnerId, leaderboard);
        }
    }
}
