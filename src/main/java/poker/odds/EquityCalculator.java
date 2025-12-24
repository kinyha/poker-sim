package poker.odds;

import poker.model.*;

public class EquityCalculator {
    private final OutsCalculator outsCalculator;

    public EquityCalculator() {
        this.outsCalculator = new OutsCalculator();
    }

    public double calculate(Player player, GameState state) {
        int outs = outsCalculator.calculateOuts(player, state);
        int cardsTocome = getCardsTocome(state.getStage());

        return quickEquityEstimate(outs, cardsTocome);
    }

    private int getCardsTocome(GameStage stage) {
        return switch (stage) {
            case PREFLOP -> 5;
            case FLOP -> 2;
            case TURN -> 1;
            case RIVER, SHOWDOWN -> 0;
        };
    }

    private double quickEquityEstimate(int outs, int cardsTocome) {
        if (cardsTocome == 0) {
            return 0.0;
        }

        int multiplier = cardsTocome >= 2 ? 4 : 2;
        double estimate = outs * multiplier / 100.0;

        return Math.min(estimate, 1.0);
    }

    public String explain(Player player, GameState state) {
        int outs = outsCalculator.calculateOuts(player, state);
        int cardsTocome = getCardsTocome(state.getStage());
        double equity = quickEquityEstimate(outs, cardsTocome);

        StringBuilder sb = new StringBuilder();
        sb.append("=== Правило 4-2 ===\n");

        if (cardsTocome == 0) {
            sb.append("Все карты на столе, дро не поможет.\n");
            return sb.toString();
        }

        sb.append("Аутсов: ").append(outs).append("\n");

        if (cardsTocome >= 2) {
            sb.append("На флопе: ").append(outs).append(" × 4 = ").append(outs * 4).append("%\n");
            sb.append("(шанс попасть к риверу)\n");
        } else {
            sb.append("На тёрне: ").append(outs).append(" × 2 = ").append(outs * 2).append("%\n");
            sb.append("(шанс попасть на ривере)\n");
        }

        sb.append("Эквити: ").append(String.format("%.1f%%", equity * 100));

        return sb.toString();
    }

    public OutsCalculator.DrawInfo getDrawInfo(Player player, GameState state) {
        return outsCalculator.analyzeDraws(player, state);
    }
}
