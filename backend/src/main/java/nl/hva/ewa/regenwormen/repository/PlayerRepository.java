package nl.hva.ewa.regenwormen.repository;

import nl.hva.ewa.regenwormen.domain.Player;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository {
    Player save(Player player);
    Optional<Player> findById(String id);
    List<Player> findAll();
    List<Player> findByGameId(String gameId);

    boolean existsByGameIdAndUserId(String gameId, String userId);
    boolean deleteByGameIdAndUserId(String gameId, String userId);
    long countByGameId(String gameId);
}
