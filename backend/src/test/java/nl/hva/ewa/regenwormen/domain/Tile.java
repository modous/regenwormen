package nl.hva.ewa.regenwormen.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TileTest {

    private Player anyPlayer() {
        return mock(Player.class);
    }

    // ---------- Constructor & getters ----------
    @Nested
    @DisplayName("Constructor & basisinvariant")
    class CtorAndBasics {

        @Test
        @DisplayName("Geldige waarde initialiseert IN_POT, not flipped, owner=null")
        void ctor_valid_initialState() {
            // Arrange
            int value = 21;

            // Act
            Tile t = new Tile(value);

            // Assert
            assertEquals(21, t.getValue());
            assertEquals(TileState.IN_POT, t.getState());
            assertFalse(t.isFlipped());
            assertNull(t.getOwner());
            assertTrue(t.isAvailableInPot());
        }

        @ParameterizedTest(name = "value={0} -> points={1}")
        @CsvSource({
                "21,1","22,1","23,1","24,1",
                "25,2","26,2","27,2","28,2",
                "29,3","30,3","31,3","32,3",
                "33,4","34,4","35,4","36,4"
        })
        @DisplayName("Points mapping klopt t.o.v. waarde")
        void pointsMapping(int value, int expectedPoints) {
            // Arrange & Act
            Tile t = new Tile(value);
            // Assert
            assertEquals(expectedPoints, t.getPoints());
        }

        @ParameterizedTest(name = "invalid value {0} should throw")
        @CsvSource({"-1","0","20","37","100"})
        @DisplayName("Ongeldige waarde gooit IllegalArgumentException")
        void ctor_invalid_throws(int invalid) {
            // Arrange + Act + Assert
            IllegalArgumentException ex =
                    assertThrows(IllegalArgumentException.class, () -> new Tile(invalid));
            assertTrue(ex.getMessage().toLowerCase().contains("between"));
        }
    }

    // ---------- Eigenaarschap / stelen ----------
    @Nested
    @DisplayName("Eigenaarschap & stelen")
    class Ownership {

        @Test
        @DisplayName("takeTile(player) vanuit pot zet OWNED en owner")
        void take_fromPot_setsOwnedAndOwner() {
            // Arrange
            Tile t = new Tile(25);
            Player p = anyPlayer();
            assertEquals(TileState.IN_POT, t.getState());

            // Act
            t.takeTile(p);

            // Assert
            assertEquals(TileState.OWNED, t.getState());
            assertEquals(p, t.getOwner());
            assertFalse(t.isFlipped());
            assertFalse(t.isAvailableInPot());
        }

        @Test
        @DisplayName("takeTile(null) doet niets")
        void take_null_noop() {
            // Arrange
            Tile t = new Tile(25);

            // Act
            t.takeTile(null);

            // Assert
            assertEquals(TileState.IN_POT, t.getState());
            assertNull(t.getOwner());
        }

        @Test
        @DisplayName("Stelen toegestaan: tweede takeTile verandert owner (blijft OWNED)")
        void steal_allowed_secondTake_changesOwner() {
            // Arrange
            Tile t = new Tile(26);
            Player a = anyPlayer();
            Player b = anyPlayer();
            t.takeTile(a);
            assertEquals(a, t.getOwner());

            // Act
            t.takeTile(b);

            // Assert
            assertEquals(TileState.OWNED, t.getState());
            assertEquals(b, t.getOwner());
        }
    }

    // ---------- Terug naar pot / flip ----------
    @Nested
    @DisplayName("Terugleggen & flipregels")
    class ReturnAndFlip {

        @Test
        @DisplayName("tileToPot() vanuit OWNED zet IN_POT, owner=null")
        void tileToPot_fromOwned_resetsOwnerAndState() {
            // Arrange
            Tile t = new Tile(30);
            Player p = anyPlayer();
            t.takeTile(p);
            assertEquals(TileState.OWNED, t.getState());

            // Act
            t.tileToPot();

            // Assert
            assertEquals(TileState.IN_POT, t.getState());
            assertNull(t.getOwner());
            assertFalse(t.isFlipped()); // tileToPot() forceert geen flip
            assertTrue(t.isAvailableInPot());
        }

        @Test
        @DisplayName("flip() eindigt altijd in pot en zet flipped=true")
        void flip_alwaysEndsInPot_setsFlipped() {
            // Arrange
            Tile t = new Tile(33);
            Player p = anyPlayer();
            t.takeTile(p); // OWNED

            // Act
            t.flip();

            // Assert
            assertEquals(TileState.IN_POT, t.getState()); // jouw flip() roept tileToPot() aan
            assertNull(t.getOwner());
            assertTrue(t.isFlipped());
            assertFalse(t.isAvailableInPot()); // flipped in pot is onbeschikbaar
        }

        @Test
        @DisplayName("flip() is idempotent: tweede flip verandert niets")
        void flip_idempotent() {
            // Arrange
            Tile t = new Tile(34);

            // Act
            t.flip();
            t.flip();

            // Assert
            assertTrue(t.isFlipped());
            assertEquals(TileState.IN_POT, t.getState());
        }
    }

    // ---------- equals / hashCode ----------
    @Nested
    @DisplayName("Gelijkheid & hashCode")
    class Equality {

        @Test
        @DisplayName("Tiles met dezelfde value zijn gelijk (uniek per waarde)")
        void equals_sameValue_true() {
            // Arrange
            Tile a = new Tile(24);
            Tile b = new Tile(24);

            // Act
            boolean eq = a.equals(b);

            // Assert
            assertTrue(eq);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Tiles met verschillende value zijn ongelijk")
        void equals_diffValue_false() {
            // Arrange
            Tile a = new Tile(24);
            Tile b = new Tile(25);

            // Act + Assert
            assertFalse(a.equals(b));
        }
    }
}
