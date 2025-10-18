package nl.hva.ewa.regenwormen.domain;

import java.util.ArrayList;
import java.util.List;
import nl.hva.ewa.regenwormen.domain.dto.LobbyPlayer;

public class Lobby {
    private int id;
    private String name;
    private int maxPlayers = 4;
    private List<LobbyPlayer> players = new ArrayList<>();

    private boolean countdownActive = false;

    public Lobby() {}

    public Lobby(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public List<LobbyPlayer> getPlayers() { return players; }
    public void setPlayers(List<LobbyPlayer> players) { this.players = players; }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    public boolean allReady() {
        return !players.isEmpty() && players.stream().allMatch(LobbyPlayer::isReady);
    }

    public boolean isCountdownActive() {
        return countdownActive;
    }

    public void setCountdownActive(boolean countdownActive) {
        this.countdownActive = countdownActive;
    }
}
