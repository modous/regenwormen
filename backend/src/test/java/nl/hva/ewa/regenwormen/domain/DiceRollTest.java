package nl.hva.ewa.regenwormen.domain;

import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import nl.hva.ewa.regenwormen.domain.Enum.TurnState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Updated test for Diceroll (compatible with latest backend).
 * - Replaced getChosenDices() → getChosenFaces()
 * - Removed references to private/removed methods like hasUntakenDice(), canRollPreCheck()
 */
class DicerollTest {

    private Diceroll dr;

    @BeforeEach
    void setUp() {
        dr = new Diceroll();
    }

    // ---------- Helpers ----------

    private DiceFace anyNonSpecial() {
        for (DiceFace f : DiceFace.values()) {
            if (f != DiceFace.SPECIAL) return f;
        }
        return DiceFace.SPECIAL;
    }

    static class FixedDice extends Dice {
        private DiceFace face;
        private int points;
        private boolean taken;

        FixedDice(DiceFace face, int points, boolean taken) {
            super();
            this.face = face;
            this.points = points;
            this.taken = taken;
        }

        @Override
        public void roll() { /* fixed dice */ }

        @Override
        public DiceFace getDiceState() { return face; }

        @Override
        public int getPoints() { return points; }

        @Override
        public boolean isTaken() { return taken; }

        @Override
        public void take() { this.taken = true; }
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object target, String fieldName) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (T) f.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------- Tests ----------

    @Test
    void initial_state_is_correct() {
        assertEquals(TurnState.CAN_ROLL, dr.getTurnState());
        assertFalse(dr.hasSpecial());
        assertFalse(dr.getBusted());
        assertEquals(0, dr.getTakenScore());
        assertTrue(dr.getChosenFaces().isEmpty(), "Chosen faces should start empty");
    }

    @Test
    void pickDice_requires_last_roll_and_must_pick() {
        // Default state is CAN_ROLL -> pickDice should throw
        assertThrows(IllegalArgumentException.class,
                () -> dr.pickDice(DiceFace.SPECIAL),
                "Must roll before picking a dice face");
    }

    @Test
    void rollRemainingDice_sets_options_and_goes_to_MUST_PICK() {
        DiceFace other = anyNonSpecial();

        List<Dice> pool = new ArrayList<>();
        pool.add(new FixedDice(other, 2, false));
        pool.add(new FixedDice(other, 2, false));
        pool.add(new FixedDice(DiceFace.SPECIAL, 5, false));
        for (int i = 0; i < 5; i++) pool.add(new FixedDice(other, 2, true));

        setField(dr, "dices", pool);

        List<DiceFace> options = dr.rollRemainingDice();

        assertEquals(TurnState.MUST_PICK, dr.getTurnState());
        assertTrue(options.contains(other));
        assertTrue(options.contains(DiceFace.SPECIAL));
        assertEquals(2, options.size());
    }

    @Test
    void pickDice_end_without_special_results_in_bust() {
        DiceFace other = anyNonSpecial();
        FixedDice d = new FixedDice(other, 2, false);

        List<Dice> pool = new ArrayList<>();
        pool.add(d);
        for (int i = 0; i < 7; i++) pool.add(new FixedDice(other, 2, true));

        setField(dr, "dices", pool);
        setField(dr, "lastRoll", new ArrayList<>(List.of(d)));
        setField(dr, "turnState", TurnState.MUST_PICK);

        dr.pickDice(other);

        assertEquals(TurnState.ENDED, dr.getTurnState());
        assertTrue(dr.getBusted());
        assertFalse(dr.hasSpecial());
    }

    @Test
    void pickDice_throws_if_face_already_chosen() {
        DiceFace other = anyNonSpecial();

        FixedDice d = new FixedDice(other, 2, false);
        setField(dr, "lastRoll", new ArrayList<>(List.of(d)));
        setField(dr, "turnState", TurnState.MUST_PICK);

        Set<DiceFace> chosen = EnumSet.noneOf(DiceFace.class);
        chosen.add(other);
        setField(dr, "chosenFaces", chosen);

        assertThrows(IllegalStateException.class, () -> dr.pickDice(other),
                "You already picked " + other);
    }

    @Test
    void pickDice_throws_if_face_not_in_last_roll() {
        DiceFace wanted = anyNonSpecial();
        DiceFace present = DiceFace.SPECIAL;

        FixedDice s = new FixedDice(present, 5, false);

        setField(dr, "lastRoll", new ArrayList<>(List.of(s)));
        setField(dr, "turnState", TurnState.MUST_PICK);

        assertThrows(IllegalStateException.class, () -> dr.pickDice(wanted),
                "Chosen face " + wanted + " was not present in the last roll.");
    }

    @Test
    void rollRemainingDice_busts_when_no_options() {
        DiceFace other = anyNonSpecial();

        FixedDice a = new FixedDice(other, 2, false);
        FixedDice b = new FixedDice(other, 2, false);

        List<Dice> pool = new ArrayList<>();
        pool.add(a);
        pool.add(b);
        for (int i = 0; i < 6; i++) pool.add(new FixedDice(other, 2, true));
        setField(dr, "dices", pool);

        Set<DiceFace> chosen = EnumSet.of(other);
        setField(dr, "chosenFaces", chosen);

        List<DiceFace> options = dr.rollRemainingDice();

        assertTrue(options.isEmpty());
        assertEquals(TurnState.ENDED, dr.getTurnState());
        assertTrue(dr.getBusted());
    }

    @Test
    void requireAlive_blocks_after_ended() {
        setField(dr, "turnState", TurnState.ENDED);
        assertThrows(IllegalStateException.class, () -> dr.rollRemainingDice());
        assertThrows(IllegalStateException.class, () -> dr.pickDice(DiceFace.SPECIAL));
    }

    @Test
    void getFullThrow_groups_all_faces_from_lastRoll() {
        DiceFace other = anyNonSpecial();
        FixedDice a = new FixedDice(other, 2, false);
        FixedDice b = new FixedDice(other, 2, false);
        FixedDice s = new FixedDice(DiceFace.SPECIAL, 5, false);

        setField(dr, "lastRoll", new ArrayList<>(Arrays.asList(a, b, s)));

        Map<DiceFace, Long> map = dr.getFullThrow();
        assertEquals(2L, map.get(other));
        assertEquals(1L, map.get(DiceFace.SPECIAL));
        assertEquals(2, map.size());
    }

    @Test
    void getTakenScore_sums_only_taken_dice_points() {
        DiceFace other = anyNonSpecial();
        FixedDice taken1 = new FixedDice(other, 2, true);
        FixedDice taken2 = new FixedDice(DiceFace.SPECIAL, 5, true);
        FixedDice notTaken = new FixedDice(other, 2, false);

        List<Dice> pool = Arrays.asList(taken1, taken2, notTaken);
        setField(dr, "dices", new ArrayList<>(pool));

        assertEquals(7, dr.getTakenScore());
    }
}
