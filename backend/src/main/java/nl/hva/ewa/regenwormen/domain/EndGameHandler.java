package nl.hva.ewa.regenwormen.domain;

import nl.hva.ewa.regenwormen.controller.GameWebSocketController;
import nl.hva.ewa.regenwormen.domain.Enum.GameState;
import nl.hva.ewa.regenwormen.domain.dto.PlayersLeaderboardView;
import nl.hva.ewa.regenwormen.entity.GameResult;
import nl.hva.ewa.regenwormen.repository.GameResultRepository;

import java.util.List;

public class EndGameHandler {

    private final Game game;
    private final GameWebSocketController wsController;
    private final GameResultRepository gameResultRepository;

    public EndGameHandler(
            Game game,
            GameWebSocketController wsController,
            GameResultRepository gameResultRepository
    ) {
        this.game = game;
        this.wsController = wsController;
        this.gameResultRepository = gameResultRepository;
    }

    /**
     * Wordt veilig meerdere keren aangeroepen.
     * Slaat de game maar één keer op zodra hij ENDED is.
     */
    public void checkAndHandleEndGame() {

        if (game.getTilesPot() == null) return;
        if (game.getTilesPot().amountAvailableTiles() != 0) return;

        // 🛑 voorkom dubbel opslaan
        if (gameResultRepository.existsByGameId(game.getId())) {
            return;
        }

        System.out.println("🏁 GAME FINISHED → saving results for " + game.getId());

        game.endGame(); // leaderboard berekenen (in memory is prima)

        List<PlayersLeaderboardView> leaderboard = game.getLeaderboard();
        String winnerId = leaderboard.isEmpty() ? null : leaderboard.get(0).playerId();

        String winnerUsername = winnerId == null ? null :
                game.getPlayers().stream()
                        .filter(p -> p.getId().equals(winnerId))
                        .map(Player::getName)
                        .findFirst()
                        .orElse(null);

        List<String> players = game.getPlayers()
                .stream()
                .map(Player::getName)
                .toList();

        for (Player p : game.getPlayers()) {

            boolean won = p.getId().equals(winnerId);

            int score = leaderboard.stream()
                    .filter(l -> l.playerId().equals(p.getId()))
                    .map(PlayersLeaderboardView::points)
                    .findFirst()
                    .orElse(0);

            GameResult result = new GameResult(
                    game.getId(),
                    p.getName(),
                    won,
                    score,
                    players.size(),
                    0,
                    winnerUsername,
                    players
            );

            gameResultRepository.save(result);

            System.out.println("✅ Saved result for " + p.getName() + " | won=" + won);
        }

        wsController.sendGameEnded(game, winnerId, leaderboard);
    }

}
