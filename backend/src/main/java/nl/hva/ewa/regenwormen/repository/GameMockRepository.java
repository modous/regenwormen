package nl.hva.ewa.regenwormen.repository;

import nl.hva.ewa.regenwormen.domain.Enum.GameState;
import nl.hva.ewa.regenwormen.domain.Game;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.*;

@Primary
@Repository
public class GameMockRepository implements GameRepository {

    private final Map<String, Game> games = new HashMap<>();

    public GameMockRepository() {
        Game g1 = new Game("Room_1", 2);
        Game g2 = new Game("Room_2", 4);
        games.put(g1.getId(), g1);
        games.put(g2.getId(), g2);
    }

    @Override
    public List<Game> findAll() {
        return new ArrayList<>(games.values());
    }

    @Override
    public List<Game> findAllPreGames() {
        return games.values().stream()
                .filter(g -> g.getGameState() == GameState.PRE_GAME)
                .toList();
    }

    @Override
    public Optional<Game> findById(String id) {
        return Optional.ofNullable(games.get(id));
    }

    @Override
    public Game save(Game game) {
        games.put(game.getId(), game);
        return game;
    }

    @Override
    public Game deleteById(String id) {
        return games.remove(id);
    }
}
