package poker.ai;

import poker.model.*;
import poker.strategy.StartingHandChart;

import java.util.Random;

public class CallingStationAI implements AIStrategy {
    private final Random random = new Random();
    private final StartingHandChart chart = new StartingHandChart();

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

        if (toCall == 0) {
            return Action.check();
        }

        if (strength == HandStrength.TRASH) {
            if (random.nextDouble() < 0.40) {
                return Action.call(toCall);
            }
            return Action.fold();
        }

        return Action.call(toCall);
    }

    private Action decidePostflopAction(Player player, GameState state) {
        int toCall = state.getAmountToCall(player);

        if (toCall == 0) {
            return Action.check();
        }

        if (random.nextDouble() < 0.85) {
            return Action.call(toCall);
        }

        return Action.fold();
    }

    @Override
    public String getPlayerTypeName() {
        return "Calling Station";
    }

    @Override
    public String getPlayerTypeDescription() {
        return "Коллирует почти всё, редко рейзит, не умеет фолдить";
    }
}
