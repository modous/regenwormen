package nl.hva.ewa.regenwormen.repository;

import nl.hva.ewa.regenwormen.domain.Enum.GameState;
import nl.hva.ewa.regenwormen.domain.Game;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public Game findById(String id) {
        for(Game game: games) {
            if (game.getId().equals(id)) {return game;}
        }
        return null;
    }

    @Override
    public Game save(Game game) {
        Game found = findById(game.getId());
        if(found != null){games.remove(found);}
        games.add(game);
        return game;
    }

    @Override
    public Game deleteById(String id) {
        Game found = findById(id);
        if(found == null) {return null;}
        games.remove(found);
        return found;
    }
}
