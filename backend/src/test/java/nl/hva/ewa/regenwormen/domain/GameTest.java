package nl.hva.ewa.regenwormen.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GameBasicTest {

    @Test
    void createGame_andAddPlayers_untilMax() {
        // Arrange
        Game g = new Game("Test", 3);

        // Act
        boolean a1 = g.addPlayer(new Player("Alice"));
        boolean a2 = g.addPlayer(new Player("Bob"));
        boolean a3 = g.addPlayer(new Player("Cara"));

        // Assert
        assertThat(a1).isTrue();
        assertThat(a2).isTrue();
        assertThat(a3).isTrue();
        assertThat(g.playersAmount()).isEqualTo(3);

        // Act & Assert: boven max
        assertThatThrownBy(() -> g.addPlayer(new Player("Dave")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Max players");
    }

    @Test
    void leavePlayer_inPreGame_removesPlayer() {
        // Arrange
        Game g = new Game("Lobby", 4);
        Player a = new Player("Alice");
        Player b = new Player("Bob");
        g.addPlayer(a);
        g.addPlayer(b);
        assertThat(g.playersAmount()).isEqualTo(2);

        // Act
        boolean left = g.leavePlayer(a.getId());

        // Assert
        assertThat(left).isTrue();
        assertThat(g.playersAmount()).isEqualTo(1);
        assertThatThrownBy(() -> g.leavePlayer("unknown"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void startGame_requiresMinPlayers_andTransitionsToPlaying() {
        // Arrange
        Game g = new Game("Room", 4);

        // Act & Assert: te weinig spelers
        g.addPlayer(new Player("Alice"));
        assertThatThrownBy(g::startGame)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too little players");

        // Arrange
        g.addPlayer(new Player("Bob"));

        // Act
        g.startGame();

        // Assert
        assertThat(g.getGameState()).isEqualTo(GameState.PLAYING);
    }

    @Test
    void setNextPlayersTurn_wrapsToZero() {
        // Arrange
        Game g = new Game("Turns", 3);
        Player a = new Player("Alice");
        Player b = new Player("Bob");
        Player c = new Player("Cara");
        g.addPlayer(a);
        g.addPlayer(b);
        g.addPlayer(c);

        // Act
        assertThat(g.getTurnIndex()).isEqualTo(0); // startindex
        g.setNextPlayersTurn(); // -> 1
        g.setNextPlayersTurn(); // -> 2
        g.setNextPlayersTurn(); // -> wrap -> 0

        // Assert
        assertThat(g.getTurnIndex()).isEqualTo(0);
    }

    @Test
    void findPlayerById_returnsCorrectPlayerOrNull() {
        // Arrange
        Game g = new Game("Find", 4);
        Player a = new Player("Alice");
        Player b = new Player("Bob");
        g.addPlayer(a);
        g.addPlayer(b);

        // Act
        Player p1 = g.findPlayerById(a.getId());
        Player p2 = g.findPlayerById("does-not-exist");

        // Assert
        assertThat(p1).isEqualTo(a);
        assertThat(p2).isNull();
    }

    @Test
    void addPlayer_afterStart_throws() {
        // Arrange
        Game g = new Game("AfterStart", 3);
        g.addPlayer(new Player("Alice"));
        g.addPlayer(new Player("Bob"));
        g.startGame();

        // Act & Assert
        assertThatThrownBy(() -> g.addPlayer(new Player("Cara")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Game already started");
    }
}
