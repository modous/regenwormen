package nl.hva.ewa.regenwormen.domain;

import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import nl.hva.ewa.regenwormen.domain.Enum.TurnState;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Diceroll {
    private static final int AMOUNT_DICES = 8;

    private TurnState turnState = TurnState.CAN_ROLL;
    private boolean hasSpecial = false;
    private boolean busted = false;

    /** All 8 dice for the turn */
    private final List<Dice> dices = new ArrayList<>(AMOUNT_DICES);
    /** The dice from the most recent roll (subset of dices that are not taken) */
    private List<Dice> lastRoll = new ArrayList<>();
    /** Faces already chosen (locked) in this turn */
    private final Set<DiceFace> chosen = EnumSet.noneOf(DiceFace.class);

    public Diceroll() {
        for (int i = 0; i < AMOUNT_DICES; i++) {
            dices.add(new Dice());
        }
    }

    // --- GETTERS / STATE ---
    public TurnState getTurnState() { return turnState; }
    public boolean hasSpecial() { return hasSpecial; }
    public boolean getBusted() { return busted; }

    /** Faces already chosen in this turn */
    public Set<DiceFace> getChosenFaces() {
        return EnumSet.copyOf(chosen);
    }

    /** Count of the last roll (what actually appeared) */
    public Map<DiceFace, Long> getFullThrow() {
        return lastRoll.stream()
                .map(Dice::getDiceState)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    /** Returns only pickable (not already chosen) dice counts */
    public Map<DiceFace, Long> getOptionCounts() {
        return lastRoll.stream()
                .map(Dice::getDiceState)
                .filter(face -> !chosen.contains(face))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    /** Pickable faces THIS roll (rolled AND not chosen yet) */
    public List<DiceFace> getPickableFaces() {
        return lastRoll.stream()
                .map(Dice::getDiceState)
                .filter(face -> !chosen.contains(face))
                .distinct()
                .toList();
    }

    /** Faces that were rolled but disabled (already chosen earlier in turn) */
    public List<DiceFace> getDisabledFaces() {
        return lastRoll.stream()
                .map(Dice::getDiceState)
                .filter(chosen::contains)
                .distinct()
                .toList();
    }

    public int getTakenScore() {
        return dices.stream()
                .filter(Dice::isTaken)
                .mapToInt(Dice::getPoints)
                .sum();
    }

    public int getRemainingDiceCount() {
        return (int) dices.stream().filter(d -> !d.isTaken()).count();
    }

    // --- INTERNAL HELPERS ---
    private void requireAlive() {
        if (turnState == TurnState.ENDED) {
            throw new IllegalStateException("Turn already ended.");
        }
    }

    private boolean hasUntakenDice() {
        return dices.stream().anyMatch(d -> !d.isTaken());
    }

    private boolean hasUnchosenFacesRemainingOverall() {
        // If you already chose all possible faces, you can’t continue
        return chosen.size() < DiceFace.values().length;
    }

    private boolean canRollPreCheck() {
        return hasUntakenDice() && hasUnchosenFacesRemainingOverall();
    }

    // --- MAIN API USED BY GAME ---
    /** Roll all remaining dice (those not taken yet). Returns pickable faces for this roll. */
    public List<DiceFace> rollRemainingDice() {
        requireAlive();

        // ✅ Must pick before re-rolling again
        if (turnState != TurnState.CAN_ROLL) {
            throw new IllegalArgumentException("Pick a dice face before rolling again.");
        }

        if (!canRollPreCheck()) {
            busted = true;
            turnState = TurnState.ENDED;
            lastRoll = List.of();
            return List.of();
        }

        // 🎲 Roll all untaken dice
        List<Dice> current = new ArrayList<>();
        for (Dice d : dices) {
            if (!d.isTaken()) {
                d.roll();
                current.add(d);
            }
        }
        lastRoll = current;

        // 🧩 Determine pickable faces
        List<DiceFace> options = getPickableFaces();
        if (options.isEmpty()) {
            busted = true;
            turnState = TurnState.ENDED;
            return List.of();
        }

        turnState = TurnState.MUST_PICK;
        return options;
    }

    /**
     * Pick a face from the last roll (locks ALL dice showing that face).
     * Returns the updated total taken score.
     */
    public int pickDice(DiceFace face) {
        requireAlive();

        if (turnState != TurnState.MUST_PICK) {
            throw new IllegalArgumentException("Roll before picking a dice face.");
        }
        if (chosen.contains(face)) {
            throw new IllegalStateException("Already picked " + face + " earlier this turn.");
        }

        int kept = 0;
        for (Dice d : lastRoll) {
            if (d.getDiceState() == face) {
                d.take();
                kept++;
            }
        }
        if (kept == 0) {
            throw new IllegalStateException("Chosen face " + face + " was not rolled.");
        }

        if (face == DiceFace.SPECIAL) {
            hasSpecial = true;
        }

        chosen.add(face);
        lastRoll = new ArrayList<>(); // clear after choosing

        // ✅ You can roll again if there are dice left and faces left to choose
        if (canRollPreCheck()) {
            turnState = TurnState.CAN_ROLL;
        } else {
            turnState = TurnState.ENDED;
            if (!hasSpecial) {
                busted = true; // end without special = bust
            }
        }

        return getTakenScore();
    }
}
