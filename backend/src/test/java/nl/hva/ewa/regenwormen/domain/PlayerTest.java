package nl.hva.ewa.regenwormen.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PlayerTest {

    @Test
    void constructor_setsIdAndName_andValidatesLength() {
        // Arrange & Act
        Player p = new Player("Alice");

        // Assert
        assertThat(p.getId()).isNotBlank();
        assertThat(p.getName()).isEqualTo("Alice");

        // Arrange/Act/Assert invalid
        assertThatThrownBy(() -> new Player(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Player("ABCDEFGHIJKLMNOPQ")) // 17 chars
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addTile_setsOwnerAndAddsToStack_affectsPoints() {
        // Arrange
        Player p = new Player("Bob");
        Tile t25 = new Tile(25); // 2 points
        Tile t33 = new Tile(33); // 4 points

        // Act
        p.addTile(t25); // addTile roept takeTile(this) aan
        p.addTile(t33);

        // Assert
        assertThat(p.getTopTile()).isEqualTo(t33);
        assertThat(t25.getOwner()).isEqualTo(p);
        assertThat(t33.getOwner()).isEqualTo(p);
        assertThat(p.getPoints()).isEqualTo(2 + 4);

        // Arrange: double points voor 33
        p.setDoublePointsTile(33);

        // Act
        int pts = p.getPoints();

        // Assert
        assertThat(pts).isEqualTo(2 + 4 * 2);
    }

    @Test
    void getTopTile_returnsLastOrNull_andLoseTopTileRemovesIt() {
        // Arrange
        Player p = new Player("Dan");

        // Act & Assert
        assertThat(p.getTopTile()).isNull();

        Tile t21 = new Tile(21);
        p.addTile(t21);
        assertThat(p.getTopTile()).isEqualTo(t21);

        Tile t22 = new Tile(22);
        p.addTile(t22);
        assertThat(p.getTopTile()).isEqualTo(t22);

        // Act
        p.loseTopTileToStack();

        // Assert
        assertThat(p.getTopTile()).isEqualTo(t21);
    }

    @Test
    void loseTopTileToStack_throwsWhenEmpty() {
        // Arrange
        Player p = new Player("Eve");

        // Act & Assert
        assertThatThrownBy(p::loseTopTileToStack)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No tiles owned");
    }

    @Test
    void setName_validatesAndTrims() {
        // Arrange
        Player p = new Player("  Frank  ");

        // Act
        p.setName("  New Name  ");

        // Assert
        assertThat(p.getName()).isEqualTo("New Name");
        assertThatThrownBy(() -> p.setName(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> p.setName("ABCDEFGHIJKLMNOPQ"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
