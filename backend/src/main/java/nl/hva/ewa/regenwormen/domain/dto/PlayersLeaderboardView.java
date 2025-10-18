package nl.hva.ewa.regenwormen.domain.dto;

public record PlayersLeaderboardView(
    String playerId,
    String name,
    int points,
    int rank
) {}

