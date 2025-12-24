package poker.strategy;

import poker.model.*;
import poker.odds.OddsCalculator;
import poker.evaluation.HandEvaluator;

import java.util.ArrayList;
import java.util.List;

public class RecommendationEngine {
    private final StartingHandChart preflopChart;
    private final OddsCalculator oddsCalculator;
    private final HandEvaluator handEvaluator;

    public RecommendationEngine() {
        this.preflopChart = new StartingHandChart();
        this.oddsCalculator = new OddsCalculator();
        this.handEvaluator = new HandEvaluator();
    }

    public Recommendation getRecommendation(Player player, GameState state) {
        if (state.getStage() == GameStage.PREFLOP) {
            return getPreflopRecommendation(player, state);
        } else {
            return getPostflopRecommendation(player, state);
        }
    }

    private Recommendation getPreflopRecommendation(Player player, GameState state) {
        Position position = state.getPlayerPosition(player);
        boolean facingRaise = state.getCurrentBet() > state.getBigBlind();
        int raiserCount = state.getRaiserCount();
        HoleCards cards = player.getHoleCards();

        ActionRecommendation action = preflopChart.getRecommendation(
            cards, position, facingRaise, raiserCount
        );

        String reasoning = buildPreflopReasoning(cards, position, facingRaise, raiserCount, action);
        List<String> tips = buildPreflopTips(cards, position, state);

        int suggestedAmount = 0;
        if (action == ActionRecommendation.RAISE || action == ActionRecommendation.RERAISE) {
            suggestedAmount = preflopChart.getSuggestedRaiseSize(
                state.getBigBlind(), state.getPot().getTotal(), state.hasLimpers()
            );
        } else if (action == ActionRecommendation.CALL) {
            suggestedAmount = state.getAmountToCall(player);
        }

        return new Recommendation(action, reasoning, tips, suggestedAmount);
    }

    private Recommendation getPostflopRecommendation(Player player, GameState state) {
        List<Card> allCards = new ArrayList<>(state.getCommunityCards());
        allCards.addAll(player.getHoleCards().getCards());

        Hand currentHand = handEvaluator.evaluate(allCards);
        OddsResult odds = oddsCalculator.calculateOdds(player, state);

        int toCall = state.getAmountToCall(player);
        int potSize = state.getPot().getTotal();

        ActionRecommendation action;
        String reasoning;

        if (currentHand.getRank().getStrength() >= HandRank.TWO_PAIR.getStrength()) {
            action = toCall == 0 ? ActionRecommendation.RAISE : ActionRecommendation.RAISE;
            reasoning = String.format(
                "Сильная комбинация: %s. Ставь за велью!",
                currentHand.getRank().getRussianName()
            );
        }
        else if (currentHand.getRank().getStrength() >= HandRank.PAIR.getStrength()) {
            if (toCall == 0) {
                action = ActionRecommendation.RAISE;
                reasoning = String.format(
                    "%s - ставь за велью. Новички часто коллируют со слабым.",
                    currentHand.getRank().getRussianName()
                );
            } else if (odds.shouldCall()) {
                action = ActionRecommendation.CALL;
                reasoning = String.format(
                    "%s с эквити %.1f%% против пот-оддсов %.1f%% - можно коллировать.",
                    currentHand.getRank().getRussianName(),
                    odds.equity() * 100,
                    odds.potOdds() * 100
                );
            } else {
                action = ActionRecommendation.FOLD;
                reasoning = String.format(
                    "%s слабовата. Эквити %.1f%% < нужных %.1f%%. Лучше фолд.",
                    currentHand.getRank().getRussianName(),
                    odds.equity() * 100,
                    odds.requiredEquity() * 100
                );
            }
        }
        else if (odds.outs() > 0) {
            if (odds.shouldCall()) {
                action = toCall == 0 ? ActionRecommendation.CHECK : ActionRecommendation.CALL;
                reasoning = String.format(
                    "Дро: %d аутсов = %.1f%% шанс улучшиться. Пот-оддсы позволяют колл.",
                    odds.outs(),
                    odds.equity() * 100
                );
            } else {
                action = toCall == 0 ? ActionRecommendation.CHECK : ActionRecommendation.FOLD;
                reasoning = String.format(
                    "Дро: %d аутсов = %.1f%%, но пот-оддсы %.1f%% не позволяют. Чек/Фолд.",
                    odds.outs(),
                    odds.equity() * 100,
                    odds.potOdds() * 100
                );
            }
        }
        else {
            action = toCall == 0 ? ActionRecommendation.CHECK : ActionRecommendation.FOLD;
            reasoning = "Нет ничего - ни руки, ни дро. Не блефуй против новичков!";
        }

        List<String> tips = buildPostflopTips(currentHand, odds, state);

        int suggestedAmount = 0;
        if (action == ActionRecommendation.RAISE) {
            suggestedAmount = (int) (potSize * 0.66);
        } else if (action == ActionRecommendation.CALL) {
            suggestedAmount = toCall;
        }

        return new Recommendation(action, reasoning, tips, suggestedAmount);
    }

