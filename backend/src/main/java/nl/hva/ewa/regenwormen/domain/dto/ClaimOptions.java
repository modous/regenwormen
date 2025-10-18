package nl.hva.ewa.regenwormen.domain.dto;

import nl.hva.ewa.regenwormen.domain.Player;

import java.util.List;

public record ClaimOptions(
        String playerId,
        List<Integer> claimablePotValues,      // bv. beschikbare pot-tiles (values)
        List<StealOptions> stealableTopTiles    // top tiles van tegenstanders die je mag stelen
) {}

