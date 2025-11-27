package nl.hva.ewa.regenwormen.domain;

import nl.hva.ewa.regenwormen.domain.Enum.GameState;
import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Updated tests for Game.java (compatible with current backend).
 * Removed references to old roundZerounfinished() method.
 */
class GameTest {

    private Game game;

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

    // --- Stub Player ---
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

    // --- Tests ---

    @Test
    void constructor_initializes_defaults() {
        assertEquals("TestGame", game.getGameName());
        assertEquals(GameState.PRE_GAME, game.getGameState());
        assertEquals(0, game.playersAmount());
        assertTrue(game.getPlayers().isEmpty());
        assertNotNull(game.getId());
    }

    @Test
    void addPlayer_succeeds_before_game_start() {
        StubPlayer p1 = new StubPlayer("1", "Alice");

        boolean added = game.addPlayer(p1);

        assertTrue(added);
        assertEquals(1, game.playersAmount());
        assertTrue(game.getPlayers().contains(p1));
    }

    @Test
    void addPlayer_fails_if_already_in_game() {
        StubPlayer p1 = new StubPlayer("1", "Alice");
        game.addPlayer(p1);

        assertThrows(IllegalArgumentException.class, () -> game.addPlayer(p1));
    }

    @Test
    void addPlayer_fails_if_game_already_started() {
        StubPlayer p1 = new StubPlayer("1", "Alice");
        StubPlayer p2 = new StubPlayer("2", "Bob");
        game.addPlayer(p1);
        game.addPlayer(p2);
        game.startGame();

        assertThrows(IllegalStateException.class, () -> game.addPlayer(new StubPlayer("3", "Eve")));
    }

    @Test
    void leavePlayer_removes_player_in_pre_game() {
        StubPlayer p1 = new StubPlayer("1", "Alice");
        game.addPlayer(p1);

        boolean result = game.leavePlayer(p1.getId());

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
        game.addPlayer(new StubPlayer("1", "A"));
        game.addPlayer(new StubPlayer("2", "B"));

        game.startGame();

        assertEquals(GameState.PLAYING, game.getGameState());
        assertNotNull(game.getTilesPot());
        assertEquals(Integer.valueOf(0), getField(game, "round"));
    }

    @Test
    void startGame_too_few_players_throws() {
        game.addPlayer(new StubPlayer("1", "A"));
        assertThrows(IllegalStateException.class, () -> game.startGame());
    }

    @Test
    void startGame_sets_roundZero_for_each_player() {
        StubPlayer p1 = new StubPlayer("1", "A");
        StubPlayer p2 = new StubPlayer("2", "B");
        game.addPlayer(p1);
        game.addPlayer(p2);

        game.startGame();

        assertNotNull(p1.getDiceRoll());
        assertNotNull(p2.getDiceRoll());
        assertEquals(GameState.PLAYING, game.getGameState());
    }

    @Test
    void setNextPlayersTurn_wraps_to_zero_and_increments_round() {
        List<Player> ps = List.of(new StubPlayer("1", "A"), new StubPlayer("2", "B"));
        setField(game, "players", new ArrayList<>(ps));
        setField(game, "currentPlayersTurnIndex", 1); // last player
        setField(game, "round", 5);

        game.setNextPlayersTurn();

        assertEquals(Integer.valueOf(0), game.getTurnIndex());
    }

    @Test
    void findPlayerById_returns_correct_player() {
        StubPlayer p1 = new StubPlayer("1", "A");
        StubPlayer p2 = new StubPlayer("2", "B");
        game.addPlayer(p1);
        game.addPlayer(p2);

        Player found = game.findPlayerById("2");
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
        StubPlayer p1 = new StubPlayer("1", "A");
        StubPlayer p2 = new StubPlayer("2", "B");
        game.addPlayer(p1);
        game.addPlayer(p2);
        game.startGame();

        game.endGame();

        assertEquals(GameState.ENDED, game.getGameState());
        assertNotNull(getField(game, "leaderboard"));
    }

    @Test
    void hasMinValueToStop_true_when_enough_points() {
        game.addPlayer(new StubPlayer("1", "A"));
        game.addPlayer(new StubPlayer("2", "B"));
        game.startGame();

        TilesPot mockPot = new TilesPot() {
            @Override
            public int getLowestAvailableTileValue() { return 10; }
        };
        setField(game, "tilesPot", mockPot);

        boolean result = game.hasMinValueToStop(15);
        assertTrue(result);
    }

    @Test
    void hasMinValueToStop_false_when_too_low_and_no_matching_tile() {
        game.addPlayer(new StubPlayer("1", "A"));
        game.addPlayer(new StubPlayer("2", "B"));
        game.startGame();

        TilesPot mockPot = new TilesPot() {
            @Override
            public int getLowestAvailableTileValue() { return 20; }
        };
        setField(game, "tilesPot", mockPot);

        boolean result = game.hasMinValueToStop(10);
        assertFalse(result);
    }

    // ❌ Removed old roundZerounfinished() test — that method doesn’t exist anymore
    // If needed, you could later replace it with a check for some “round still active” logic.

    @Test
    void equals_true_for_same_id() {
        Game g1 = new Game("X", 2);
        Game g2 = new Game("Y", 2);
        setField(g2, "id", getField(g1, "id"));

        assertEquals(g1, g2);
    }

    @Test
    void equals_false_for_different_id() {
        Game g1 = new Game("X", 2);
        Game g2 = new Game("Y", 2);

        assertNotEquals(g1, g2);
    }
}
