package nl.hva.ewa.regenwormen.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TilesPot {
    private List<Tile> tiles = new ArrayList<>();

    private static final int MIN_VALUE = 21;
    private static final int MAX_VALUE = 36;
    
    public TilesPot(){
        createTiles();
    }
    
    private void createTiles(){
        if(tiles.size() > 0){return;}
        for(int i = MIN_VALUE; i <= MAX_VALUE; i++){
            tiles.add(new Tile(i));
        }
    }

    public List<Tile> getTiles() {
        return Collections.unmodifiableList(tiles);
    }

    public Tile findTileByValue(int value) {
        for (Tile t : tiles) {
            if (t.getValue() == value) return t;
        }
        return null;
    }

    public Tile findAvailableTileByScore(int score){
        List<Tile> availableTiles = getAvailableTiles();
        availableTiles.sort(Comparator.comparingInt(Tile::getValue).reversed());
        for (Tile tile : availableTiles) {
            if (tile.getValue() <= score) {
                return tile;
            }
        }
        return null;
    }

    public List<Tile> getAvailableTiles(){
        List<Tile> available = new ArrayList<>();
        for (Tile t : tiles) {
            if (t.isAvailableInPot()) {
                available.add(t);
            }
        }
        return available;
    }

    public int amountAvailableTiles(){
        return getAvailableTiles().size();
    }

    public Tile getHighestAvailableTile(){
        Tile highest = null;
        for (Tile t : tiles) {
            if (t.isAvailableInPot()) {
                if (highest == null || t.getValue() > highest.getValue()) {
                    highest = t;
                }
            }
        }
        return highest;
    }
    public int getHighestAvailableTileValue () {
        Tile HighestTile = getHighestAvailableTile();
        if (HighestTile == null){return 0;}
        int highestTileValue = HighestTile.getValue();
        return highestTileValue;
    }

    public void flipHighestAvailableTileIfAny() {
        Tile highest = getHighestAvailableTile();
        if (highest != null) {
            highest.flip();
        }
    }

    public Tile getLowestAvailableTile() {
        Tile lowest = null;
        for (Tile t : tiles) {
            if (t.isAvailableInPot()) {
                if (lowest == null || t.getValue() < lowest.getValue()) {
                    lowest = t;
                }
            }
        }
        return lowest;
    }

    public int getLowestAvailableTileValue () {
        Tile lowestTile = getLowestAvailableTile();
        if (lowestTile == null){return 0;}
        int lowestTileValue = lowestTile.getValue();
        return lowestTileValue;
    }
}
