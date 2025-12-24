package poker.analytics;

import poker.model.*;

import java.util.*;

public class SessionStats {
    private int handsPlayed;
    private int handsWon;
    private int startingChips;
    private int currentChips;
    private int optimalDecisions;
    private int totalDecisions;
    private final Map<String, Integer> handResults;
    private final Map<String, Integer> handsPlayedByNotation;
    private final List<HandAnalysis> handHistory;

    public SessionStats(int startingChips) {
        this.startingChips = startingChips;
        this.currentChips = startingChips;
        this.handsPlayed = 0;
        this.handsWon = 0;
        this.optimalDecisions = 0;
        this.totalDecisions = 0;
        this.handResults = new HashMap<>();
        this.handsPlayedByNotation = new HashMap<>();
        this.handHistory = new ArrayList<>();
    }

    public void recordDecision(HandAnalysis analysis) {
        totalDecisions++;
        if (analysis.wasOptimal()) {
            optimalDecisions++;
        }
        handHistory.add(analysis);
    }

    public void recordHandResult(HoleCards cards, int chipsDelta, boolean won) {
        handsPlayed++;
        if (won) handsWon++;
        currentChips += chipsDelta;

        String notation = cards.getNotation();
        handResults.merge(notation, chipsDelta, Integer::sum);
        handsPlayedByNotation.merge(notation, 1, Integer::sum);
    }

    public void updateChips(int chips) {
        this.currentChips = chips;
    }

    public double getWinRate() {
        return handsPlayed > 0 ? (double) handsWon / handsPlayed : 0;
    }

    public double getOptimalPlayRate() {
        return totalDecisions > 0 ? (double) optimalDecisions / totalDecisions : 0;
    }

    public int getChipsDelta() {
        return currentChips - startingChips;
    }

    public String getSessionSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Итоги сессии ===\n\n");

        sb.append(String.format("Сыграно рук: %d\n", handsPlayed));
        sb.append(String.format("Выиграно: %d (%.1f%%)\n", handsWon, getWinRate() * 100));
        sb.append(String.format("Фишки: %d (%+d)\n", currentChips, getChipsDelta()));
        sb.append(String.format("Оптимальных решений: %.1f%%\n", getOptimalPlayRate() * 100));

        sb.append("\n--- Лучшие руки ---\n");
        sb.append(getTopHands(3, true));

        sb.append("\n--- Худшие руки ---\n");
        sb.append(getTopHands(3, false));

        sb.append("\n--- Советы ---\n");
        sb.append(getImproventSuggestions());

        return sb.toString();
    }

    private String getTopHands(int count, boolean best) {
        StringBuilder sb = new StringBuilder();

        List<Map.Entry<String, Integer>> sorted = handResults.entrySet().stream()
            .sorted((a, b) -> best
                ? Integer.compare(b.getValue(), a.getValue())
                : Integer.compare(a.getValue(), b.getValue()))
            .limit(count)
            .toList();

        for (Map.Entry<String, Integer> entry : sorted) {
            int timesPlayed = handsPlayedByNotation.getOrDefault(entry.getKey(), 1);
            sb.append(String.format("  %s: %+d (%d раз)\n",
                entry.getKey(), entry.getValue(), timesPlayed));
        }

        if (sorted.isEmpty()) {
            sb.append("  (пока нет данных)\n");
        }

        return sb.toString();
    }

    private String getImproventSuggestions() {
        StringBuilder sb = new StringBuilder();

        if (getOptimalPlayRate() < 0.6) {
            sb.append("  • Изучи чарт стартовых рук - слишком много неоптимальных решений\n");
        }

        if (getOptimalPlayRate() >= 0.8) {
            sb.append("  • Отличная игра! Продолжай в том же духе\n");
        }

        long trashLosses = handResults.entrySet().stream()
            .filter(e -> e.getKey().contains("o") &&
                        !e.getKey().startsWith("A") &&
                        !e.getKey().startsWith("K"))
            .mapToInt(Map.Entry::getValue)
            .filter(v -> v < 0)
            .sum();

        if (trashLosses < -100) {
            sb.append("  • Фолди мусорные руки раньше - они стоят тебе фишек\n");
        }

        if (sb.length() == 0) {
            sb.append("  • Продолжай играть для сбора статистики\n");
        }

        return sb.toString();
    }

    public int getHandsPlayed() {
        return handsPlayed;
    }

    public int getHandsWon() {
        return handsWon;
    }

    public int getTotalDecisions() {
        return totalDecisions;
    }

    public int getOptimalDecisions() {
        return optimalDecisions;
    }

    public List<HandAnalysis> getHandHistory() {
        return new ArrayList<>(handHistory);
    }
}
