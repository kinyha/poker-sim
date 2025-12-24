package poker.model;

import poker.ai.AIStrategy;

public class AIPlayer extends Player {
    private final AIStrategy strategy;

    public AIPlayer(String name, int startingChips, AIStrategy strategy) {
        super(name, startingChips);
        this.strategy = strategy;
    }

    @Override
    public Action decideAction(GameState gameState) {
        return strategy.decide(this, gameState);
    }

    public String getPlayerType() {
        return strategy.getPlayerTypeName();
    }

    public AIStrategy getStrategy() {
        return strategy;
    }
}
