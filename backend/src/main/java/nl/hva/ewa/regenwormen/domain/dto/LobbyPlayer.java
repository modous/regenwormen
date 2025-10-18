package nl.hva.ewa.regenwormen.domain.dto;

import java.util.UUID;

public class LobbyPlayer {
    private UUID userId;
    private String username;
    private boolean ready;

    public LobbyPlayer() {}

    public LobbyPlayer(UUID userId, String username) {
        this.userId = userId;
        this.username = username;
        this.ready = false;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
