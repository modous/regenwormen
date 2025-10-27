package nl.hva.ewa.regenwormen.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String id;
    private final String userId;
    private String name;
    private List<Tile> ownedTiles = new ArrayList<>();
    private Diceroll turn;
    private int doublePointsTile = -1;
    private static final int MAX_NAME_LENGTH = 16;

    public Player(String name) {
        setPlayerNameInternal(name);
        this.id = Helpers.generateShortHexId(10);
        this.userId = null;
    }

    public Player(String name, String userId) {
        setPlayerNameInternal(name);
        this.id = Helpers.generateShortHexId(10);
        this.userId = userId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getUser() { return userId; }
    public Diceroll getDiceRoll() { return turn; }
    public int getDoublePointsTile() { return doublePointsTile; }

    public int getPoints() {
        return ownedTiles.stream()
                .filter(t -> t.getOwner() == this.getId())
                .mapToInt(t -> (t.getValue() == doublePointsTile) ? t.getPoints() * 2 : t.getPoints())
                .sum();
    }

    public Tile getTopTile() {
        if (ownedTiles.isEmpty()) return null;
        return ownedTiles.get(ownedTiles.size() - 1);
    }

    public void setName(String name) { setPlayerNameInternal(name); }
    public void setDoublePointsTile(int value) { doublePointsTile = value; }

    public void setStartTurn(Diceroll dices) {
        if (turn != null) throw new IllegalStateException("Throw turn is already active");
        turn = dices;
    }

    public void setEndTurn() { this.turn = null; }

    public void addTile(Tile tile) {
        if (tile == null) throw new IllegalArgumentException("missing tile");
        tile.takeTile(this);
        ownedTiles.add(tile);
    }

    public void loseTopTileToStack() {
        Tile topTile = getTopTile();
        if (topTile == null) throw new IllegalStateException("No tiles owned");
        ownedTiles.remove(topTile);
    }

    private void setPlayerNameInternal(String playerName) {
        if (playerName == null || playerName.isBlank() || playerName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Player name must be 1–" + MAX_NAME_LENGTH + " characters");
        }
        this.name = playerName.trim();
    }

    public void returnAllTilesToPot() {
        while (getTopTile() != null) {
            Tile top = getTopTile();
            top.tileToPot();
            loseTopTileToStack();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Player)) return false;
        Player other = (Player) obj;
        return id.equals(other.id);
    }

    // JSON serialization helpers
    @JsonProperty("points")
    public int jsonPoints() { return getPoints(); }

    @JsonProperty("topTile")
    public Tile jsonTopTile() { return getTopTile(); }

    @JsonIgnore
    public List<Tile> getOwnedTiles() { return ownedTiles; }

    @JsonProperty("tiles")
    public List<Tile> jsonTiles() { return ownedTiles; }
}
