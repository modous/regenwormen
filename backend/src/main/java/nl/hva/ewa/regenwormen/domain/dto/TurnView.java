package nl.hva.ewa.regenwormen.domain.dto;

import nl.hva.ewa.regenwormen.domain.Dice;
import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import nl.hva.ewa.regenwormen.domain.Player;

import java.util.List;
import java.util.Map;

public record TurnView(
        String playerId,                    //SpelerID
        int takenScore,                     //tussentijdse Dice value
        List<Dice> chosenDices,             //List gekozen Dices
        List<DiceFace> options,             // kiesbare gezichten (leeg = bust of einde) deze throw
        Map<DiceFace, Long> optionCounts,
        boolean hasSpecial,                 // SPECIAL al gekozen?
        boolean hasMinValue,                    // mag je stoppen? (regels bepalen dit)
        boolean busted                      // is de speler zojuist gebust?
) {
    public static TurnView turnViewThrown(Player p, List<DiceFace> options ,boolean hasMinValue) {
        return new TurnView(p.getId(),
                p.getDiceRoll().getTakenScore(),
                p.getDiceRoll().getChosenDices(),
                options,
                p.getDiceRoll().getOptionCounts(),
                p.getDiceRoll().hasSpecial(),
                hasMinValue,
                false);
    }
    public static TurnView turnViewChosen(Player p, boolean hasMinValue) {
        return new TurnView(p.getId(),
                p.getDiceRoll().getTakenScore(),
                p.getDiceRoll().getChosenDices(),
                List.of(),
                Map.of(),
                p.getDiceRoll().hasSpecial(),
                hasMinValue,
                false);
    }
    public static TurnView bust(Player p) {
        return new TurnView(p.getId(), 0, List.of(), List.of(), Map.of(), false, false, true);
    }
}