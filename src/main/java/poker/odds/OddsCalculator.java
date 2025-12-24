package poker.odds;

import poker.model.*;

public class OddsCalculator {
    private final OutsCalculator outsCalculator;
    private final PotOddsCalculator potOddsCalculator;
    private final EquityCalculator equityCalculator;

    public OddsCalculator() {
        this.outsCalculator = new OutsCalculator();
        this.potOddsCalculator = new PotOddsCalculator();
        this.equityCalculator = new EquityCalculator();
    }

    public OddsResult calculateOdds(Player player, GameState state) {
        int outs = outsCalculator.calculateOuts(player, state);
        double equity = equityCalculator.calculate(player, state);
        double potOdds = potOddsCalculator.calculate(state, player);
        double requiredEquity = potOddsCalculator.getRequiredEquity(state, player);

        return new OddsResult(outs, equity, potOdds, requiredEquity);
    }

    public double calculateEquity(Player player, GameState state) {
        return equityCalculator.calculate(player, state);
    }

    public double calculatePotOdds(GameState state, Player player) {
        return potOddsCalculator.calculate(state, player);
    }

    public int calculateOuts(Player player, GameState state) {
        return outsCalculator.calculateOuts(player, state);
    }

    public OutsCalculator.DrawInfo getDrawInfo(Player player, GameState state) {
        return outsCalculator.analyzeDraws(player, state);
    }

    public String getFullAnalysis(Player player, GameState state) {
        StringBuilder sb = new StringBuilder();

        OutsCalculator.DrawInfo drawInfo = getDrawInfo(player, state);
        OddsResult odds = calculateOdds(player, state);

        sb.append("=== Анализ шансов ===\n\n");

        sb.append("Дро: ").append(drawInfo.getDescription()).append("\n\n");

        sb.append(equityCalculator.explain(player, state)).append("\n\n");

        sb.append(potOddsCalculator.explain(state, player)).append("\n\n");

        sb.append("=== Вывод ===\n");
        if (odds.shouldCall()) {
            sb.append("КОЛЛ выгоден! Ваше эквити (").append(String.format("%.1f%%", odds.equity() * 100));
            sb.append(") > нужного (").append(String.format("%.1f%%", odds.requiredEquity() * 100)).append(")\n");
        } else if (odds.requiredEquity() == 0) {
            sb.append("Можно чекнуть бесплатно!\n");
        } else {
            sb.append("ФОЛД лучше. Ваше эквити (").append(String.format("%.1f%%", odds.equity() * 100));
            sb.append(") < нужного (").append(String.format("%.1f%%", odds.requiredEquity() * 100)).append(")\n");
        }

        return sb.toString();
    }
}
