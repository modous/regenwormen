package nl.hva.ewa.regenwormen.domain;

import nl.hva.ewa.regenwormen.domain.dto.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Game {
    private final String id;
    private String gameName;
    private List<Player> players;

    private int round = 0;
    private GameState gameState;
    private TilesPot tilesPot;

    private List<PlayersLeaderboardView> leaderboard;

    private int currentPlayersTurnIndex;
    private int maxPlayers ;

    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 8;
    private static final int MAX_NAME_LENGTH = 16;

    private static final Comparator<Player> ROUND_ZERO_ORDER =
            Comparator.comparingInt(Player::getDoublePointsTile)
                    .reversed()
                    .thenComparing(Player::getId);

    private static final Comparator<Player> SORT_LEADERBOARD =
            Comparator.comparingInt(Player::getPoints).reversed();



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
    public void setGameName(String gameName) {setGameNameInternal(gameName);}
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

    public boolean addPlayer (Player player) {
        if (player == null){return false;}
        if (gameState != GameState.PRE_GAME) {throw new IllegalStateException("Game already started. Cannot join current game.");}
        if (playersAmount() >= maxPlayers){ throw new IllegalStateException("Max players how game is already reached");}

        return players.add(player);
    }

    public boolean leavePlayer(String playerId){
        if (playerId == null || playerId.isBlank()){throw new IllegalArgumentException("Player is missing");}

        Player playerToLeaveGame = findPlayerById(playerId);
        if(playerToLeaveGame ==null){throw new IllegalArgumentException("Player not in game");}

        if (gameState == GameState.PRE_GAME) {
            players.remove(playerToLeaveGame);
            return true;
        }

        int leavingIdx = players.indexOf(playerToLeaveGame);
        boolean wasCurrent = (leavingIdx == currentPlayersTurnIndex);
        boolean leavingIdxIsLast = (leavingIdx == (playersAmount()-1));

        if(gameState == GameState.PLAYING){
            for(Tile t : tilesPot.getTiles()){
                Player owner = t.getOwner();
                if(owner!= null && owner.equals(playerToLeaveGame)){
                    t.tileToPot();
                }
            }
            if(wasCurrent){
                playerToLeaveGame.setEndTurn();
            }
        }

        players.remove(leavingIdx);
        if(gameState == GameState.PLAYING) {
            if (players.size() < MIN_PLAYERS) {endGame();}
            if (leavingIdx < currentPlayersTurnIndex) {currentPlayersTurnIndex--;
            }
            if (leavingIdxIsLast && wasCurrent) {currentPlayersTurnIndex = 0;}
        }
        return true;
    }

    public void startGame(){
        if (gameState != GameState.PRE_GAME) {throw new IllegalStateException("Game already started/ended");}
        if (playersAmount() < MIN_PLAYERS){throw new IllegalStateException("too little players to start the game");}
        if (playersAmount() > MAX_PLAYERS){throw new IllegalStateException("too many players to start the game");}
        gameState = GameState.PLAYING;
        tilesPot = new TilesPot();
        startRoundZero();
    }

    //MIDGAME Flow
    private boolean startRoundZero(){
        ensurePlaying();
        if(round != 0){throw new IllegalStateException("Round must be round: 0");}
        for(Player player : players){
            player.setStartTurn(new Diceroll(player));
        }
        return true;
    }

    private void finalizeRoundZeroAndSetTurnOrder(){
        ensurePlaying();
        if (round != 0) {throw new IllegalStateException("Turn order only set at end of round 0");}
        for(Player p: players){
            p.setEndTurn();
        }
        players.sort(ROUND_ZERO_ORDER);
        round = 1;
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
        if(options.isEmpty()){
            finishRoundZero(p);
            return TurnView.bust(p);
        }
        return TurnView.turnViewThrown(p, options, hasMinValueToStop(p.getDiceRoll().getTakenScore()));
    }

    public TurnView pickDiceFaceZero(Player p, DiceFace face){
        int taken = p.getDiceRoll().pickDice(face);
        TurnView dto = TurnView.turnViewChosen(p, hasMinValueToStop(taken));
        return dto;
    }

    public EndTurnView finishRoundZero(Player p){
        //adding double points round zero value.
        ensurePlaying();
        Diceroll roll = p.getDiceRoll();
        if(roll == null){throw new IllegalStateException("No active round zero roll");}
        if(roll.getBusted()){p.setDoublePointsTile(0);}
        else {
            if (!roll.hasSpecial()) {throw new IllegalStateException("You must have a SPECIAL to finish");}
            int score = roll.getTakenScore();
            if (score < 21) {throw new IllegalStateException("You must throw atleast 21");}
            p.setDoublePointsTile(score);
        }

        //checking if all zero round have been played
        boolean unfinishedRoundZero = roundZerounfinished();
        EndTurnView dto = EndTurnView.roundZero(p);

        if(!unfinishedRoundZero){ finalizeRoundZeroAndSetTurnOrder();}
        return dto;
    }

    //MIDGAME round >=1 rolls
    public TurnView startAndRollRound() {
        ensurePlaying();
        Player p = getCurrentPlayer();
        p.setStartTurn(new Diceroll(p));

        List<DiceFace> options = throwDices(p);
        TurnView dto = TurnView.turnViewThrown(p, options, hasMinValueToStop(p.getDiceRoll().getTakenScore()));

        return dto;
    }

    public TurnView reRollRound(){
        ensurePlaying();
        Player p = getCurrentPlayer();

        if (!p.getDiceRoll().canRollPreCheck()){throw new IllegalStateException("Cannot throw anymore");}
        List<DiceFace> options = throwDices(p);
        if(options.isEmpty()){
            finishRound();
            return TurnView.bust(p);
        }
        return TurnView.turnViewThrown(p, options, hasMinValueToStop(p.getDiceRoll().getTakenScore()));
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
        return options;
    }

    public EndTurnView finishRound(){
        ensurePlaying();
        Player p = getCurrentPlayer();
        Diceroll roll = p.getDiceRoll();
        if(roll == null){throw new IllegalStateException("No active round roll");}
        if(roll.getBusted()){
            handleBust(p);
            return EndTurnView.round(p, new ClaimOptions(p.getId(), List.of(), List.of()));
        }
        int score = roll.getTakenScore();
        if(!hasMinValueToStop(score)){throw new IllegalStateException("Not enough points to pick something");}
        ClaimOptions tilePickOptions = buildClaimOptions(p);

        return EndTurnView.round(p, tilePickOptions);
    }

    //Action after throwing
    public void claimFromPot() {
        ensurePlaying();
        Player p = getCurrentPlayer();

        int score = p.getDiceRoll().getTakenScore();

        Tile t = tilesPot.findAvailableTileByScore(score);
        if (t == null ) { throw new IllegalArgumentException("Tile not available in pot"); }
        t.takeTile(p);
        p.setEndTurn();
        if(endGameCheck()){endGame();}
        setNextPlayersTurn();
    }

    public void stealTopTile(int value) {
        ensurePlaying();
        Player victim = tilesPot.findTileByValue(value).getOwner();
        String victimPlayerId = victim.getId();
        Player thief = getCurrentPlayer();


        Tile top = victim.getTopTile();
        if (top == null || top.getValue() != value) {
            throw new IllegalArgumentException("Nothing to steal");
        }

        victim.loseTopTileToStack();
        top.takeTile(thief);
        thief.addTile(top);
        thief.setEndTurn();
        setNextPlayersTurn();
    }


    public void endGame() {
        if (gameState != GameState.PLAYING) return;
        gameState = GameState.ENDED;
        calculateLeaderboard();
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

    public Player findPlayerById(String id){
        Player playerFound = null;
        for(Player p:players){
            if (p.getId().equals(id)){
                playerFound = p;
            }
        }
        return playerFound;
    }

    private void ensurePlaying() {
        if (gameState != GameState.PLAYING) throw new IllegalStateException("Game not playing");
    }

    public void setNextPlayersTurn() {
        int maxIndexPlayers = getPlayers().size()-1;
       if (getTurnIndex() == maxIndexPlayers) {
           currentPlayersTurnIndex = 0;
           round++;
           return;
       }
       currentPlayersTurnIndex++;
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

    public boolean roundZerounfinished(){
        boolean unfinishedRoundZero = false;
        for(Player player: players){
            int doublePointTileValue = player.getDoublePointsTile();
            if(doublePointTileValue < 0) {unfinishedRoundZero = true;}
        }
        return unfinishedRoundZero;
    }

    private ClaimOptions buildClaimOptions(Player p) {
        Diceroll roll = p.getDiceRoll();
        boolean hasSpecial = roll != null && roll.hasSpecial();
        if (!hasSpecial) {
            // Zonder SPECIAL mag je niets claimen of stelen
            return new ClaimOptions(p.getId(), List.of(), List.of());
        }

        int score = p.getDiceRoll().getTakenScore();

        // 1) Pot-tiles die je mag pakken: score >= tile value & tile is beschikbaar
        List<Integer> claimablePot = new ArrayList<>();
        for (Tile t : tilesPot.getAvailableTiles()) {
            if (score >= t.getValue()) {
                claimablePot.add(t.getValue());
            }
        }

        // 2) Steal-opties: score == andermans top tile value
        List<StealOptions> steals = new ArrayList<>();
        for (Player victim : players) {
            if (victim.equals(p)) continue;
            Tile top = victim.getTopTile();
            if (top != null && score == top.getValue()) {
                steals.add(new StealOptions(victim.getId(), top.getValue()));
            }
        }

        return new ClaimOptions(p.getId(), claimablePot, steals);
    }

    private void handleBust(Player p) {
        Tile top = p.getTopTile();
        if (top != null) {
            top.tileToPot();
            p.loseTopTileToStack();
        }
        tilesPot.flipHighestAvailableTileIfAny();
        p.setEndTurn();
        if(endGameCheck()){endGame();}
        setNextPlayersTurn();
    }

    private boolean endGameCheck(){
        return (tilesPot.amountAvailableTiles() == 0)  ? true : false;
    }

    private void calculateLeaderboard(){
        List<Player> sorted = players.stream()
                .sorted(Comparator.comparingInt(Player::getPoints).reversed())
                .toList();

        List<PlayersLeaderboardView> withRank = new ArrayList<PlayersLeaderboardView>();
        int rank = 0;
        int lastscore = -1;
        for (Player p : sorted){
            int pts = p.getPoints();
            if (pts != lastscore) {
                rank++;
                lastscore = pts;
            }
            withRank.add(new PlayersLeaderboardView(p.getId(), p.getName(), pts, rank));
        }
        leaderboard = withRank;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Game)) return false;
        Game other = (Game) obj;
        return id.equals(other.id);
    }


}
