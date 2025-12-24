package poker.odds;

import poker.model.GameState;
import poker.model.Player;

public class PotOddsCalculator {

    public double calculate(GameState state, Player player) {
        int potSize = state.getPot().getTotal();
        int toCall = state.getAmountToCall(player);

        if (toCall == 0) {
            return 0.0;
        }

        return (double) toCall / (potSize + toCall);
    }

    public double getRequiredEquity(GameState state, Player player) {
        return calculate(state, player);
    }

    public String explain(GameState state, Player player) {
        int potSize = state.getPot().getTotal();
        int toCall = state.getAmountToCall(player);

        if (toCall == 0) {
            return "Нет ставки для колла - можете чекнуть бесплатно!";
        }

        double potOdds = calculate(state, player);
        double requiredEquity = potOdds * 100;

        return String.format(
            "Пот: %d, Ставка для колла: %d\n" +
            "Пот-оддсы: %d / (%d + %d) = %.1f%%\n" +
            "Нужное эквити для прибыльного колла: %.1f%%",
            potSize, toCall,
            toCall, potSize, toCall, potOdds * 100,
            requiredEquity
        );
    }
}
