package nl.hva.ewa.regenwormen.domain;

import java.util.UUID;

public class Player {
    private final UUID id;
    private String name;

    private static final int MAX_NAME_LENGTH = 16;

    public Player(String name) {
        setPlayerNameInternal(name);
        this.id = UUID.randomUUID();
    }

    //getters
    public UUID getId() {return id;}

    public String getName() {return name;}

    //setter
    public void setName(String name) {setPlayerNameInternal(name);}

    //helpers
    private void setPlayerNameInternal(String playerName) {
        if (playerName == null || playerName.isBlank() || playerName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Player name must be 1–" + MAX_NAME_LENGTH + " characters");}
        this.name = playerName.trim();
    }


}
