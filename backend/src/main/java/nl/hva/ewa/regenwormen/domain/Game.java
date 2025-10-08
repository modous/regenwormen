package nl.hva.ewa.regenwormen.domain;

import nl.hva.ewa.regenwormen.domain.dto.TurnView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game {
    private final String id;
    private String gameName;
    private int maxPlayers ;
    private List<Player> players;
    private int currentPlayersTurnIndex;
    private GameState gameState;
    private int round = 0;
    private TilesPot tilesPot;

    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 8;
    private static final int MAX_NAME_LENGTH = 16;

    public Game(String gameName, int maxPlayers) {
        setGameNameInternal(gameName);
        setMaxPlayersInternal(maxPlayers);

        this.id = Helpers.generateShortHexId(6);
        this.players = new ArrayList<>();
        this.currentPlayersTurnIndex = 0;
        this.gameState = GameState.PRE_GAME;
    }

    //Getters
    public String getId() {return id;}

    public String getGameName() {return gameName;}

    public int getMaxPlayers() {return maxPlayers;}

    public List<Player> getPlayers() {return Collections.unmodifiableList(players);}

    public int playersAmount(){return  players.size();}

    public int getTurnIndex() {return currentPlayersTurnIndex;}

    public Player getCurrentPlayer(){return players.get(getTurnIndex());}

    public GameState getGameState() {return gameState;}

    //setters
    public void setGameName(String gameName) {
        setGameNameInternal(gameName);

    }

    public void setMaxPlayers(int maxPlayers){
        setMaxPlayersInternal(maxPlayers);
    }

    //Logic
    //Pregame
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
        this.currentPlayersTurnIndex = (getTurnIndex() == maxIndexPlayers) ? 0 : currentPlayersTurnIndex+1;
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
        tilesPot = new TilesPot();
        roundZero();
    }

    //MIDGAME Flow
    public void roundZero(){
        ensurePlaying();
        if(round != 0){throw new IllegalStateException("Round must be round: 0");}
        for(Player player : players){
            player.setStartTurn(new Diceroll(player));
        }
    }

    //MIDGAME roundZero actions
    public TurnView startAndRollRoundZero(Player p) {
        ensurePlaying();

        List<DiceFace> options = throwDices(p);
        TurnView dto = TurnView.turnViewThrown(p, options, hasMinValueToStop(p.getDiceRoll().getTakenScore()));

        return dto;
    }

    public TurnView reRollRoundZero(Player p){
        ensurePlaying();
        if (!p.getDiceRoll().canRollPreCheck()){throw new IllegalStateException("Cannot throw anymore");}
        List<DiceFace> options = throwDices(p);
        TurnView dto = options.isEmpty() ? TurnView.bust(p) : TurnView.turnViewThrown(p, options, hasMinValueToStop(p.getDiceRoll().getTakenScore()));
        return dto;
    }

    public TurnView pickDiceFaceZero(Player p, DiceFace face){
        int taken = p.getDiceRoll().pickDice(face);
        TurnView dto = TurnView.turnViewChosen(p, hasMinValueToStop(taken));
        return dto;
    }

    //MIDGAME round >=1
    public TurnView startAndRollRound() {
        ensurePlaying();
        Player p = getCurrentPlayer();

        List<DiceFace> options = throwDices(p);
        TurnView dto = TurnView.turnViewThrown(p, options, hasMinValueToStop(p.getDiceRoll().getTakenScore()));

        return dto;
    }

    public TurnView reRollRound(){
        ensurePlaying();
        Player p = getCurrentPlayer();

        if (!p.getDiceRoll().canRollPreCheck()){throw new IllegalStateException("Cannot throw anymore");}
        List<DiceFace> options = throwDices(p);
        TurnView dto = options.isEmpty() ? TurnView.bust(p) : TurnView.turnViewThrown(p, options, hasMinValueToStop(p.getDiceRoll().getTakenScore()));
        return dto;
    }

    public TurnView pickDiceFace(DiceFace face){
        ensurePlaying();
        Player p = getCurrentPlayer();

        int taken = p.getDiceRoll().pickDice(face);
        TurnView dto = TurnView.turnViewChosen(p, hasMinValueToStop(taken));
        return dto;
    }


    public List<DiceFace> throwDices (Player player){
        List<DiceFace> options = player.getDiceRoll().rollRemainingDice();
        if(options.isEmpty()){return null;}
        return options;
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

    private void ensurePlaying() {
        if (gameState != GameState.PLAYING) throw new IllegalStateException("Game not playing");
    }

    private void addDoubleValue(Player player, int value) {
        if(value < 21){return;}
        player.setDoublePointsTile(value);
    }

    public boolean hasMinValueToStop(int pointsThrown){
        int lowestPot = tilesPot.getLowestAvailableTileValue();
        List<Integer> topTileValues = new ArrayList<Integer>();
        for(Player p: players){
            Tile t = p.getTopTile();
            if(t != null) {
                topTileValues.add(t.getValue());
            }
        }
        return pointsThrown >= lowestPot || topTileValues.contains(pointsThrown);
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Game)) return false;
        Game other = (Game) obj;
        return id.equals(other.id);
    }


}
