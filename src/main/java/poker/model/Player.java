package poker.model;

import java.util.Objects;

public abstract class Player {
    protected final String name;
    protected int chips;
    protected HoleCards holeCards;
    protected Position position;
    protected int currentBet;
    protected boolean folded;
    protected boolean allIn;
    protected int seatIndex;

    public Player(String name, int startingChips) {
        this.name = Objects.requireNonNull(name);
        this.chips = startingChips;
        this.currentBet = 0;
        this.folded = false;
        this.allIn = false;
    }

    public abstract Action decideAction(GameState gameState);

    public void bet(int amount) {
        int actualBet = Math.min(amount, chips);
        chips -= actualBet;
        currentBet += actualBet;
        if (chips == 0) {
            allIn = true;
        }
    }

    public void receiveCards(HoleCards cards) {
        this.holeCards = cards;
        this.folded = false;
        this.currentBet = 0;
        this.allIn = false;
    }

    public void fold() {
        this.folded = true;
    }

    public void win(int amount) {
        this.chips += amount;
    }

    public void resetForNewHand() {
        holeCards = null;
        currentBet = 0;
        folded = false;
        allIn = false;
    }

    public void resetBetForNewRound() {
        currentBet = 0;
    }

    public String getName() {
        return name;
    }

    public int getChips() {
        return chips;
    }

    public HoleCards getHoleCards() {
        return holeCards;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public boolean isFolded() {
        return folded;
    }

    public boolean isAllIn() {
        return allIn;
    }

    public boolean isActive() {
        return !folded && !allIn;
    }

    public boolean isInHand() {
        return !folded;
    }

    public int getSeatIndex() {
        return seatIndex;
    }

    public void setSeatIndex(int seatIndex) {
        this.seatIndex = seatIndex;
    }

    public boolean canAct() {
        return !folded && !allIn && chips > 0;
    }

    @Override
    public String toString() {
        return name + " ($" + chips + ")";
    }
}
