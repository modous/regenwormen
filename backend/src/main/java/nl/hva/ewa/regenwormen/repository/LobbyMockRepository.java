package nl.hva.ewa.regenwormen.repository;

import nl.hva.ewa.regenwormen.domain.Lobby;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class LobbyMockRepository implements LobbyRepository {

    private final List<Lobby> lobbies = new ArrayList<>();

    public LobbyMockRepository() {
        for (int i = 1; i <= 4; i++) {
            lobbies.add(new Lobby(i, "Lobby " + i));
        }
    }

    @Override
    public List<Lobby> findAll() {
        return lobbies;
    }

    @Override
    public Lobby findById(int id) {
        return lobbies.stream().filter(l -> l.getId() == id).findFirst().orElse(null);
    }

    @Override
    public Lobby save(Lobby lobby) {
        return lobby; // not persistent — just a mock
    }
}
