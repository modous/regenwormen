package nl.hva.ewa.regenwormen.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void createsPlayerWithValidName() {
        // Arrange
        String name = "Alice";

        // Act
        Player player = new Player(name);

        // Assert
        assertNotNull(player.getId());
        assertEquals("Alice", player.getName());
    }

    @Test
    void trimsWhitespaceFromName() {
        // Arrange
        String nameWithSpaces = "  Bob  ";

        // Act
        Player player = new Player(nameWithSpaces);

        // Assert
        assertEquals("Bob", player.getName());
    }

    @Test
    void rejectsNullName() {
        // Arrange
        String name = null;

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> new Player(name));
    }

    @Test
    void rejectsBlankName() {
        // Arrange
        String blank = "   ";

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> new Player(blank));
    }

    @Test
    void rejectsTooLongName() {
        // Arrange
        String tooLong = "x".repeat(17); // MAX_NAME_LENGTH = 16

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> new Player(tooLong));
    }

    @Test
    void setNameUpdatesWhenValid() {
        // Arrange
        Player player = new Player("Ann");

        // Act
        player.setName("Eve");

        // Assert
        assertEquals("Eve", player.getName());
    }

    @Test
    void setNameRejectsInvalid() {
        // Arrange
        Player player = new Player("Ann");

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> player.setName(" ".repeat(5)));
        assertThrows(IllegalArgumentException.class, () -> player.setName("x".repeat(17)));
    }
}
