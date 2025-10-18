package nl.hva.ewa.regenwormen.domain;


import nl.hva.ewa.regenwormen.domain.Enum.TileState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TileTest {

    @Test
    void constructor_setsValuePointsAndDefaults() {
        // Arrange & Act
        Tile tile = new Tile(25);

        // Assert
        assertThat(tile.getValue()).isEqualTo(25);
        assertThat(tile.getPoints()).isEqualTo(2); // 25→2 points
        assertThat(tile.getState()).isEqualTo(TileState.IN_POT);
        assertThat(tile.isFlipped()).isFalse();
        assertThat(tile.getOwner()).isNull();
    }

    @Test
    void constructor_throwsWhenValueOutsideRange() {
        // Arrange, Act & Assert
        assertThatThrownBy(() -> new Tile(20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between");
        assertThatThrownBy(() -> new Tile(37))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void takeTile_assignsOwnerAndChangesState() {
        // Arrange
        Player player = new Player("Alice");
        Tile tile = new Tile(30);

        // Act
        tile.takeTile(player);

        // Assert
        assertThat(tile.getOwner()).isEqualTo(player);
        assertThat(tile.getState()).isEqualTo(TileState.OWNED);
    }

    @Test
    void takeTile_throwsWhenFlipped() {
        // Arrange
        Player player = new Player("Alice");
        Tile tile = new Tile(33);
        tile.flip(); // flipped

        // Act & Assert
        assertThatThrownBy(() -> tile.takeTile(player))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flipped");
    }


    @Test
    void tileToPot_resetsOwnerAndState() {
        // Arrange
        Player player = new Player("Bob");
        Tile tile = new Tile(26);
        tile.takeTile(player);
        assertThat(tile.getOwner()).isEqualTo(player);

        // Act
        tile.tileToPot();

        // Assert
        assertThat(tile.getOwner()).isNull();
        assertThat(tile.getState()).isEqualTo(TileState.IN_POT);
    }

    @Test
    void flip_movesTileToPotAndMarksFlipped() {
        // Arrange
        Player player = new Player("Eve");
        Tile tile = new Tile(29);
        tile.takeTile(player);

        // Act
        tile.flip();

        // Assert
        assertThat(tile.isFlipped()).isTrue();
        assertThat(tile.getOwner()).isNull();
        assertThat(tile.getState()).isEqualTo(TileState.IN_POT);
    }

    @Test
    void calculatePoints_isCorrectForAllRanges() {
        // Arrange & Act & Assert
        assertThat(new Tile(21).getPoints()).isEqualTo(1);
        assertThat(new Tile(24).getPoints()).isEqualTo(1);
        assertThat(new Tile(25).getPoints()).isEqualTo(2);
        assertThat(new Tile(28).getPoints()).isEqualTo(2);
        assertThat(new Tile(29).getPoints()).isEqualTo(3);
        assertThat(new Tile(32).getPoints()).isEqualTo(3);
        assertThat(new Tile(33).getPoints()).isEqualTo(4);
        assertThat(new Tile(36).getPoints()).isEqualTo(4);
    }

    @Test
    void equals_basedOnValueOnly() {
        // Arrange
        Tile t1 = new Tile(22);
        Tile t2 = new Tile(22);
        Tile t3 = new Tile(23);

        // Act & Assert
        assertThat(t1).isEqualTo(t2);
        assertThat(t1).isNotEqualTo(t3);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    void isAvailableInPot_trueWhenInPotAndNotFlipped() {
        // Arrange
        Tile tile = new Tile(24);

        // Act
        boolean available = tile.isAvailableInPot();

        // Assert
        assertThat(available).isTrue();
    }

    @Test
    void isAvailableInPot_falseWhenOwnedOrFlipped() {
        // Arrange
        Player player = new Player("Alice");
        Tile tile = new Tile(24);
        tile.takeTile(player);

        // Act & Assert
        assertThat(tile.isAvailableInPot()).isFalse();

        // Flip en opnieuw checken
        tile.flip();
        assertThat(tile.isAvailableInPot()).isFalse();
    }
}
