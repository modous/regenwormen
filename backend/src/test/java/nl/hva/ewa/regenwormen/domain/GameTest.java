package nl.hva.ewa.regenwormen.domain;

import nl.hva.ewa.regenwormen.domain.Enum.GameState;
import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests voor Game.java volgens Arrange-Act-Assert principe.
 * Externe domeinklassen worden zo nodig gesimuleerd met eenvoudige stubs.
 */
class GameTest {

    private Game game;

    // --- hulpfuncties ---
    private static void setField(Object target, String field, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T getField(Object target, String field) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return (T) f.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        game = new Game("TestGame", 4);
    }

    // Stub Player (minimale logica)
    static class StubPlayer extends Player {
        private final String id;
        private final String name;
        private int doubleTile = -1;
        private Tile topTile;
        private Diceroll roll = new Diceroll();
        private int points = 0;
        boolean endTurnCalled = false;

        StubPlayer(String id, String name) {
            super(name);
            this.id = id;
            this.name = name;
        }

        @Override public String getId() { return id; }
        @Override public String getName() { return name; }
        @Override public int getDoublePointsTile() { return doubleTile; }
        @Override public void setDoublePointsTile(int val) { doubleTile = val; }
        @Override public Tile getTopTile() { return topTile; }
        @Override public int getPoints() { return points; }
        @Override public void setStartTurn(Diceroll d) { this.roll = d; }
        @Override public void setEndTurn() { endTurnCalled = true; }
        @Override public Diceroll getDiceRoll() { return roll; }
    }

    // --- TESTS ---

    @Test
    void constructor_initializes_defaults() {
        // Arrange & Act: constructor is in setUp

        // Assert
        assertEquals("TestGame", game.getGameName());
        assertEquals(GameState.PRE_GAME, game.getGameState());
        assertEquals(0, game.playersAmount());
        assertTrue(game.getPlayers().isEmpty());
        assertNotNull(game.getId());
    }

    @Test
    void addPlayer_succeeds_before_game_start() {
        // Arrange
        StubPlayer p1 = new StubPlayer("1", "Alice");

        // Act
        boolean added = game.addPlayer(p1);

        // Assert
        assertTrue(added);
        assertEquals(1, game.playersAmount());
        assertTrue(game.getPlayers().contains(p1));
    }

    @Test
    void addPlayer_fails_if_already_in_game() {
        // Arrange
        StubPlayer p1 = new StubPlayer("1", "Alice");
        game.addPlayer(p1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> game.addPlayer(p1));
    }

    @Test
    void addPlayer_fails_if_game_already_started() {
        // Arrange
        StubPlayer p1 = new StubPlayer("1", "Alice");
        game.addPlayer(p1);
        StubPlayer p2 = new StubPlayer("2", "Bob");
        game.addPlayer(p2);

        game.startGame(); // now PLAYING

        // Act + Assert
        assertThrows(IllegalStateException.class, () -> game.addPlayer(new StubPlayer("3", "Eve")));
    }

    @Test
    void leavePlayer_removes_player_in_pre_game() {
        // Arrange
        StubPlayer p1 = new StubPlayer("1", "Alice");
        game.addPlayer(p1);

        // Act
        boolean result = game.leavePlayer(p1.getId());

        // Assert
        assertTrue(result);
        assertEquals(0, game.playersAmount());
    }

    @Test
    void leavePlayer_invalid_id_throws() {
        assertThrows(IllegalArgumentException.class, () -> game.leavePlayer(null));
        assertThrows(IllegalArgumentException.class, () -> game.leavePlayer(""));
    }

    @Test
    void leavePlayer_not_in_game_throws() {
        StubPlayer p1 = new StubPlayer("1", "Alice");
        game.addPlayer(p1);
        assertThrows(IllegalArgumentException.class, () -> game.leavePlayer("999"));
    }

    @Test
    void startGame_transitions_to_PLAYING_and_inits_tilespot() {
        // Arrange
        game.addPlayer(new StubPlayer("1", "A"));
        game.addPlayer(new StubPlayer("2", "B"));

        // Act
        game.startGame();

        // Assert
        assertEquals(GameState.PLAYING, game.getGameState());
        assertNotNull(game.getTilesPot());
        assertEquals(Integer.valueOf(0), getField(game, "round"));

    }

