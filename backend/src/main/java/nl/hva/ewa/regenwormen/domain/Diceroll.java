package nl.hva.ewa.regenwormen.domain;

import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import nl.hva.ewa.regenwormen.domain.Enum.TurnState;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Diceroll {
    private static final int AMOUNT_DICES = 8;

//    private final Player player;
    private TurnState turnState = TurnState.CAN_ROLL;
    private boolean hasSpecial = false;
    private boolean busted = false;

    private List<Dice> dices = new ArrayList<Dice>(AMOUNT_DICES);
    private List<Dice> lastRoll= new ArrayList<>();
    private Set<DiceFace> chosen = EnumSet.noneOf(DiceFace.class);

    public Diceroll(){
        for (int i = 0; i < AMOUNT_DICES; i++){
            dices.add(new Dice());
        }
    }

    //getters
    public TurnState getTurnState() { return turnState; }
    public boolean hasSpecial() { return hasSpecial; }
    public boolean getBusted(){return busted;}

    // ----------------- helpers -----------------

    private void requireAlive() {
        if (turnState == TurnState.ENDED){throw new IllegalStateException("Turn already ended.");};
    }

    public List<Dice> getChosenDices(){
        return dices.stream()
                .filter(Dice::isTaken)
                .toList();
    }

    public List<DiceFace> getPickableFaces() {
        return lastRoll.stream()
                .map(Dice::getDiceState)
                .filter(face -> !chosen.contains(face))
                .distinct()
                .toList();
    }

    public Map<DiceFace, Long> getFullThrow() {
        return lastRoll.stream()
                .map(Dice::getDiceState)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public Map<DiceFace, Long> getOptionCounts() {
        return lastRoll.stream()
                .map(Dice::getDiceState)
                .filter(face -> !chosen.contains(face))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public int getTakenScore() {
        return dices.stream()
                .filter(Dice::isTaken)
                .mapToInt(Dice::getPoints)
                .sum();
    }
    public boolean hasUntakenDice() {
        return dices.stream().anyMatch(d -> !d.isTaken());
    }
    public boolean hasUnchosenFacesRemaining() {
        return chosen.size() < DiceFace.values().length;
    }
    public boolean canRollPreCheck() {
        return hasUntakenDice() && hasUnchosenFacesRemaining();
    }

    //functions
    public int pickDice(DiceFace face) {
        requireAlive();
        if(turnState != TurnState.MUST_PICK){throw new IllegalArgumentException("First throw dice before picking a dice face");}
        if (chosen.contains(face)) {throw new IllegalStateException("You already picked " + face + " before!");}

        int kept = 0;
        for(Dice d : lastRoll){
            if(d.getDiceState() == face){
                d.take();
                kept++;
            }
        }
        if(kept==0){ throw new IllegalStateException("Chosen face " + face + " was not present in the last roll.");}

        if(face == DiceFace.SPECIAL){hasSpecial = true;}
        chosen.add(face);
        lastRoll = new ArrayList<>();

        turnState = canRollPreCheck() ? TurnState.CAN_ROLL : TurnState.ENDED;

        if (turnState == TurnState.ENDED && !hasSpecial) {
            busted = true;
        }

        return getTakenScore();
    }

    public List<DiceFace> rollRemainingDice(){
        requireAlive();
        if(turnState != TurnState.CAN_ROLL){throw new IllegalArgumentException("First pick a dice face before throwing dice");}
        if(!canRollPreCheck()){
            turnState = TurnState.ENDED;
            busted = true;
            return List.of();
        }

        lastRoll = new ArrayList<>();
        for (Dice d : dices) {
            if(!d.isTaken()){
                d.roll();
                lastRoll.add(d);

            }
        }
        List<DiceFace> options = getPickableFaces();
        if (options.isEmpty()){
            turnState = TurnState.ENDED;
            busted = true;
            return List.of();
        }

        turnState = TurnState.MUST_PICK;
        return options;
    }

}
