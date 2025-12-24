package poker.ai;

import poker.model.*;
import poker.strategy.StartingHandChart;
import poker.evaluation.HandEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TightPassiveAI implements AIStrategy {
    private final Random random = new Random();
    private final StartingHandChart chart = new StartingHandChart();
    private final HandEvaluator evaluator = new HandEvaluator();

    @Override
    public Action decide(Player player, GameState state) {
        if (state.getStage() == GameStage.PREFLOP) {
            return decidePreflopAction(player, state);
        } else {
            return decidePostflopAction(player, state);
        }
    }

    private Action decidePreflopAction(Player player, GameState state) {
        HandStrength strength = chart.getHandStrength(player.getHoleCards());
        int toCall = state.getAmountToCall(player);

        if (strength.getTier() < HandStrength.STRONG.getTier()) {
            if (toCall == 0) {
                return Action.check();
            }
            return Action.fold();
        }

        if (toCall == 0) {
            return Action.check();
        }
        return Action.call(toCall);
    }

    private Action decidePostflopAction(Player player, GameState state) {
        List<Card> allCards = new ArrayList<>(state.getCommunityCards());
        allCards.addAll(player.getHoleCards().getCards());
        Hand hand = evaluator.evaluate(allCards);

        int toCall = state.getAmountToCall(player);

        if (hand.getRank().getStrength() >= HandRank.TWO_PAIR.getStrength()) {
            if (toCall == 0) {
                return Action.check();
            }
            return Action.call(toCall);
        }

        if (toCall == 0) {
            return Action.check();
        }
        return Action.fold();
    }

    @Override
    public String getPlayerTypeName() {
        return "Tight-Passive (Скала)";
    }

    @Override
    public String getPlayerTypeDescription() {
        return "Играет мало рук, никогда не рейзит, легко блефовать";
    }
}
