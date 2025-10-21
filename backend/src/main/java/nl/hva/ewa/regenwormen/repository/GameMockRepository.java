package nl.hva.ewa.regenwormen.repository;

import nl.hva.ewa.regenwormen.domain.Enum.GameState;
import nl.hva.ewa.regenwormen.domain.Game;
import nl.hva.ewa.regenwormen.domain.Player;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class GameMockRepository implements GameRepository{

    private final List<Game> games = new ArrayList<>();

    public GameMockRepository() {
        games.add(new Game("Room_1", 2));
        games.add(new Game("Room_2", 4));
    }

    @Override
    public List<Game> findAll() {
        return Collections.unmodifiableList(games);
    }

    @Override
    public List<Game> findAllPreGames(){
        List<Game> preGames= games.stream().filter(g -> g.getGameState() == GameState.PRE_GAME).toList();
        return Collections.unmodifiableList(preGames);
    }

    @Override
    public Optional<Game> findById(String id) {
        return games.stream()
                .filter(p -> Objects.equals(p.getId(), id))
                .findFirst();
    }

    @Override
    public Game save(Game game) {
        findById(game.getId()).ifPresent(games::remove);
        games.add(game);
        return game;
    }

    @Override
    public Game deleteById(String id) {
        Optional<Game> found = findById(id);
        found.ifPresent(games::remove);
        return found.orElse(null);
    }
}
