package nl.hva.ewa.regenwormen.domain;

import nl.hva.ewa.regenwormen.domain.Enum.TileState;

/**
 * Represents a single tile in the game.
 * Each tile has a value between 21 and 36, and a number of worms (1–4).
 */
public class Tile {
    private final int value;
    private final int points;
    private boolean flipped;
    private TileState state;
    private String ownerPlayerId;

    private static final int MIN_VALUE = 21;
    private static final int MAX_VALUE = 36;

    public Tile(int value) {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new IllegalArgumentException("Tile value must be between "+MIN_VALUE+ " and "+MAX_VALUE);
        }
        this.value = value;
        this.points = calculatePoints(value);
        this.flipped = false;
        this.state = TileState.IN_POT;
        this.ownerPlayerId =  null;
    }

    // --- getters ---
    public int getValue() {return value;}
    public int getPoints() {return points;}
    public boolean isFlipped() { return flipped; }
    public TileState getState() { return state; }
    public String getOwner() { return ownerPlayerId; }

    // ---functies---
    public void flip(){
        if(flipped){return;}
        tileToPot();
        flipped = true;
    }

    public void takeTile(Player player){
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        if (flipped) throw new IllegalStateException("Cannot take a flipped tile");
        state = TileState.OWNED;
        ownerPlayerId = player.getId();
    }

    public void tileToPot(){
        if(state != TileState.OWNED){ return;}
        ownerPlayerId = null;
        state = TileState.IN_POT;
    }


    // --- private helper ---
    private int calculatePoints(int value) {
        if (value <= 24) return 1;
        if (value <= 28) return 2;
        if (value <= 32) return 3;
        return 4;
    }

    public boolean isAvailableInPot() {
        return state == TileState.IN_POT && !flipped;
    }

    // --- equals/hashCode/toString ---
    @Override
    public String toString() {
        return "Tile{" +
                "value=" + value +
                ", points=" + points +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Tile)) return false;
        Tile other = (Tile) obj;
        return value == other.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}
