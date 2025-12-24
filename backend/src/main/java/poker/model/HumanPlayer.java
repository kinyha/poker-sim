package poker.model;

import java.util.ArrayList;
import java.util.List;

public class HumanPlayer extends Player {
    private Action pendingAction;
    private final List<HandAnalysis> analysisHistory;

    public HumanPlayer(String name, int startingChips) {
        super(name, startingChips);
        this.analysisHistory = new ArrayList<>();
    }

    @Override
    public Action decideAction(GameState gameState) {
        if (pendingAction != null) {
            Action action = pendingAction;
            pendingAction = null;
            return action;
        }
        throw new IllegalStateException("No action set for human player");
    }

    public void setAction(Action action) {
        this.pendingAction = action;
    }

    public void recordAnalysis(HandAnalysis analysis) {
        analysisHistory.add(analysis);
    }

    public List<HandAnalysis> getAnalysisHistory() {
        return new ArrayList<>(analysisHistory);
    }

    public HandAnalysis getLastAnalysis() {
        if (analysisHistory.isEmpty()) {
            return null;
        }
        return analysisHistory.get(analysisHistory.size() - 1);
    }

    public void clearAnalysisHistory() {
        analysisHistory.clear();
    }
}
