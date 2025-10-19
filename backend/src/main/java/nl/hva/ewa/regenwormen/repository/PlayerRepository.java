package nl.hva.ewa.regenwormen.repository;

import nl.hva.ewa.regenwormen.domain.Player;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository {
    Player save(Player player);
    Optional<Player> findById(String id);
    boolean deleteByPlayerId(String id);
    List<Player> findAll();
}
