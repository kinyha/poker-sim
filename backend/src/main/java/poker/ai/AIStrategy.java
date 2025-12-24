package poker.ai;

import poker.model.*;

public interface AIStrategy {
    Action decide(Player player, GameState state);
    String getPlayerTypeName();
    String getPlayerTypeDescription();
}
