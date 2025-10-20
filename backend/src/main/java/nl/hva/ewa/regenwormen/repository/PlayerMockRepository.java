package nl.hva.ewa.regenwormen.repository;

import nl.hva.ewa.regenwormen.domain.Player;
import nl.hva.ewa.regenwormen.domain.User;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PlayerMockRepository implements PlayerRepository {

    private final List<Player> players = new ArrayList<>();

    public PlayerMockRepository(){

        players.add(new Player("Player1"));
        players.add(new Player("Player2"));
        players.add(new Player("Player3"));
        players.add(new Player("Player4"));
    }

    @Override
    public Player save(Player player) {
        // Verwijder oude versie met dezelfde id (upsert)
        players.removeIf(p -> Objects.equals(p.getId(), player.getId()));
        players.add(player);
        return player;
    }

    @Override
    public Optional<Player> findById(String id) {
        return players.stream()
                .filter(p -> Objects.equals(p.getId(), id))
                .findFirst();
    }

    public boolean deleteByPlayerId(String id){
        return players.removeIf(p -> Objects.equals(p.getId(), id));
    }

    @Override
    public List<Player> findAll() {
        return List.copyOf(players);
    }
}
