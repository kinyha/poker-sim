package poker.ui;

import poker.model.*;

import java.util.List;

public class TableRenderer {
    private static final String BORDER = "=".repeat(60);
    private static final String HIDDEN_CARDS = "[??][??]";

    public void renderTable(GameState state, Player humanPlayer) {
        System.out.println();
        System.out.println(BORDER);

        renderPlayers(state, humanPlayer);
        renderCommunityCards(state);
        renderPot(state);
        renderHumanHand(humanPlayer, state);

        System.out.println(BORDER);
    }

    private void renderPlayers(GameState state, Player humanPlayer) {
        List<Player> players = state.getPlayers();

        System.out.println();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            renderPlayer(p, i, state, humanPlayer);
        }
    }

    private void renderPlayer(Player player, int index, GameState state, Player humanPlayer) {
        StringBuilder sb = new StringBuilder();

        sb.append("  ");

        String posMarker = getPositionMarker(player, state);
        sb.append(String.format("%-6s", posMarker));

        String name = player.getName();
        if (name.length() > 12) {
            name = name.substring(0, 12);
        }
        sb.append(String.format("%-13s", name));

        if (player.isFolded()) {
            sb.append(String.format("%-10s", "FOLDED"));
        } else if (player.isAllIn()) {
            sb.append(String.format("%-10s", "ALL-IN"));
        } else {
            sb.append(String.format("$%-9d", player.getChips()));
        }

        if (!player.isFolded()) {
            String cards;
            if (player == humanPlayer) {
                cards = renderCards(player.getHoleCards());
            } else if (state.isShowdown()) {
                cards = renderCards(player.getHoleCards());
            } else {
                cards = HIDDEN_CARDS;
            }
            sb.append(cards);
        }

        if (player.getCurrentBet() > 0) {
            sb.append("  (bet: ").append(player.getCurrentBet()).append(")");
        }

        if (state.getActivePlayerIndex() == player.getSeatIndex() && player.canAct()) {
            sb.append(" <-- ВАШ ХОД");
        }

        System.out.println(sb);
    }

    private String getPositionMarker(Player player, GameState state) {
        Position pos = player.getPosition();
        if (pos == null) return "";

        return "[" + pos.getAbbreviation() + "]";
    }

    private void renderCommunityCards(GameState state) {
        List<Card> community = state.getCommunityCards();
        System.out.println();

        System.out.print("  Board: ");
        if (community.isEmpty()) {
            System.out.println("[Префлоп - карты ещё не открыты]");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Card card : community) {
                sb.append("[").append(card).append("] ");
            }
            System.out.println(sb.toString().trim());
        }
        System.out.println();
    }

    private void renderPot(GameState state) {
        System.out.printf("  Pot: $%d   |   Текущая ставка: $%d   |   Стадия: %s%n",
            state.getPot().getTotal(),
            state.getCurrentBet(),
            state.getStage().getRussianName()
        );
    }

    private void renderHumanHand(Player human, GameState state) {
        if (human == null || human.getHoleCards() == null) {
            return;
        }

        System.out.println();
        System.out.println("  ╔════════════════════════════════════════╗");
        System.out.println("  ║  ВАШИ КАРТЫ: " + renderCards(human.getHoleCards()) +
            " - " + human.getHoleCards().getNotation() + padRight("", 15) + "║");
        System.out.println("  ╚════════════════════════════════════════╝");
    }

    private String renderCards(HoleCards holeCards) {
        return "[" + holeCards.getCard1() + "][" + holeCards.getCard2() + "]";
    }

    private String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    public void renderMessage(String message) {
        System.out.println("\n" + message);
    }

    public void renderAnalysis(poker.model.HandAnalysis analysis) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                    АНАЛИЗ РЕШЕНИЯ                        ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");

        String verdict = analysis.wasOptimal() ? "Правильно!" : "Неоптимально";
        System.out.println("║  Оценка: " + verdict + padRight("", 48 - verdict.length()) + "║");

        String rec = "Рекомендация: " + analysis.recommendation().getRussianName();
        System.out.println("║  " + rec + padRight("", 56 - rec.length()) + "║");

        if (analysis.oddsResult() != null && analysis.oddsResult().outs() > 0) {
            String outs = "Аутсов: " + analysis.oddsResult().outs() +
                ", Эквити: " + String.format("%.0f%%", analysis.oddsResult().equity() * 100);
            System.out.println("║  " + outs + padRight("", 56 - outs.length()) + "║");
        }

        System.out.println("╚══════════════════════════════════════════════════════════╝");

        for (String tip : analysis.tips()) {
            if (tip.length() > 55) {
                System.out.println("  Совет: " + tip.substring(0, 52) + "...");
            } else {
                System.out.println("  Совет: " + tip);
            }
        }
    }

    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
