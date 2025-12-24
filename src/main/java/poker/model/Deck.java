package poker.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards;
    private int currentIndex;

    public Deck() {
        cards = new ArrayList<>(52);
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(cards);
        currentIndex = 0;
    }

    public Card deal() {
        if (currentIndex >= cards.size()) {
            throw new IllegalStateException("No cards left in deck");
        }
        return cards.get(currentIndex++);
    }

    public List<Card> deal(int count) {
        List<Card> dealt = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            dealt.add(deal());
        }
        return dealt;
    }

    public int remainingCards() {
        return cards.size() - currentIndex;
    }

    public void reset() {
        shuffle();
    }
}
