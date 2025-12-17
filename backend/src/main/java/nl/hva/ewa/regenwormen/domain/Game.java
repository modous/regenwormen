package nl.hva.ewa.regenwormen.domain;

import nl.hva.ewa.regenwormen.domain.Enum.DiceFace;
import nl.hva.ewa.regenwormen.domain.Enum.GameState;
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
    private int maxPlayers;

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

    // === GETTERS ===
    public String getId() { return id; }
    public String getGameName() { return gameName; }
    public int getMaxPlayers() { return maxPlayers; }
    public TilesPot getTilesPot() { return tilesPot; }
    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }
    public int playersAmount() { return players.size(); }
    public int getTurnIndex() { return currentPlayersTurnIndex; }
    public GameState getGameState() { return gameState; }
    public List<PlayersLeaderboardView> getLeaderboard() {
        return leaderboard;
    }


    // === SETTERS ===
    public void setGameName(String gameName) { setGameNameInternal(gameName); }
    public void setMaxPlayers(int maxPlayers) { setMaxPlayersInternal(maxPlayers); }

    // === PRE-GAME ===
    public boolean addPlayer(Player player) {
        if (player == null) return false;
        if (findPlayerById(player.getId()) != null)
            throw new IllegalArgumentException("Player already in game");
        if (gameState != GameState.PRE_GAME)
            throw new IllegalStateException("Game already started");
        if (playersAmount() >= maxPlayers)
            throw new IllegalStateException("Max players reached");
        return players.add(player);
    }

    public boolean leavePlayer(String playerId) {
        if (playerId == null || playerId.isBlank())
            throw new IllegalArgumentException("Player missing");

        Player player = findPlayerById(playerId);
        if (player == null) throw new IllegalArgumentException("Player not in game");

        if (gameState == GameState.PRE_GAME) {
            players.remove(player);
            return true;
        }

        int leavingIdx = players.indexOf(player);
        boolean wasCurrent = leavingIdx == currentPlayersTurnIndex;
        boolean wasLast = leavingIdx == playersAmount() - 1;

        if (gameState == GameState.PLAYING) {
            player.returnAllTilesToPot();
            if (wasCurrent) player.setEndTurn();
        }

        players.remove(leavingIdx);
        if (gameState == GameState.PLAYING) {
            if (players.size() < MIN_PLAYERS) endGame();
            if (leavingIdx < currentPlayersTurnIndex) currentPlayersTurnIndex--;
            if (wasLast && wasCurrent) currentPlayersTurnIndex = 0;
        }
        return true;
    }

    public void startGame() {
        if (gameState != GameState.PRE_GAME)
            throw new IllegalStateException("Game already started/ended");
        if (playersAmount() < MIN_PLAYERS)
            throw new IllegalStateException("Too few players");
        if (playersAmount() > MAX_PLAYERS)
            throw new IllegalStateException("Too many players");

        gameState = GameState.PLAYING;
        tilesPot = new TilesPot();
        round = 1;
    }

    // === ROUND ZERO ===
    public TurnView startAndRollRoundZero(Player p) {
        ensurePlaying();
        List<DiceFace> options = throwDices(p);
        return TurnView.turnViewThrown(p, options, hasMinValueToStop(p.getDiceRoll().getTakenScore()));
    }

    public TurnView reRollRoundZero(Player p) {
        ensurePlaying();
        List<DiceFace> options = throwDices(p);
        if (options.isEmpty()) {
            finishRoundZero(p);
            return TurnView.bust(p);
        }
        return TurnView.turnViewThrown(p, options, hasMinValueToStop(p.getDiceRoll().getTakenScore()));
    }

    public TurnView pickDiceFaceZero(Player p, DiceFace face) {
        int taken = p.getDiceRoll().pickDice(face);
        return TurnView.turnViewChosen(p, hasMinValueToStop(taken));
    }

    public EndTurnView finishRoundZero(Player p) {
        ensurePlaying();
        Diceroll roll = p.getDiceRoll();
        if (roll == null) throw new IllegalStateException("No active round zero roll");

        if (roll.getBusted()) p.setDoublePointsTile(0);
        else {
            if (!roll.hasSpecial()) throw new IllegalStateException("Must have SPECIAL");
            int score = roll.getTakenScore();
            if (score < 21) throw new IllegalStateException("Min 21 points");
            p.setDoublePointsTile(score);
        }
        return EndTurnView.roundZero(p);
    }

    // === NORMAL ROUNDS ===
    public TurnView startAndRollRound() {
        ensurePlaying();
        Player p = getCurrentPlayer();
        p.setStartTurn(new Diceroll());
        List<DiceFace> options = p.getDiceRoll().rollRemainingDice();
        return TurnView.turnViewThrown(p, options, hasMinValueToStop(p.getDiceRoll().getTakenScore()));
    }

    public TurnView reRollRound() {
        ensurePlaying();
        Player p = getCurrentPlayer();

        Diceroll roll = p.getDiceRoll();
        if (roll == null) throw new IllegalStateException("No active roll");
        if (roll.getTurnState().toString().equals("ENDED") || roll.getBusted()) {
            handleBust(p);
            return TurnView.bust(p);
        }

        List<DiceFace> options = roll.rollRemainingDice();
        if (options.isEmpty()) {
            handleBust(p);
            return TurnView.bust(p);
        }

        return TurnView.turnViewThrown(p, options, hasMinValueToStop(roll.getTakenScore()));
    }

    public List<DiceFace> throwDices(Player player) {
        if (player.getDiceRoll() == null) {
            player.setStartTurn(new Diceroll());
        }
        return player.getDiceRoll().rollRemainingDice();
    }

    public TurnView pickDiceFace(DiceFace face) {
        ensurePlaying();
        Player p = getCurrentPlayer();
        int taken = p.getDiceRoll().pickDice(face);
        return TurnView.turnViewChosen(p, hasMinValueToStop(taken));
    }

    public EndTurnView finishRound() {
        ensurePlaying();
        Player p = getCurrentPlayer();
        Diceroll roll = p.getDiceRoll();
        if (roll == null) throw new IllegalStateException("No active roll");
        if (roll.getBusted()) {
            handleBust(p);
            return EndTurnView.round(p, new ClaimOptions(p.getId(), List.of(), List.of()));
        }

        int score = roll.getTakenScore();
        if (!hasMinValueToStop(score))
            throw new IllegalStateException("Not enough points to pick something");

        ClaimOptions options = buildClaimOptions(p);
        return EndTurnView.round(p, options);
    }

    // === TILE CLAIMING ===
    public Game claimFromPot() {
        ensurePlaying();

        Player player = getCurrentPlayer();
        Diceroll roll = player.getDiceRoll();
        if (roll == null || roll.getBusted())
            throw new IllegalStateException("No valid roll to claim from.");
        if (!roll.hasSpecial())
            throw new IllegalStateException("Need at least one worm to claim a tile.");

        int score = roll.getTakenScore();
        Tile claimedTile = tilesPot.findHighestAvailableTileAtOrBelow(score);
        if (claimedTile == null)
            throw new IllegalArgumentException("No tile available at or below your score (" + score + ").");

        // ✅ Give tile to player
        claimedTile.takeTile(player);
        player.addTile(claimedTile);

        player.setEndTurn();
        if (endGameCheck()) endGame();
        else setNextPlayersTurn();

        return this;
    }

    // === TILE STEALING ===
    public TilesPot stealTopTile(String victimId) {
        ensurePlaying();
        Player thief = getCurrentPlayer();
        Player victim = findPlayerById(victimId);
        if (victim == null) throw new IllegalArgumentException("Victim not found");

        Tile top = victim.getTopTile();
        if (top == null) throw new IllegalArgumentException("Nothing to steal");

        Diceroll roll = thief.getDiceRoll();
        if (roll == null || roll.getBusted())
            throw new IllegalStateException("No active roll");
        if (!roll.hasSpecial())
            throw new IllegalStateException("SPECIAL required");
        if (roll.getTakenScore() != top.getValue())
            throw new IllegalArgumentException("Score must equal victim’s top tile value");

        victim.loseTopTileToStack();
        top.takeTile(thief);
        thief.addTile(top);
        thief.setEndTurn();
        setNextPlayersTurn();

        return getTilesPot();
    }

    // === SUPPORT / ENDGAME ===
    public void endGame() {
        if (gameState != GameState.PLAYING) return;
        gameState = GameState.ENDED;
        calculateLeaderboard();
    }

    private void ensurePlaying() {
        if (gameState != GameState.PLAYING)
            throw new IllegalStateException("Game not playing");
    }

    public Player getCurrentPlayer() {
        if (players == null || players.isEmpty()) return null;
        if (currentPlayersTurnIndex < 0 || currentPlayersTurnIndex >= players.size()) return null;
        return players.get(currentPlayersTurnIndex);
    }

    public Player findPlayerById(String id) {
        for (Player p : players)
            if (p.getId().equals(id)) return p;
        return null;
    }

    public void setNextPlayersTurn() {
        int maxIndex = players.size() - 1;
        if (currentPlayersTurnIndex == maxIndex) {
            currentPlayersTurnIndex = 0;
            round++;
        } else currentPlayersTurnIndex++;
    }

    private boolean endGameCheck() {
        return tilesPot.amountAvailableTiles() == 0;
    }

    private void handleBust(Player p) {
        Tile top = p.getTopTile();
        if (top != null) {
            top.tileToPot();
            p.loseTopTileToStack();
        }
        tilesPot.flipHighestAvailableTileIfAny();
        p.setEndTurn();
        if (endGameCheck()) endGame();
        setNextPlayersTurn();
    }

    private ClaimOptions buildClaimOptions(Player p) {
        Diceroll roll = p.getDiceRoll();
        boolean hasSpecial = roll != null && roll.hasSpecial();
        if (!hasSpecial) return new ClaimOptions(p.getId(), List.of(), List.of());

        int score = roll.getTakenScore();
        List<Integer> claimablePot = new ArrayList<>();
        for (Tile t : tilesPot.getAvailableTiles())
            if (score >= t.getValue()) claimablePot.add(t.getValue());

        List<StealOptions> steals = new ArrayList<>();
        for (Player victim : players) {
            if (victim.equals(p)) continue;
            Tile top = victim.getTopTile();
            if (top != null && score == top.getValue())
                steals.add(new StealOptions(victim.getId(), top.getValue()));
        }

        return new ClaimOptions(p.getId(), claimablePot, steals);
    }

    private void calculateLeaderboard() {
        List<Player> sorted = players.stream()
                .sorted(Comparator.comparingInt(Player::getPoints).reversed())
                .toList();

        List<PlayersLeaderboardView> withRank = new ArrayList<>();
        int rank = 0, lastScore = -1;
        for (Player p : sorted) {
            int pts = p.getPoints();
            if (pts != lastScore) {
                rank++;
                lastScore = pts;
            }
            withRank.add(new PlayersLeaderboardView(p.getId(), p.getName(), pts, rank));
        }
        leaderboard = withRank;
    }

    private void setGameNameInternal(String name) {
        if (name == null || name.isBlank() || name.length() > MAX_NAME_LENGTH)
            throw new IllegalArgumentException("Name must be 1–" + MAX_NAME_LENGTH);
        this.gameName = name.trim();
    }

    private void setMaxPlayersInternal(int max) {
        if (max < MIN_PLAYERS || max > MAX_PLAYERS)
            throw new IllegalArgumentException("Max players must be between 2 and 8");
        this.maxPlayers = max;
    }

    public boolean hasPlayer(Player player) {
        if (player == null) return false;
        for (Player p : players) {
            if (p.getId().equals(player.getId())) return true;
        }
        return false;
    }

    public boolean hasMinValueToStop(int points) {
        int lowestPot = tilesPot.getLowestAvailableTileValue();
        List<Integer> topTileValues = new ArrayList<>();
        for (Player p : players) {
            Tile t = p.getTopTile();
            if (t != null) topTileValues.add(t.getValue());
        }
        return points >= lowestPot || topTileValues.contains(points);
    }

    // === FORCE SKIP SUPPORT ===
    /**
     * Used when a player's turn expires or must be skipped.
     * It safely advances to the next player without requiring
     * any dice roll or points, cleans up dice state, and ensures
     * the game stays valid.
     */
    public void forceNextPlayer() {
        if (gameState != GameState.PLAYING) return;

        Player current = getCurrentPlayer();
        if (current != null) {
            // 🧹 End their turn cleanly
            current.setEndTurn();

            // 🪱 Apply optional bust penalty logic
            tilesPot.flipHighestAvailableTileIfAny();
        }

        // ⏩ Move to next player's turn
        setNextPlayersTurn();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Game)) return false;
        return id.equals(((Game) obj).id);
    }
}
