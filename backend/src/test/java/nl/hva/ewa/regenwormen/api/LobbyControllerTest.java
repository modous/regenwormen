package nl.hva.ewa.regenwormen.api;

import nl.hva.ewa.regenwormen.RegenwormenBackendApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = RegenwormenBackendApplication.class)
@AutoConfigureMockMvc
class LobbyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void joinLobby_addsPlayer_whenNotAlreadyIn() throws Exception {
        String playerJson = "{\"username\": \"John\", \"ready\": false}";

        mockMvc.perform(post("/api/lobbies/join/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(playerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players[0].username").value("John"));
    }

    @Test
    void joinLobby_doesNotAddDuplicatePlayer() throws Exception {
        String playerJson = "{\"username\": \"John\", \"ready\": false}";

        mockMvc.perform(post("/api/lobbies/join/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(playerJson));


        mockMvc.perform(post("/api/lobbies/join/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(playerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players.length()").value(1));
    }
}
