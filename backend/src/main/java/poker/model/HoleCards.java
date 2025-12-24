package poker.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HoleCards {
    private final Card card1;
    private final Card card2;

    public HoleCards(Card card1, Card card2) {
        Objects.requireNonNull(card1);
        Objects.requireNonNull(card2);

        if (card1.compareTo(card2) >= 0) {
            this.card1 = card1;
            this.card2 = card2;
        } else {
            this.card1 = card2;
            this.card2 = card1;
        }
    }

    public Card getCard1() {
        return card1;
    }

    public Card getCard2() {
        return card2;
    }

    public List<Card> getCards() {
        return Arrays.asList(card1, card2);
    }

    public boolean isSuited() {
        return card1.getSuit() == card2.getSuit();
    }

    public boolean isPair() {
        return card1.getRank() == card2.getRank();
    }

    public boolean isConnector() {
        int gap = Math.abs(card1.getRank().getValue() - card2.getRank().getValue());
        return gap == 1;
    }

    public int getGap() {
        return Math.abs(card1.getRank().getValue() - card2.getRank().getValue()) - 1;
    }

    public boolean isBroadway() {
        return card1.getRank().getValue() >= 10 && card2.getRank().getValue() >= 10;
    }

    public String getNotation() {
        String notation = "" + card1.getRank().getSymbol() + card2.getRank().getSymbol();
        if (isPair()) {
            return notation;
        }
        return notation + (isSuited() ? "s" : "o");
    }

    public int getHighCardValue() {
        return card1.getRank().getValue();
    }

    public int getLowCardValue() {
        return card2.getRank().getValue();
    }

    @Override
    public String toString() {
        return "[" + card1 + "][" + card2 + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoleCards holeCards = (HoleCards) o;
        return Objects.equals(card1, holeCards.card1) && Objects.equals(card2, holeCards.card2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(card1, card2);
    }
}
