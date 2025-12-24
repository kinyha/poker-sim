package poker.ai;

import poker.model.*;
import poker.strategy.StartingHandChart;

import java.util.Random;

public class LooseAggressiveAI implements AIStrategy {
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

        if (strength == HandStrength.TRASH && random.nextDouble() > 0.60) {
            if (toCall == 0) {
                return Action.check();
            }
            return Action.fold();
        }

        int raiseMultiplier = 3 + random.nextInt(3);
        int raiseAmount = state.getBigBlind() * raiseMultiplier;
        int totalRaise = state.getCurrentBet() + raiseAmount;

        if (totalRaise > player.getChips()) {
            return Action.allIn(player.getChips());
        }

        return Action.raise(totalRaise);
    }

    private Action decidePostflopAction(Player player, GameState state) {
        int potSize = state.getPot().getTotal();
        int toCall = state.getAmountToCall(player);

        if (random.nextDouble() < 0.60) {
            if (toCall == 0) {
                double betMultiplier = 0.75 + random.nextDouble() * 0.75;
                int betSize = (int) (potSize * betMultiplier);
                betSize = Math.max(betSize, state.getBigBlind());

                if (betSize > player.getChips()) {
                    return Action.allIn(player.getChips());
                }
                return Action.bet(betSize);
            } else {
                int raiseSize = toCall + potSize / 2;
                if (raiseSize > player.getChips()) {
                    return Action.allIn(player.getChips());
                }
                return Action.raise(state.getCurrentBet() + raiseSize);
            }
        }

        if (toCall == 0) {
            return Action.check();
        }
        return Action.fold();
    }

    @Override
    public String getPlayerTypeName() {
        return "Loose-Aggressive (Маньяк)";
    }

    @Override
    public String getPlayerTypeDescription() {
        return "Играет много рук агрессивно, часто блефует, большие ставки";
    }
}
