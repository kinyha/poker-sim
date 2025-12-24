package poker.evaluation;

import poker.model.*;

import java.util.*;

public class WinnerDeterminer {
    private final HandEvaluator evaluator = new HandEvaluator();

    public List<Player> determineWinners(List<Player> players, List<Card> communityCards) {
        Map<Player, Hand> playerHands = new HashMap<>();

        for (Player player : players) {
            if (!player.isFolded()) {
                List<Card> allCards = new ArrayList<>(communityCards);
                allCards.addAll(player.getHoleCards().getCards());
                playerHands.put(player, evaluator.evaluate(allCards));
            }
        }

        if (playerHands.isEmpty()) {
            return Collections.emptyList();
        }

        Hand bestHand = playerHands.values().stream()
            .max(Comparator.naturalOrder())
            .orElseThrow();

        return playerHands.entrySet().stream()
            .filter(e -> e.getValue().compareTo(bestHand) == 0)
            .map(Map.Entry::getKey)
            .toList();
    }

    public Map<Player, Hand> evaluateAllHands(List<Player> players, List<Card> communityCards) {
        Map<Player, Hand> hands = new HashMap<>();
        for (Player player : players) {
            if (!player.isFolded()) {
                List<Card> allCards = new ArrayList<>(communityCards);
                allCards.addAll(player.getHoleCards().getCards());
                hands.put(player, evaluator.evaluate(allCards));
            }
        }
        return hands;
    }

    public ShowdownResult resolveShowdown(GameState state) {
        List<Player> playersInHand = state.getPlayersStillInHand();
        List<Card> communityCards = state.getCommunityCards();

        if (playersInHand.size() == 1) {
            Player winner = playersInHand.get(0);
            int potAmount = state.getPot().getTotal();
            return new ShowdownResult(
                List.of(winner),
                Map.of(winner, potAmount),
                null,
                false
            );
        }

        Map<Player, Hand> hands = evaluateAllHands(playersInHand, communityCards);
        List<Player> winners = determineWinners(playersInHand, communityCards);

        int potAmount = state.getPot().getTotal();
        int splitAmount = potAmount / winners.size();
        int remainder = potAmount % winners.size();

        Map<Player, Integer> winnings = new HashMap<>();
        for (int i = 0; i < winners.size(); i++) {
            int amount = splitAmount + (i < remainder ? 1 : 0);
            winnings.put(winners.get(i), amount);
        }

        Hand winningHand = hands.get(winners.get(0));
        boolean isSplit = winners.size() > 1;

        return new ShowdownResult(winners, winnings, winningHand, isSplit);
    }

    public record ShowdownResult(
        List<Player> winners,
        Map<Player, Integer> winnings,
        Hand winningHand,
        boolean isSplit
    ) {
        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            if (isSplit) {
                sb.append("Split pot между: ");
                for (Player winner : winners) {
                    sb.append(winner.getName()).append(" ");
                }
            } else {
                Player winner = winners.get(0);
                sb.append(winner.getName()).append(" выигрывает ");
                sb.append(winnings.get(winner)).append(" фишек");
            }
            if (winningHand != null) {
                sb.append("\nКомбинация: ").append(winningHand.getDescription());
            }
            return sb.toString();
        }
    }
}
