package poker.model;

import java.util.Objects;

public class Card implements Comparable<Card> {
    private final Rank rank;
    private final Suit suit;

    public Card(Rank rank, Suit suit) {
        this.rank = Objects.requireNonNull(rank);
        this.suit = Objects.requireNonNull(suit);
    }

    public static Card of(String notation) {
        if (notation == null || notation.length() != 2) {
            throw new IllegalArgumentException("Invalid card notation: " + notation);
        }
        Rank rank = Rank.fromSymbol(notation.substring(0, 1));
        Suit suit = Suit.fromCode(notation.substring(1, 2));
        return new Card(rank, suit);
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    @Override
    public int compareTo(Card other) {
        return Integer.compare(this.rank.getValue(), other.rank.getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return rank == card.rank && suit == card.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, suit);
    }

    @Override
    public String toString() {
        return rank.getSymbol() + suit.getSymbol();
    }

    public String toNotation() {
        return rank.getSymbol() + suit.getCode();
    }
}
