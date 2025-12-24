package poker.web;

import poker.engine.GameEngine;
import poker.model.*;
import poker.ai.*;
import poker.strategy.RecommendationEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class GameSession {
    private final String sessionId;
    private final GameEngine engine;
    private final List<Player> players;
    private final HumanPlayer humanPlayer;
    private final RecommendationEngine recommendationEngine;
    private final BlockingQueue<Action> humanActionQueue = new LinkedBlockingQueue<>();
    private volatile boolean gameRunning = false;
    private volatile boolean handComplete = false;
    private volatile String lastResultMessage = null;
    private Thread gameThread;

    public GameSession(String sessionId, int playerCount, int humanPosition, int startingChips, int smallBlind, int bigBlind, String aiType) {
        this.sessionId = sessionId;
        this.players = new ArrayList<>();
        this.recommendationEngine = new RecommendationEngine();

        HumanPlayer human = null;
        for (int i = 0; i < playerCount; i++) {
            if (i == humanPosition) {
                human = new HumanPlayer("You", startingChips);
                players.add(human);
            } else {
                AIStrategy strategy = createAIStrategy(aiType, i);
                players.add(new AIPlayer("Bot" + (i + 1), startingChips, strategy));
            }
        }
        this.humanPlayer = human;

        this.engine = new GameEngine(players, smallBlind, bigBlind);
        this.engine.setHumanActionProvider((state, player) -> {
            try {
                return humanActionQueue.poll(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Action.fold();
            }
        });
    }

    private AIStrategy createAIStrategy(String aiType, int index) {
        if (aiType == null) aiType = "mixed";
        return switch (aiType) {
            case "calling_station" -> new CallingStationAI();
            case "tight_passive" -> new TightPassiveAI();
            case "loose_aggressive" -> new LooseAggressiveAI();
            default -> switch (index % 3) {
                case 0 -> new CallingStationAI();
                case 1 -> new TightPassiveAI();
                default -> new LooseAggressiveAI();
            };
        };
    }

    public void startHand() {
        if (gameRunning) return;

        // Check if human player has 0 chips
        if (humanPlayer.getChips() <= 0) {
            handComplete = true;
            lastResultMessage = "Игра окончена! У вас закончились фишки.";
            return;
        }

        gameRunning = true;
        handComplete = false;
        lastResultMessage = null;
        humanActionQueue.clear();

        gameThread = new Thread(() -> {
            try {
                GameEngine.HandResult result = engine.playHand();
                if (result != null && !result.winners().isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    if (result.isSplit()) {
                        sb.append("Split pot!");
                    } else {
                        Player winner = result.winners().get(0);
                        int amount = result.winnings().get(winner);
                        sb.append(winner.getName()).append(" выигрывает ").append(amount).append(" фишек!");
                    }
                    if (result.winningHand() != null) {
                        sb.append(" (").append(result.winningHand().getDescription()).append(")");
                    }
                    lastResultMessage = sb.toString();
                }
            } finally {
                handComplete = true;
                gameRunning = false;
            }
        });
        gameThread.start();
    }

    public void submitAction(Action action) {
        humanActionQueue.offer(action);
    }

    public GameState getCurrentState() {
        return engine.getCurrentState();
    }

    public HumanPlayer getHumanPlayer() {
        return humanPlayer;
    }

    public String getRecommendation() {
        if (humanPlayer == null || handComplete) return null;
        GameState state = getCurrentState();
        if (state == null) return null;
        if (state.getCurrentPlayer() != humanPlayer) return null;

        try {
            var rec = recommendationEngine.getRecommendation(humanPlayer, state);
            return rec.action().getRussianName() + ": " + rec.reasoning(); //???
        } catch (Exception e) {
            return null;
        }
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public boolean isHandComplete() {
        return handComplete;
    }

    public String getLastResultMessage() {
        return lastResultMessage;
    }

    public String getSessionId() {
        return sessionId;
    }
}
