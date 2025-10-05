package nl.hva.ewa.regenwormen.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TilesPotTest {

    private Player anyPlayer() {
        return mock(Player.class);
    }

    @Test
    @DisplayName("Bij start bevat pot 16 tegels: values 21..36")
    void initial_pot_hasSixteenTiles() {
        // Arrange + Act
        TilesPot pot = new TilesPot();

        // Assert
        List<Tile> all = pot.getTiles();
        assertEquals(16, all.size());
        assertNotNull(pot.findTileByValue(21));
        assertNotNull(pot.findTileByValue(36));
        assertNull(pot.findTileByValue(20));
        assertNull(pot.findTileByValue(37));
    }

    @Test
    @DisplayName("Alle tegels zijn initial beschikbaar (IN_POT & !flipped)")
    void initial_allAvailable() {
        // Arrange
        TilesPot pot = new TilesPot();

        // Act
        List<Tile> available = pot.getAvailableTiles();

        // Assert
        assertEquals(16, available.size());
        assertEquals(16, pot.amountAvailableTiles());
        assertTrue(available.stream().allMatch(Tile::isAvailableInPot));
    }

    @Test
    @DisplayName("Flippen van een tegel in de pot verlaagt beschikbaar aantal")
    void flipping_makesTileUnavailable() {
        // Arrange
        TilesPot pot = new TilesPot();
        Tile t21 = pot.findTileByValue(21);

        // Act
        t21.flip(); // IN_POT -> flipped

        // Assert
        assertFalse(t21.isAvailableInPot());
        assertEquals(15, pot.amountAvailableTiles());
    }

    @Test
    @DisplayName("Tegel pakken (OWNED) verlaagt beschikbaar aantal")
    void taking_reducesAvailable() {
        // Arrange
        TilesPot pot = new TilesPot();
        Tile t22 = pot.findTileByValue(22);
        Player p = anyPlayer();

        // Act
        t22.takeTile(p);

        // Assert
        assertEquals(TileState.OWNED, t22.getState());
        assertEquals(15, pot.amountAvailableTiles());
        assertTrue(pot.getAvailableTiles().stream().noneMatch(t -> t.getValue() == 22));
    }

    @Test
    @DisplayName("Terugleggen in pot zonder flip maakt weer beschikbaar")
    void tileBackToPot_withoutFlip_isAvailable() {
        // Arrange
        TilesPot pot = new TilesPot();
        Tile t23 = pot.findTileByValue(23);
        Player p = anyPlayer();
        t23.takeTile(p);
        assertEquals(15, pot.amountAvailableTiles());

        // Act
        t23.tileToPot(); // jouw implementatie flip’t niet

        // Assert
        assertEquals(TileState.IN_POT, t23.getState());
        assertFalse(t23.isFlipped());
        assertEquals(16, pot.amountAvailableTiles());
    }

    @Test
    @DisplayName("Terugleggen met flip (via flip()) maakt onbeschikbaar in pot")
    void tileBackToPot_withFlip_isUnavailable() {
        // Arrange
        TilesPot pot = new TilesPot();
        Tile t24 = pot.findTileByValue(24);
        Player p = anyPlayer();
        t24.takeTile(p);

        // Act
        t24.flip(); // jouw flip() doet: tileToPot(); flipped=true

        // Assert
        assertEquals(TileState.IN_POT, t24.getState());
        assertTrue(t24.isFlipped());
        assertEquals(15, pot.amountAvailableTiles());
    }
    @Test
    @DisplayName("Hoogste beschikbare tile wordt correct gevonden")
    void highestAvailableTile_returnsCorrect() {
        // Arrange
        TilesPot pot = new TilesPot();
        // Flip hoogste (36) → niet meer beschikbaar
        Tile t36 = pot.findTileByValue(36);
        t36.flip();
        // Take 35 → OWNED → ook niet beschikbaar
        Tile t35 = pot.findTileByValue(35);
        t35.takeTile(mock(Player.class));

        // Act
        Tile highest = pot.getHighestAvailableTile();

        // Assert
        assertNotNull(highest);
        assertEquals(34, highest.getValue());
    }

}
