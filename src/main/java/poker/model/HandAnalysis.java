package poker.model;

import java.util.List;

public record HandAnalysis(
    GameStage stage,
    HoleCards holeCards,
    List<Card> communityCards,
    Action playerAction,
    ActionRecommendation recommendation,
    boolean wasOptimal,
    OddsResult oddsResult,
    String feedback,
    List<String> tips
) {
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Стадия: ").append(stage.getRussianName()).append("\n");
        sb.append("Ваши карты: ").append(holeCards).append(" (").append(holeCards.getNotation()).append(")\n");
        if (!communityCards.isEmpty()) {
            sb.append("Борд: ");
            for (Card card : communityCards) {
                sb.append("[").append(card).append("] ");
            }
            sb.append("\n");
        }
        sb.append("Ваше действие: ").append(playerAction.toRussianString()).append("\n");
        sb.append("Рекомендация: ").append(recommendation.getRussianName()).append("\n");
        sb.append("Оценка: ").append(wasOptimal ? "Правильно!" : "Можно лучше").append("\n");
        if (oddsResult != null) {
            sb.append(oddsResult.getSummary());
        }
        return sb.toString();
    }
}
