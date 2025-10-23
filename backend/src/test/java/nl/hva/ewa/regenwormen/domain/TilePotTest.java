package nl.hva.ewa.regenwormen.domain;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class TilesPotTest {

    @Test
    void constructor_createsTilesFrom21To36_andAllAreInitiallyAvailable() {
        TilesPot pot = new TilesPot();

        List<Tile> tiles = pot.getTiles();
        assertThat(tiles).hasSize(16);
        assertThat(tiles).extracting(Tile::getValue).containsExactly(
                21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36
        );
        assertThat(pot.amountAvailableTiles()).isEqualTo(16);
        assertThat(pot.getLowestAvailableTileValue()).isEqualTo(21);
        assertThat(pot.getHighestAvailableTileValue()).isEqualTo(36);
    }

    @Test
    void findAvailableTileByScore_returnsHighestTileLEQScore() {
        TilesPot pot = new TilesPot();

        Tile t1 = pot.findAvailableTileByScore(27);
        Tile t2 = pot.findAvailableTileByScore(36);
        Tile t3 = pot.findAvailableTileByScore(20); // onder minimum

        assertThat(t1).isNotNull();
        assertThat(t1.getValue()).isEqualTo(27);
        assertThat(t2).isNotNull();
        assertThat(t2.getValue()).isEqualTo(36);
        assertThat(t3).isNull();
    }

    @Test
    void getAvailableTiles_excludesOwnedAndFlippedTiles() {
        TilesPot pot = new TilesPot();
        Tile t25 = pot.findTileByValue(25);
        Tile t36 = pot.findTileByValue(36);
        Player alice = new Player("Alice");

        t25.takeTile(alice);  // niet meer beschikbaar
        t36.flip();           // omgedraaid → niet meer beschikbaar

        List<Tile> available = pot.getAvailableTiles();

        assertThat(available).extracting(Tile::getValue)
                .doesNotContain(25, 36);
        assertThat(pot.amountAvailableTiles()).isEqualTo(14); // 16 - 2
    }

    @Test
    void flipHighestAvailableTileIfAny_flipsCurrentHighest_andReducesAvailability() {
        TilesPot pot = new TilesPot();
        int beforeCount = pot.amountAvailableTiles();
        int beforeHighest = pot.getHighestAvailableTileValue();

        pot.flipHighestAvailableTileIfAny();

        int afterCount = pot.amountAvailableTiles();
        int afterHighest = pot.getHighestAvailableTileValue();

        assertThat(afterCount).isEqualTo(beforeCount - 1);
        assertThat(afterHighest).isLessThan(beforeHighest);

        Tile flipped = pot.findTileByValue(beforeHighest);
        assertThat(flipped.isFlipped()).isTrue();
        assertThat(flipped.isAvailableInPot()).isFalse();
    }

    @Test
    void getLowestAndHighestAvailableTile_trackChangesWhenTilesGetOwned() {
        TilesPot pot = new TilesPot();
        Player bob = new Player("Bob");

        Tile low = pot.getLowestAvailableTile();
        Tile high = pot.getHighestAvailableTile();
        low.takeTile(bob);
        high.takeTile(bob);

        assertThat(pot.getLowestAvailableTileValue()).isEqualTo(22); // 21 was weg
        assertThat(pot.getHighestAvailableTileValue()).isEqualTo(35); // 36 was weg
        assertThat(pot.amountAvailableTiles()).isEqualTo(14);
    }

    @Test
    void findTileByValue_returnsTileRegardlessOfState() {
        TilesPot pot = new TilesPot();
        Player p = new Player("Eve");
        Tile t = pot.findTileByValue(30);

        t.takeTile(p); // nu owned, niet meer beschikbaar
        Tile same = pot.findTileByValue(30);

        assertThat(same).isNotNull();
        assertThat(same.getValue()).isEqualTo(30);
        // ✅ FIXED: expect the player ID, not the player object
        assertThat(same.getOwner()).isEqualTo(p.getId());
    }
}
