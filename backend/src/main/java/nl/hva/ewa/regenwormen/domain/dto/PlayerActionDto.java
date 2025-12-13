package nl.hva.ewa.regenwormen.domain.dto;

/**
 * DTO voor speler acties via WebSocket (disconnect, reconnect, heartbeat)
 */
public class PlayerActionDto {
    private String gameId;
    private String playerId;

    public PlayerActionDto() {
    }

    public PlayerActionDto(String gameId, String playerId) {
        this.gameId = gameId;
        this.playerId = playerId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    @Override
    public String toString() {
        return "PlayerActionDto{" +
                "gameId='" + gameId + '\'' +
                ", playerId='" + playerId + '\'' +
                '}';
    }
}

