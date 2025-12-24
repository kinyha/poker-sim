package poker.analytics;

import poker.model.*;
import poker.strategy.RecommendationEngine;
import poker.odds.OddsCalculator;

import java.util.ArrayList;
import java.util.List;

public class DecisionAnalyzer {
    private final RecommendationEngine recommendationEngine;
    private final OddsCalculator oddsCalculator;

    public DecisionAnalyzer() {
        this.recommendationEngine = new RecommendationEngine();
        this.oddsCalculator = new OddsCalculator();
    }

    public HandAnalysis analyzeDecision(Player player, GameState state, Action playerAction) {
        RecommendationEngine.Recommendation recommendation = recommendationEngine.getRecommendation(player, state);
        OddsResult oddsResult = oddsCalculator.calculateOdds(player, state);

        boolean wasOptimal = recommendation.action().matchesAction(playerAction);

        String feedback = generateFeedback(playerAction, recommendation, oddsResult, state);
        List<String> tips = generateTips(playerAction, recommendation, state, player);

        return new HandAnalysis(
            state.getStage(),
            player.getHoleCards(),
            new ArrayList<>(state.getCommunityCards()),
            playerAction,
            recommendation.action(),
            wasOptimal,
            oddsResult,
            feedback,
            tips
        );
    }

    private String generateFeedback(Action actual, RecommendationEngine.Recommendation recommended,
                                    OddsResult odds, GameState state) {
        StringBuilder feedback = new StringBuilder();

        boolean isOptimal = recommended.action().matchesAction(actual);

        if (isOptimal) {
            feedback.append("Правильно! ");
        } else {
            feedback.append("Можно лучше: ");
            feedback.append(String.format(
                "Вы сделали %s, но лучше было %s. ",
                actual.toRussianString(),
                recommended.action().getRussianName()
            ));
        }

        feedback.append("\n").append(recommended.reasoning());

        return feedback.toString();
    }

    private List<String> generateTips(Action actual, RecommendationEngine.Recommendation rec,
                                       GameState state, Player player) {
        List<String> tips = new ArrayList<>(rec.tips());

        Position position = state.getPlayerPosition(player);
        if (position.isEarly() && actual.getType() != ActionType.FOLD) {
            if (rec.action() == ActionRecommendation.FOLD) {
                tips.add("С ранней позиции играй только премиум руки!");
            }
        }

        if (actual.isAggressive()) {
            int potSize = state.getPot().getTotal();
            if (actual.getAmount() > 0) {
                double betRatio = (double) actual.getAmount() / potSize;
                if (betRatio < 0.5) {
                    tips.add("Мелкие ставки (<50% пота) дают оппонентам хорошие оддсы для колла.");
                } else if (betRatio > 2.0) {
                    tips.add("Оверставка (>200% пота) часто выглядит как блеф или монстр.");
                }
            }
        }

        return tips;
    }

    public String getQuickVerdict(HandAnalysis analysis) {
        if (analysis.wasOptimal()) {
            return "Отлично!";
        }
        return "Неоптимально - " + analysis.recommendation().getRussianName() + " было бы лучше";
    }
}
