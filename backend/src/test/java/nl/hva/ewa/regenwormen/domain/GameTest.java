package nl.hva.ewa.regenwormen.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void createsGameWithValidValues() {
        // Arrange
        String title = "Regenwormen";
        int maxPlayers = 4;

        // Act
        Game game = new Game(title, maxPlayers);

        // Assert
        assertNotNull(game.getId());
        assertEquals("Regenwormen", game.getGameName());
        assertEquals(4, game.getMaxPlayers());
        assertEquals(GameState.PRE_GAME, game.getGameState());
        assertEquals(0, game.playersAmount());
    }

    @Test
    void rejectsInvalidNameOrMaxPlayers() {
        // Arrange
        String empty = "";
        String tooLong = "X".repeat(17); // MAX_NAME_LENGTH = 16

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> new Game(empty, 4));
        assertThrows(IllegalArgumentException.class, () -> new Game(tooLong, 4));
        assertThrows(IllegalArgumentException.class, () -> new Game("OK", 1));
        assertThrows(IllegalArgumentException.class, () -> new Game("OK", 9));
    }

    @Test
    void addPlayerSucceedsInPreGame() {
        // Arrange
        Game game = new Game("Test", 2);
        Player alice = new Player("Alice");

        // Act
        boolean added = game.addPlayer(alice);

        // Assert
        assertTrue(added);
        assertEquals(1, game.playersAmount());
        assertEquals("Alice", game.getPlayers().get(0).getName());
    }

    @Test
    void addPlayerFailsAfterGameStarts() {
        // Arrange
        Game game = new Game("Test", 2);
        game.addPlayer(new Player("Alice"));
        game.addPlayer(new Player("Bob"));
        game.startGame();

        // Act + Assert
        assertThrows(IllegalStateException.class, () -> game.addPlayer(new Player("Charlie")));
    }

    @Test
    void startGameNeedsAtLeastTwoPlayers() {
        // Arrange
        Game game = new Game("Test", 3);
        game.addPlayer(new Player("Alice"));

        // Act + Assert
        assertThrows(IllegalStateException.class, game::startGame);
    }

    @Test
    void startGameSetsStateAndTurn() {
        // Arrange
        Game game = new Game("Test", 3);
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        game.addPlayer(p1);
        game.addPlayer(p2);

        // Act
        game.startGame();

        // Assert
        assertEquals(GameState.PLAYING, game.getGameState());
        assertEquals(p1, game.getPlayersTurn());
        assertEquals(0, game.getTurnIndex());
    }

    @Test
    void turnCyclesThroughPlayers() {
        // Arrange
        Game game = new Game("Test", 3);
        Player p1 = new Player("A");
        Player p2 = new Player("B");
        Player p3 = new Player("C");
        game.addPlayer(p1);
        game.addPlayer(p2);
        game.addPlayer(p3);
        game.startGame();

        // Act
        game.setNextPlayersTurn(); // -> B
        Player afterOne = game.getPlayersTurn();
        game.setNextPlayersTurn(); // -> C
        Player afterTwo = game.getPlayersTurn();
        game.setNextPlayersTurn(); // -> A
        Player afterThree = game.getPlayersTurn();

        // Assert
        assertEquals(p2, afterOne);
        assertEquals(p3, afterTwo);
        assertEquals(p1, afterThree);
    }

    @Test
    void endGameTransitionsToEnded() {
        // Arrange
        Game game = new Game("Test", 2);
        game.addPlayer(new Player("A"));
        game.addPlayer(new Player("B"));
        game.startGame();

        // Act
        game.endGame();

        // Assert
        assertEquals(GameState.ENDED, game.getGameState());
    }
}
