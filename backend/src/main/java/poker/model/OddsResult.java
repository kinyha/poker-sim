package poker.model;

public record OddsResult(
    int outs,
    double equity,
    double potOdds,
    double requiredEquity
) {
    public boolean shouldCall() {
        return equity > requiredEquity;
    }

    public boolean isProfitableCall() {
        return equity > potOdds;
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        if (outs > 0) {
            sb.append("Аутсов: ").append(outs).append("\n");
        }
        sb.append(String.format("Эквити: %.1f%%\n", equity * 100));
        if (potOdds > 0) {
            sb.append(String.format("Пот-оддсы: %.1f%%\n", potOdds * 100));
            sb.append(String.format("Нужное эквити для колла: %.1f%%\n", requiredEquity * 100));
            if (shouldCall()) {
                sb.append("Вывод: КОЛЛ выгоден (+EV)\n");
            } else {
                sb.append("Вывод: ФОЛД лучше (-EV)\n");
            }
        }
        return sb.toString();
    }

    public static OddsResult empty() {
        return new OddsResult(0, 0.0, 0.0, 0.0);
    }
}
