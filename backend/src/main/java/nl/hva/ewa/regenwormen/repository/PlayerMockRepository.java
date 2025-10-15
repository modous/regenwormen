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
        User User1 = new User();
        User User2 = new User();

        players.add(new Player("Player1", User1));
        players.add(new Player("Player2", User2));
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

    @Override
    public List<Player> findAll() {
        return List.copyOf(players);
    }

    @Override
    public List<Player> findByGameId(String gameId) {
        return players.stream()
                .filter(p -> Objects.equals(p.getGame().getId(), gameId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByGameIdAndUserId(String gameId, String userId) {
        return players.stream().anyMatch(p ->
                Objects.equals(p.getGame().getId(), gameId)
                        && Objects.equals(p.getUser().getId(), userId)
        );
    }

    @Override
    public boolean deleteByGameIdAndUserId(String gameId, String userId) {
        return players.removeIf(p ->
                Objects.equals(p.getGame().getId(), gameId)
                        && Objects.equals(p.getUser().getId(), userId)
        );
    }

    @Override
    public long countByGameId(String gameId) {
        return players.stream()
                .filter(p -> Objects.equals(p.getGame().getId(), gameId))
                .count();
    }
}
