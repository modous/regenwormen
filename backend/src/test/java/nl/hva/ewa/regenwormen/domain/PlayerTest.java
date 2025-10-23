package nl.hva.ewa.regenwormen.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PlayerTest {

    @Test
    void constructor_setsIdAndName_andValidatesLength() {
        Player p = new Player("Alice");

        assertThat(p.getId()).isNotBlank();
        assertThat(p.getName()).isEqualTo("Alice");

        assertThatThrownBy(() -> new Player(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Player("ABCDEFGHIJKLMNOPQ"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addTile_setsOwnerAndAddsToStack_affectsPoints() {
        Player p = new Player("Bob");
        Tile t25 = new Tile(25); // 2 points
        Tile t33 = new Tile(33); // 4 points

        p.addTile(t25);
        p.addTile(t33);

        assertThat(p.getTopTile()).isEqualTo(t33);

        // ✅ Updated here — expect the player ID, not the player object
        assertThat(t25.getOwner()).isEqualTo(p.getId());
        assertThat(t33.getOwner()).isEqualTo(p.getId());

        assertThat(p.getPoints()).isEqualTo(2 + 4);

        p.setDoublePointsTile(33);
        int pts = p.getPoints();
        assertThat(pts).isEqualTo(2 + 4 * 2);
    }

    @Test
    void getTopTile_returnsLastOrNull_andLoseTopTileRemovesIt() {
        Player p = new Player("Dan");

        assertThat(p.getTopTile()).isNull();

        Tile t21 = new Tile(21);
        p.addTile(t21);
        assertThat(p.getTopTile()).isEqualTo(t21);

        Tile t22 = new Tile(22);
        p.addTile(t22);
        assertThat(p.getTopTile()).isEqualTo(t22);

        p.loseTopTileToStack();

        assertThat(p.getTopTile()).isEqualTo(t21);
    }

    @Test
    void loseTopTileToStack_throwsWhenEmpty() {
        Player p = new Player("Eve");

        assertThatThrownBy(p::loseTopTileToStack)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No tiles owned");
    }

    @Test
    void setName_validatesAndTrims() {
        Player p = new Player("  Frank  ");

        p.setName("  New Name  ");

        assertThat(p.getName()).isEqualTo("New Name");
        assertThatThrownBy(() -> p.setName(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> p.setName("ABCDEFGHIJKLMNOPQ"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
