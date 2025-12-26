package nl.hva.ewa.regenwormen.api;

import jakarta.annotation.PostConstruct;
import nl.hva.ewa.regenwormen.entity.GameResult;
import nl.hva.ewa.regenwormen.repository.GameResultRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/history")
@CrossOrigin(origins = "http://localhost:5173")
public class MatchHistoryController {

    private final GameResultRepository repo;

    public MatchHistoryController(GameResultRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/{username}")
    public List<GameResult> getHistory(@PathVariable String username) {
        return repo.findByUsernameOrderByFinishedAtDesc(username);
    }
    @PostConstruct
    public void init() {
        System.out.println(">>> MatchHistoryController LOADED");
    }
}
