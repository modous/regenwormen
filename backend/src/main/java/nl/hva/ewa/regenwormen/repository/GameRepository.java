package nl.hva.ewa.regenwormen.repository;

import nl.hva.ewa.regenwormen.domain.Game;

import java.util.List;

public interface GameRepository {

    List<Game> findAll();
    List<Game> findAllPreGames();
    Game findById(String id);
    Game save(Game game);
    Game deleteById(String id);
}
