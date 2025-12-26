package nl.hva.ewa.regenwormen.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String gameId;

    private String username; // voor wie deze record is

    private boolean won;

    private int score;

    private int playerCount;

    private int durationMin;

    private String winnerUsername;

    private LocalDateTime finishedAt;

    @ElementCollection
    private List<String> players;

    // JPA requires no-arg constructor
    protected GameResult() {}

    public GameResult(
            String gameId,
            String username,
            boolean won,
            int score,
            int playerCount,
            int durationMin,
            String winnerUsername,
            List<String> players
    ) {
        this.gameId = gameId;
        this.username = username;
        this.won = won;
        this.score = score;
        this.playerCount = playerCount;
        this.durationMin = durationMin;
        this.winnerUsername = winnerUsername;
        this.players = players;
        this.finishedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getGameId() { return gameId; }
    public String getUsername() { return username; }
    public boolean isWon() { return won; }
    public int getScore() { return score; }
    public int getPlayerCount() { return playerCount; }
    public int getDurationMin() { return durationMin; }
    public String getWinnerUsername() { return winnerUsername; }
    public List<String> getPlayers() { return players; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
}
