package nl.hva.ewa.regenwormen.domain.dto;

import nl.hva.ewa.regenwormen.domain.Player;

public record EndTurnView(
        String playerId,                    //SpelerID
        int takenScore,                     //End Diceroll value
        boolean busted,                     // is de speler zojuist gebust?
        ClaimOptions claimOptions
) {
    public static EndTurnView roundZero(Player p) {
        return new EndTurnView(p.getId(), p.getDoublePointsTile(), p.getDiceRoll().getBusted(), null);
    }
    public static EndTurnView round(Player p, ClaimOptions options){
        return new EndTurnView(p.getId(), p.getDiceRoll().getTakenScore(), p.getDiceRoll().getBusted(), options);
    }
}
