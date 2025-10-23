package nl.hva.ewa.regenwormen.service;

import nl.hva.ewa.regenwormen.domain.Game;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PreGameServiceTest {

    @Autowired
    private PreGameService service;

    @Test
    void createGame_shouldReturnGameWithCorrectName() {
        Game game = service.createGame("TestRoom", 4);
        assertNotNull(game);
        assertEquals("TestRoom", game.getGameName());
        assertEquals(4, game.getMaxPlayers());
    }

    @Test
    void deleteGame_shouldThrowExceptionWhenGameNotFound() {
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            service.deleteGameByID("nonexistent-id");
        });
    }

}