    private String buildPreflopReasoning(HoleCards cards, Position position,
                                          boolean facingRaise, int raiserCount,
                                          ActionRecommendation action) {
        StringBuilder sb = new StringBuilder();
        HandStrength strength = preflopChart.getHandStrength(cards);

        sb.append(preflopChart.getHandDescription(cards)).append("\n");
        sb.append("Позиция: ").append(position.getRussianName()).append("\n");

        if (facingRaise) {
            sb.append("Facing raise от ").append(raiserCount).append(" игрока(ов)\n");
        }

        sb.append("Рекомендация: ").append(action.getRussianName());

        return sb.toString();
    }

    private List<String> buildPreflopTips(HoleCards cards, Position position, GameState state) {
        List<String> tips = new ArrayList<>();
        HandStrength strength = preflopChart.getHandStrength(cards);

        tips.add(preflopChart.getPositionAdvice(position));

        if (strength == HandStrength.TRASH) {
            tips.add("Правило #1: Играй тайтово! Фолд мусора = экономия фишек.");
        }

        if (state.hasLimpers() && strength.getTier() >= HandStrength.PLAYABLE.getTier()) {
            tips.add("Правило #4: Бей лимперов рейзом! Они часто сфолдят или зайдут слабой рукой.");
        }

        return tips;
    }

    private List<String> buildPostflopTips(Hand hand, OddsResult odds, GameState state) {
        List<String> tips = new ArrayList<>();

        if (hand.getRank().getStrength() >= HandRank.PAIR.getStrength()) {
            tips.add("Правило #2: Вэлью-бет толще! Новички коллируют со слабым.");
        }

        if (hand.getRank().getStrength() < HandRank.PAIR.getStrength() && odds.outs() == 0) {
            tips.add("Правило #3: Не блефуй часто! Новички не умеют фолдить.");
        }

        if (odds.outs() > 0) {
            tips.add(String.format("Правило 4-2: %d аутов × %d = %.0f%% шанс улучшиться",
                odds.outs(),
                state.getStage() == GameStage.FLOP ? 4 : 2,
                odds.equity() * 100
            ));
        }

        return tips;
    }

    public record Recommendation(
        ActionRecommendation action,
        String reasoning,
        List<String> tips,
        int suggestedAmount
    ) {
        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Рекомендация ===\n");
            sb.append("Действие: ").append(action.getRussianName());
            if (suggestedAmount > 0) {
                sb.append(" (").append(suggestedAmount).append(")");
            }
            sb.append("\n\n");
            sb.append(reasoning).append("\n");
            if (!tips.isEmpty()) {
                sb.append("\nСоветы:\n");
                for (String tip : tips) {
                    sb.append("• ").append(tip).append("\n");
                }
            }
            return sb.toString();
        }
    }
}
