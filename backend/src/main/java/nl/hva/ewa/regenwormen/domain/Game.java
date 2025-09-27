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

    public Game(String gameName, int maxPlayers) {
        if (gameName == null || gameName.isBlank() || gameName.length() > 16) {
            throw new IllegalArgumentException("Game name cannot be empty or more than 16 characters");
        }
        if (maxPlayers < MIN_PLAYERS || maxPlayers > MAX_PLAYERS) {
            throw new IllegalArgumentException("Max players must be between " + MIN_PLAYERS + " and " + MAX_PLAYERS);
        }

        this.id = UUID.randomUUID();
        this.gameName = gameName.trim();
        this.maxPlayers = maxPlayers;
        this.players = new ArrayList<>();
        this.playersTurn = 0;
        this.gameState = GameState.PRE_GAME;
    }

    //Getters
    public UUID getID() {return id;}

    public String getGameName() {return gameName;}

    public int getMaxPlayers() {return maxPlayers;}

    public List<Player> getPlayers() {return Collections.unmodifiableList(players);}

    public int getTurnIndex() {return playersTurn;}


    //setters
    public void setGameName(String gameName) {
        if (gameName == null || gameName.isBlank() || gameName.length() > 16) {
            throw new IllegalArgumentException("Game name cannot be empty or more than 16 characters");
        } else {
            this.gameName = gameName.trim();
        }
    }

    public void setMaxPlayers(int maxPlayers){
        if (maxPlayers < MIN_PLAYERS || maxPlayers > MAX_PLAYERS) {
            throw new IllegalArgumentException("Max players must be between "+MIN_PLAYERS+"and "+MAX_PLAYERS);
        }
        this.maxPlayers = maxPlayers;
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
        if(getTurnIndex() == maxIndexPlayers){this.playersTurn = 0;}
        else {this.playersTurn++;}
    }


}