    @Test
    void startGame_too_few_players_throws() {
        // Arrange
        game.addPlayer(new StubPlayer("1", "A"));

        // Act + Assert
        assertThrows(IllegalStateException.class, () -> game.startGame());
    }


    @Test
    void startGame_sets_roundZero_for_each_player() {
        // Arrange
        StubPlayer p1 = new StubPlayer("1", "A");
        StubPlayer p2 = new StubPlayer("2", "B");
        game.addPlayer(p1);
        game.addPlayer(p2);

        // Act
        game.startGame();

        // Assert
        assertNotNull(p1.getDiceRoll());
        assertNotNull(p2.getDiceRoll());
        assertEquals(GameState.PLAYING, game.getGameState());
    }

    @Test
    void setNextPlayersTurn_wraps_to_zero_and_increments_round() {
        // Arrange
        List<Player> ps = List.of(new StubPlayer("1", "A"), new StubPlayer("2", "B"));
        setField(game, "players", new ArrayList<>(ps));
        setField(game, "currentPlayersTurnIndex", 1); // laatste
        setField(game, "round", 5);

        // Act
        game.setNextPlayersTurn();

        // Assert
        assertEquals(Integer.valueOf(0), game.getTurnIndex());
    }

    @Test
    void findPlayerById_returns_correct_player() {
        // Arrange
        StubPlayer p1 = new StubPlayer("1", "A");
        StubPlayer p2 = new StubPlayer("2", "B");
        game.addPlayer(p1);
        game.addPlayer(p2);

        // Act
        Player found = game.findPlayerById("2");

        // Assert
        assertEquals(p2, found);
    }

    @Test
    void hasPlayer_returns_true_if_player_exists() {
        StubPlayer p1 = new StubPlayer("1", "A");
        game.addPlayer(p1);
        assertTrue(game.hasPlayer(p1));
    }

    @Test
    void endGame_switches_state_to_ENDED_and_creates_leaderboard() {
        // Arrange
        StubPlayer p1 = new StubPlayer("1", "A");
        StubPlayer p2 = new StubPlayer("2", "B");
        game.addPlayer(p1);
        game.addPlayer(p2);
        game.startGame();

        // Act
        game.endGame();

        // Assert
        assertEquals(GameState.ENDED, game.getGameState());
        assertNotNull(getField(game, "leaderboard"));
    }

    @Test
    void hasMinValueToStop_true_when_enough_points() {
        // Arrange
        game.addPlayer(new StubPlayer("1", "A"));
        game.addPlayer(new StubPlayer("2", "B"));
        game.startGame();

        TilesPot mockPot = new TilesPot() {
            @Override
            public int getLowestAvailableTileValue() { return 10; }
        };
        setField(game, "tilesPot", mockPot);

        // Act
        boolean result = game.hasMinValueToStop(15);

        // Assert
        assertTrue(result);
    }

    @Test
    void hasMinValueToStop_false_when_too_low_and_no_matching_tile() {
        // Arrange
        game.addPlayer(new StubPlayer("1", "A"));
        game.addPlayer(new StubPlayer("2", "B"));
        game.startGame();

        TilesPot mockPot = new TilesPot() {
            @Override
            public int getLowestAvailableTileValue() { return 20; }
        };
        setField(game, "tilesPot", mockPot);

        // Act
        boolean result = game.hasMinValueToStop(10);

        // Assert
        assertFalse(result);
    }

    @Test
    void roundZerounfinished_true_when_any_player_still_negative() {
        // Arrange
        StubPlayer p1 = new StubPlayer("1", "A");
        StubPlayer p2 = new StubPlayer("2", "B");
        p1.setDoublePointsTile(22);
        p2.setDoublePointsTile(-1);

        List<Player> ps = List.of(p1, p2);
        setField(game, "players", new ArrayList<>(ps));

        // Act
        boolean result = game.roundZerounfinished();

        // Assert
        assertTrue(result);
    }

    @Test
    void equals_true_for_same_id() {
        // Arrange
        Game g1 = new Game("X", 2);
        Game g2 = new Game("Y", 2);
        setField(g2, "id", getField(g1, "id")); // force same id

        // Act + Assert
        assertEquals(g1, g2);
    }

    @Test
    void equals_false_for_different_id() {
        // Arrange
        Game g1 = new Game("X", 2);
        Game g2 = new Game("Y", 2);

        // Assert
        assertNotEquals(g1, g2);
    }
}
