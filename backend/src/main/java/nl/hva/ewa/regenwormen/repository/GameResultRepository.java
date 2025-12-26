package nl.hva.ewa.regenwormen.repository;

import nl.hva.ewa.regenwormen.entity.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameResultRepository extends JpaRepository<GameResult, String> {

    List<GameResult> findByUsernameOrderByFinishedAtDesc(String username);
    boolean existsByGameId(String gameId);
}
