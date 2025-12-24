package poker.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameState {
    private final List<Player> players;
    private final List<Card> communityCards;
    private GameStage stage;
    private final Pot pot;
    private int currentBet;
    private int buttonPosition;
    private int activePlayerIndex;
    private int bigBlind;
    private int smallBlind;
    private int lastRaiserIndex;
    private int actionsThisRound;
    private boolean bettingComplete;

    public GameState(List<Player> players, int smallBlind, int bigBlind) {
        this.players = new ArrayList<>(players);
        this.communityCards = new ArrayList<>();
        this.stage = GameStage.PREFLOP;
        this.pot = new Pot();
        this.currentBet = 0;
        this.buttonPosition = 0;
        this.activePlayerIndex = 0;
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.lastRaiserIndex = -1;
        this.actionsThisRound = 0;
        this.bettingComplete = false;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public List<Card> getCommunityCards() {
        return Collections.unmodifiableList(communityCards);
    }

    public void addCommunityCard(Card card) {
        communityCards.add(card);
    }

    public void addCommunityCards(List<Card> cards) {
        communityCards.addAll(cards);
    }

    public GameStage getStage() {
        return stage;
    }

    public void setStage(GameStage stage) {
        this.stage = stage;
    }

    public void advanceStage() {
        this.stage = stage.next();
        resetBettingRound();
    }

    public Pot getPot() {
        return pot;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(int bet) {
        this.currentBet = bet;
    }

    public int getButtonPosition() {
        return buttonPosition;
    }

    public void setButtonPosition(int position) {
        this.buttonPosition = position;
    }

    public void moveButton() {
        buttonPosition = (buttonPosition + 1) % players.size();
    }

    public int getActivePlayerIndex() {
        return activePlayerIndex;
    }

    public void setActivePlayerIndex(int index) {
        this.activePlayerIndex = index;
    }

    public Player getCurrentPlayer() {
        return players.get(activePlayerIndex);
    }

    public void nextPlayer() {
        int startIndex = activePlayerIndex;
        int attempts = 0;
        do {
            activePlayerIndex = (activePlayerIndex + 1) % players.size();
            attempts++;
            if (attempts > players.size()) {
                break;
            }
        } while (!players.get(activePlayerIndex).canAct());
        actionsThisRound++;
    }

    public boolean hasPlayersWhoCanAct() {
        return players.stream().anyMatch(Player::canAct);
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public int getSmallBlind() {
        return smallBlind;
    }

    public Player getSmallBlindPlayer() {
        int sbIndex = (buttonPosition + 1) % players.size();
        return players.get(sbIndex);
    }

    public Player getBigBlindPlayer() {
        int bbIndex = (buttonPosition + 2) % players.size();
        return players.get(bbIndex);
    }

    public int getPlayersInHand() {
        return (int) players.stream().filter(Player::isInHand).count();
    }

    public int getActivePlayersCount() {
        return (int) players.stream().filter(Player::canAct).count();
    }

    public List<Player> getActivePlayers() {
        return players.stream().filter(Player::canAct).toList();
    }

    public List<Player> getPlayersStillInHand() {
        return players.stream().filter(Player::isInHand).toList();
    }

    public int getAmountToCall(Player player) {
        return Math.max(0, currentBet - player.getCurrentBet());
    }

    public int getMinRaise() {
        return currentBet + bigBlind;
    }

    public Position getPlayerPosition(Player player) {
        int playerIndex = players.indexOf(player);
        int relativePosition = (playerIndex - buttonPosition + players.size()) % players.size();
        List<Position> positions = Position.getPositionsForPlayerCount(players.size());
        if (relativePosition < positions.size()) {
            return positions.get(relativePosition);
        }
        return Position.MIDDLE_POSITION_1;
    }

    public int getRaiserCount() {
        return (int) players.stream()
            .filter(p -> p.getCurrentBet() > bigBlind)
            .count();
    }

    public boolean hasLimpers() {
        return players.stream()
            .anyMatch(p -> p.getCurrentBet() == bigBlind && !p.isFolded() && p != getBigBlindPlayer());
    }

    public int getLastRaiserIndex() {
        return lastRaiserIndex;
    }

    public void setLastRaiserIndex(int index) {
        this.lastRaiserIndex = index;
    }

    public int getActionsThisRound() {
        return actionsThisRound;
    }

    public void resetBettingRound() {
        currentBet = 0;
        actionsThisRound = 0;
        lastRaiserIndex = -1;
        bettingComplete = false;
        for (Player player : players) {
            player.resetBetForNewRound();
        }
        if (stage == GameStage.PREFLOP) {
            activePlayerIndex = (buttonPosition + 3) % players.size();
        } else {
            activePlayerIndex = (buttonPosition + 1) % players.size();
            int startIndex = activePlayerIndex;
            while (!players.get(activePlayerIndex).canAct()) {
                activePlayerIndex = (activePlayerIndex + 1) % players.size();
                if (activePlayerIndex == startIndex) break;
            }
        }
    }

    public boolean isBettingComplete() {
        return bettingComplete;
    }

    public void setBettingComplete(boolean complete) {
        this.bettingComplete = complete;
    }

    public boolean isShowdown() {
        return stage == GameStage.SHOWDOWN;
    }

    public void reset() {
        communityCards.clear();
        stage = GameStage.PREFLOP;
        pot.reset();
        currentBet = 0;
        actionsThisRound = 0;
        lastRaiserIndex = -1;
        bettingComplete = false;
        for (Player player : players) {
            player.resetForNewHand();
        }
    }
}
