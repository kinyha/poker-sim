package poker.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Hand implements Comparable<Hand> {
    private final HandRank rank;
    private final List<Card> cards;
    private final List<Integer> kickers;

    public Hand(HandRank rank, List<Card> cards, List<Integer> kickers) {
        this.rank = Objects.requireNonNull(rank);
        this.cards = Collections.unmodifiableList(new ArrayList<>(cards));
        this.kickers = Collections.unmodifiableList(new ArrayList<>(kickers));
    }

    public HandRank getRank() {
        return rank;
    }

    public List<Card> getCards() {
        return cards;
    }

    public List<Integer> getKickers() {
        return kickers;
    }

    @Override
    public int compareTo(Hand other) {
        int rankCompare = Integer.compare(this.rank.getStrength(), other.rank.getStrength());
        if (rankCompare != 0) {
            return rankCompare;
        }

        for (int i = 0; i < kickers.size() && i < other.kickers.size(); i++) {
            int kickerCompare = Integer.compare(kickers.get(i), other.kickers.get(i));
            if (kickerCompare != 0) {
                return kickerCompare;
            }
        }
        return 0;
    }

    public boolean beats(Hand other) {
        return this.compareTo(other) > 0;
    }

    public boolean ties(Hand other) {
        return this.compareTo(other) == 0;
    }

    @Override
    public String toString() {
        return rank.getRussianName() + " (" + rank.getDisplayName() + ")";
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(rank.getRussianName());
        sb.append(": ");
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(cards.get(i));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hand hand = (Hand) o;
        return rank == hand.rank && Objects.equals(kickers, hand.kickers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, kickers);
    }
}
