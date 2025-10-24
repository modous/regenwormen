package nl.hva.ewa.regenwormen.domain;

import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import nl.hva.ewa.regenwormen.domain.Enum.TurnState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests voor Diceroll met deterministische "dobbelstenen".
 * We vervangen de private 'dices' en/of 'lastRoll' via reflectie
 * door FixedDice die voorspelbare faces/points opleveren.
 */
class DicerollTest {

    private Diceroll dr;

    @BeforeEach
    void setUp() {
        dr = new Diceroll();
    }

    // ---------- helpers ----------

    /** Zoek een niet-SPECIAL face (maakt de test robust tegen enum-namen). */
    private DiceFace anyNonSpecial() {
        for (DiceFace f : DiceFace.values()) {
            if (f != DiceFace.SPECIAL) return f;
        }
        // Mocht SPECIAL de enige zijn (zou vreemd zijn), val dan terug:
        return DiceFace.SPECIAL;
    }

    /** Test-dice met vaste face/points en overridable roll/take gedrag. */
    static class FixedDice extends Dice {
        private DiceFace face;
        private int points;
        private boolean taken;

        FixedDice(DiceFace face, int points, boolean taken) {
            super(); // aanroepen van de super ctor (leeg)
            this.face = face;
            this.points = points;
            this.taken = taken;
        }

        @Override
        public void roll() {
            // doe niets: de face blijft zoals ingesteld
        }

        @Override
        public DiceFace getDiceState() {
            return face;
        }

        @Override
        public int getPoints() {
            return points;
        }

        @Override
        public boolean isTaken() {
            return taken;
        }

        @Override
        public void take() {
            this.taken = true;
        }

        // optional helper om face te wijzigen wanneer nodig (niet gebruikt)
        public void setFace(DiceFace newFace, int newPoints) {
            this.face = newFace;
            this.points = newPoints;
        }
    }

    /** Reflectie utility om private velden te zetten. */
    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Lees private veld (handig voor asserties). */
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

    // ---------- tests ----------

    @Test
    void initial_state_is_correct() {
        assertEquals(TurnState.CAN_ROLL, dr.getTurnState());
        assertFalse(dr.hasSpecial());
        assertFalse(dr.getBusted());
        assertEquals(0, dr.getTakenScore());
        assertTrue(dr.getChosenDices().isEmpty());
        assertTrue(dr.hasUnchosenFacesRemaining());
        assertTrue(dr.hasUntakenDice());
        assertTrue(dr.canRollPreCheck());
    }

    @Test
    void pickDice_requires_last_roll_and_must_pick() {
        // Default state is CAN_ROLL -> pickDice mag niet
        assertThrows(IllegalArgumentException.class, () ->
                        dr.pickDice(DiceFace.SPECIAL),
                "First throw dice before picking a dice face");
    }

    @Test
    void rollRemainingDice_sets_options_and_goes_to_MUST_PICK() {
        // Arrange: maak 3 dobbelstenen die "rollen" naar: NON-SPECIAL, NON-SPECIAL, SPECIAL.
        DiceFace other = anyNonSpecial();

        List<Dice> pool = new ArrayList<>();
        pool.add(new FixedDice(other, 2, false));
        pool.add(new FixedDice(other, 2, false));
        pool.add(new FixedDice(DiceFace.SPECIAL, 5, false));
        // rest "genomen" zodat ze niet mee rollen
        for (int i = 0; i < 5; i++) pool.add(new FixedDice(other, 2, true));

        setField(dr, "dices", pool);

        // Act
        List<DiceFace> options = dr.rollRemainingDice();

        // Assert
        assertEquals(TurnState.MUST_PICK, dr.getTurnState());
        // opties zijn unieke faces van lastRoll (die nog niet gekozen zijn)
        // = {other, SPECIAL}
        assertTrue(options.contains(other));
        assertTrue(options.contains(DiceFace.SPECIAL));
        assertEquals(2, options.size());
    }


