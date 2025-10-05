package nl.hva.ewa.regenwormen.domain;

import java.util.ArrayList;
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
        return tiles;
    }

    public Tile findTileByValue(int value) {
        for (Tile t : tiles) {
            if (t.getValue() == value) return t;
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


}
