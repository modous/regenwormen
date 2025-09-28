package nl.hva.ewa.regenwormen.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Game {
    private final UUID id;
    private String gameName;
    private int maxPlayers ;
    private List<Player> players;
    private int playersTurn;
    private GameState gameState;

    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 8;
    private static final int MAX_NAME_LENGTH = 16;

    public Game(String gameName, int maxPlayers) {
        setGameNameInternal(gameName);
        setMaxPlayersInternal(maxPlayers);

        this.id = UUID.randomUUID();
        this.players = new ArrayList<>();
        this.playersTurn = 0;
        this.gameState = GameState.PRE_GAME;
    }

    //Getters
    public UUID getId() {return id;}

    public String getGameName() {return gameName;}

    public int getMaxPlayers() {return maxPlayers;}

    public List<Player> getPlayers() {return Collections.unmodifiableList(players);}

    public int playersAmount(){return  players.size();}

    public int getTurnIndex() {return playersTurn;}

    public GameState getGameState() {return gameState;}

    //setters
    public void setGameName(String gameName) {
        setGameNameInternal(gameName);

    }

    public void setMaxPlayers(int maxPlayers){
        setMaxPlayersInternal(maxPlayers);
    }

    //logica
    public Player getPlayer(int index) {
        if(index < 0 || index >= players.size()){throw new IndexOutOfBoundsException("Invalid player index");}
        return players.get(index);
    }

    public Player getPlayersTurn(){
        if(players.size() == 0) {return null;}
        return getPlayer(getTurnIndex());
    }

    public void setNextPlayersTurn() {
        int maxIndexPlayers = getPlayers().size()-1;
        this.playersTurn = (getTurnIndex() == maxIndexPlayers) ? 0 : playersTurn+1;
    }

    public boolean addPlayer (Player player) {
        if (player == null){return false;}
        if (gameState != GameState.PRE_GAME) {throw new IllegalStateException("Game already started. Cannot join current game.");}
        if (playersAmount() >= maxPlayers){ throw new IllegalStateException("Max players how game is already reached");}

        return players.add(player);
    }

    public void startGame(){
        if (gameState != GameState.PRE_GAME) {throw new IllegalStateException("Game already started/ended");}
        if (playersAmount() < MIN_PLAYERS){throw new IllegalStateException("too little players to start the game");}
        if (playersAmount() > MAX_PLAYERS){throw new IllegalStateException("too many players to start the game");}
        gameState = GameState.PLAYING;
    }

    public void endGame() {
        if (gameState != GameState.PLAYING) return;
        gameState = GameState.ENDED;
    }

    // ---- helpers ----
    private void setGameNameInternal(String gameName) {
        if (gameName == null || gameName.isBlank() || gameName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Game name must be 1–" + MAX_NAME_LENGTH + " characters");}
        this.gameName = gameName.trim();
    }

    private void setMaxPlayersInternal(int maxPlayers) {
        if (maxPlayers < MIN_PLAYERS || maxPlayers > MAX_PLAYERS) {
            throw new IllegalArgumentException("Max players must be between " + MIN_PLAYERS + " and " + MAX_PLAYERS);
        }
        this.maxPlayers = maxPlayers;
    }


}