    @Test
    void pickDice_end_without_special_results_in_bust() {
        // Arrange: lastRoll bevat één NON-SPECIAL en daarna zijn er geen opties meer
        DiceFace other = anyNonSpecial();
        FixedDice d = new FixedDice(other, 2, false);

        // rest already taken
        List<Dice> pool = new ArrayList<>();
        pool.add(d);
        for (int i = 0; i < 7; i++) pool.add(new FixedDice(other, 2, true));

        setField(dr, "dices", pool);
        setField(dr, "lastRoll", new ArrayList<>(List.of(d)));
        setField(dr, "turnState", TurnState.MUST_PICK);

        // Act
        dr.pickDice(other);

        // Assert: omdat er geen untaken dice meer zijn OF alle faces gekozen zijn,
        // gaat de beurt naar ENDED, en zonder SPECIAL => busted = true
        assertEquals(TurnState.ENDED, dr.getTurnState());
        assertTrue(dr.getBusted());
        assertFalse(dr.hasSpecial());
    }

    @Test
    void pickDice_throws_if_face_already_chosen() {
        DiceFace other = anyNonSpecial();

        // lastRoll met 1 dobbelsteen op 'other'
        FixedDice d = new FixedDice(other, 2, false);
        setField(dr, "lastRoll", new ArrayList<>(List.of(d)));
        setField(dr, "turnState", TurnState.MUST_PICK);

        // zet 'chosen' al op 'other'
        @SuppressWarnings("unchecked")
        Set<DiceFace> chosen = EnumSet.noneOf(DiceFace.class);
        chosen.add(other);
        setField(dr, "chosen", chosen);

        assertThrows(IllegalStateException.class, () -> dr.pickDice(other),
                "You already picked " + other + " before!");
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
        // Arrange: we zorgen dat rollRemainingDice geen pickable faces oplevert.
        // Dit kan door alle lastRoll-faces al 'chosen' te hebben.
        DiceFace other = anyNonSpecial();

        FixedDice a = new FixedDice(other, 2, false);
        FixedDice b = new FixedDice(other, 2, false);

        // alleen 2 dice nog niet genomen
        List<Dice> pool = new ArrayList<>();
        pool.add(a);
        pool.add(b);
        for (int i = 0; i < 6; i++) pool.add(new FixedDice(other, 2, true));
        setField(dr, "dices", pool);

        // Markeer 'other' al gekozen zodat options leeg wordt
        Set<DiceFace> chosen = EnumSet.of(other);
        setField(dr, "chosen", chosen);

        // Act
        List<DiceFace> options = dr.rollRemainingDice();

        // Assert: geen opties => ENDED + busted
        assertTrue(options.isEmpty());
        assertEquals(TurnState.ENDED, dr.getTurnState());
        assertTrue(dr.getBusted());
    }

    @Test
    void requireAlive_blocks_after_ended() {
        // Forceer einde
        setField(dr, "turnState", TurnState.ENDED);
        assertThrows(IllegalStateException.class, () -> dr.rollRemainingDice());
        assertThrows(IllegalStateException.class, () -> dr.pickDice(DiceFace.SPECIAL));
    }

    @Test
    void getPickableFaces_and_optionCounts_respect_chosen() {
        DiceFace other = anyNonSpecial();
        FixedDice a = new FixedDice(other, 2, false);
        FixedDice s = new FixedDice(DiceFace.SPECIAL, 5, false);

        setField(dr, "lastRoll", new ArrayList<>(Arrays.asList(a, s)));
        // kies 'other' al
        Set<DiceFace> chosen = EnumSet.of(other);
        setField(dr, "chosen", chosen);

        List<DiceFace> pickable = dr.getPickableFaces();
        assertEquals(1, pickable.size());
        assertEquals(DiceFace.SPECIAL, pickable.get(0));

        Map<DiceFace, Long> optionCounts = dr.getOptionCounts();
        assertTrue(optionCounts.containsKey(DiceFace.SPECIAL));
        assertFalse(optionCounts.containsKey(other));
        assertEquals(1L, optionCounts.get(DiceFace.SPECIAL));
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
