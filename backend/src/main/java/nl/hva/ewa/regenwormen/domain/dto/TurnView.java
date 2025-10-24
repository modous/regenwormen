package nl.hva.ewa.regenwormen.domain.dto;

import nl.hva.ewa.regenwormen.domain.Diceroll;
import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import nl.hva.ewa.regenwormen.domain.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record TurnView(
        String playerId,
        int takenScore,                     // total score from taken dice
        Map<DiceFace, Long> fullThrow,      // all dice rolled this throw (8 dice total)
        Map<DiceFace, Long> optionCounts,   // counts of pickable faces this throw
        Set<DiceFace> chosenFaces,          // faces locked already in this turn
        List<DiceFace> disabledFaces,       // rolled but unavailable (already chosen)
        boolean hasSpecial,
        boolean hasMinValue,
        boolean busted
) {

    /** Create TurnView for a dice throw */
    public static TurnView turnViewThrown(Player p, List<DiceFace> options, boolean hasMinValue) {
        Diceroll roll = p.getDiceRoll();
        return new TurnView(
                p.getId(),
                roll.getTakenScore(),
                roll.getFullThrow(),
                roll.getOptionCounts(),
                roll.getChosenFaces(),
                roll.getDisabledFaces(),
                roll.hasSpecial(),
                hasMinValue,
                roll.getBusted()
        );
    }

    /** Create TurnView after picking a dice face */
    public static TurnView turnViewChosen(Player p, boolean hasMinValue) {
        Diceroll roll = p.getDiceRoll();
        return new TurnView(
                p.getId(),
                roll.getTakenScore(),
                roll.getFullThrow(),
                roll.getOptionCounts(),
                roll.getChosenFaces(),
                roll.getDisabledFaces(),
                roll.hasSpecial(),
                hasMinValue,
                roll.getBusted()
        );
    }

    /** Create TurnView when player busts */
    public static TurnView bust(Player p) {
        return new TurnView(
                p.getId(),
                0,
                Map.of(),
                Map.of(),
                Set.of(),
                List.of(),
                false,
                false,
                true
        );
    }
}
