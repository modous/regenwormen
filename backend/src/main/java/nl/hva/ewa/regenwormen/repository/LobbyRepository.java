package nl.hva.ewa.regenwormen.repository;

import nl.hva.ewa.regenwormen.domain.Lobby;
import java.util.List;

public interface LobbyRepository {
    List<Lobby> findAll();
    Lobby findById(int id);
    Lobby save(Lobby lobby);
}
