package nl.hva.ewa.regenwormen.repository;

import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Player;

import java.util.List;
import java.util.Optional;

public interface GameRepository {

    List<Game> findAll();
    List<Game> findAllPreGames();
    Optional<Game> findById(String id);
    Game save(Game game);
    Game deleteById(String id);
}
