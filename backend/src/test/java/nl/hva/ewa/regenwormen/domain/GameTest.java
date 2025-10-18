//package nl.hva.ewa.regenwormen.domain;
//
//import nl.hva.ewa.regenwormen.domain.Enum.GameState;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.*;
//
//class GameBasicTest {
//
//    @Test
//    void createGame_andAddPlayers_untilMax() {
//        // Arrange
//        Game g = new Game("Test", 3);
//        User user = new User().getId();
//
//        // Act
//        boolean a1 = g.addPlayer(new Player("Alice", user));
//        boolean a2 = g.addPlayer(new Player("Bob", user));
//        boolean a3 = g.addPlayer(new Player("Cara", user));
//
//        // Assert
//        assertThat(a1).isTrue();
//        assertThat(a2).isTrue();
//        assertThat(a3).isTrue();
//        assertThat(g.playersAmount()).isEqualTo(3);
//
//        // Act & Assert: boven max
//        assertThatThrownBy(() -> g.addPlayer(new Player("Dave", user)))
//                .isInstanceOf(IllegalStateException.class)
//                .hasMessageContaining("Max players");
//    }
//
//    @Test
//    void leavePlayer_inPreGame_removesPlayer() {
//        // Arrange
//        User user = new User();
//        Game g = new Game("Lobby", 4);
//        Player a = new Player("Alice", user);
//        Player b = new Player("Bob", user);
//        g.addPlayer(a);
//        g.addPlayer(b);
//        assertThat(g.playersAmount()).isEqualTo(2);
//
//        // Act
//        boolean left = g.leavePlayer(a.getId());
//
//        // Assert
//        assertThat(left).isTrue();
//        assertThat(g.playersAmount()).isEqualTo(1);
//        assertThatThrownBy(() -> g.leavePlayer("unknown"))
//                .isInstanceOf(IllegalArgumentException.class);
//    }
//
//    @Test
//    void startGame_requiresMinPlayers_andTransitionsToPlaying() {
//        // Arrange
//        Game g = new Game("Room", 4);
//        User user = new User();
//
//        // Act & Assert: te weinig spelers
//        g.addPlayer(new Player("Alice", user));
//        assertThatThrownBy(g::startGame)
//                .isInstanceOf(IllegalStateException.class)
//                .hasMessageContaining("too little players");
//
//        // Arrange
//        g.addPlayer(new Player("Bob", user));
//
//        // Act
//        g.startGame();
//
//        // Assert
//        assertThat(g.getGameState()).isEqualTo(GameState.PLAYING);
//    }
//
//    @Test
//    void setNextPlayersTurn_wrapsToZero() {
//        // Arrange
//        User user = new User();
//        Game g = new Game("Turns", 3);
//        Player a = new Player("Alice", user);
//        Player b = new Player("Bob", user);
//        Player c = new Player("Cara", user);
//        g.addPlayer(a);
//        g.addPlayer(b);
//        g.addPlayer(c);
//
//        // Act
//        assertThat(g.getTurnIndex()).isEqualTo(0); // startindex
//        g.setNextPlayersTurn(); // -> 1
//        g.setNextPlayersTurn(); // -> 2
//        g.setNextPlayersTurn(); // -> wrap -> 0
//
//        // Assert
//        assertThat(g.getTurnIndex()).isEqualTo(0);
//    }
//
//    @Test
//    void findPlayerById_returnsCorrectPlayerOrNull() {
//        // Arrange
//        User user = new User();
//        Game g = new Game("Find", 4);
//        Player a = new Player("Alice", user);
//        Player b = new Player("Bob", user);
//        g.addPlayer(a);
//        g.addPlayer(b);
//
//        // Act
//        Player p1 = g.findPlayerById(a.getId());
//        Player p2 = g.findPlayerById("does-not-exist");
//
//        // Assert
//        assertThat(p1).isEqualTo(a);
//        assertThat(p2).isNull();
//    }
//
//    @Test
//    void addPlayer_afterStart_throws() {
//        // Arrange
//        User user = new User();
//        Game g = new Game("AfterStart", 3);
//        g.addPlayer(new Player("Alice", user));
//        g.addPlayer(new Player("Bob", user));
//        g.startGame();
//
//        // Act & Assert
//        assertThatThrownBy(() -> g.addPlayer(new Player("Cara", user)))
//                .isInstanceOf(IllegalStateException.class)
//                .hasMessageContaining("Game already started");
//    }
//}
