package poker.engine;

import poker.model.*;

import java.util.List;

public class DealerManager {
    private Deck deck;

    public DealerManager() {
        this.deck = new Deck();
    }

    public void dealHoleCards(List<Player> players) {
        deck = new Deck();
        deck.shuffle();

        for (Player player : players) {
            Card card1 = deck.deal();
            Card card2 = deck.deal();
            player.receiveCards(new HoleCards(card1, card2));
        }
    }

    public void dealFlop(GameState state) {
        deck.deal();
        state.addCommunityCards(deck.deal(3));
    }

    public void dealTurn(GameState state) {
        deck.deal();
        state.addCommunityCard(deck.deal());
    }

    public void dealRiver(GameState state) {
        deck.deal();
        state.addCommunityCard(deck.deal());
    }

    public Deck getDeck() {
        return deck;
    }

    public void reset() {
        deck = new Deck();
    }
}
