package poker.ui;

import poker.ai.AIStrategy;
import poker.analytics.*;
import poker.engine.GameEngine;
import poker.model.*;

import java.util.*;

public class ConsoleUI {
    private final Scanner scanner;
    private final TableRenderer renderer;
    private final InputHandler input;
    private final MenuSystem menu;

    private GameEngine engine;
    private SessionStats stats;
    private HumanPlayer humanPlayer;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.renderer = new TableRenderer();
        this.input = new InputHandler(scanner);
        this.menu = new MenuSystem(scanner);
    }

    public void run() {
        menu.displayWelcome();

        MenuSystem.GameConfig config = menu.getGameConfig();
        setupGame(config);

        boolean playing = true;
        int handNumber = 0;

        while (playing && humanPlayer.getChips() > 0) {
            handNumber++;
            System.out.println("\n" + "=".repeat(60));
            System.out.println("                    РАЗДАЧА #" + handNumber);
            System.out.println("=".repeat(60));

            GameEngine.HandResult result = engine.playHand();

            recordHandResult(result);

            if (humanPlayer.getChips() <= 0) {
                System.out.println("\n\nВы проиграли все фишки! Игра окончена.");
                break;
            }

            playing = menu.askContinue();
        }

        displayFinalStats();
    }

    private void setupGame(MenuSystem.GameConfig config) {
        List<Player> players = new ArrayList<>();

        int aiIndex = 0;
        for (int i = 0; i < config.playerCount(); i++) {
            if (i == config.humanPosition()) {
                humanPlayer = new HumanPlayer("ВЫ", config.startingChips());
                humanPlayer.setSeatIndex(i);
                players.add(humanPlayer);
            } else {
                AIStrategy strategy = config.aiStrategies().get(aiIndex);
                String name = "Bot" + (aiIndex + 1) + " (" + getShortTypeName(strategy) + ")";
                AIPlayer aiPlayer = new AIPlayer(name, config.startingChips(), strategy);
                aiPlayer.setSeatIndex(i);
                players.add(aiPlayer);
                aiIndex++;
            }
        }

        stats = new SessionStats(config.startingChips());

        engine = new GameEngine(players, config.smallBlind(), config.bigBlind());

        engine.setHumanActionProvider((state, player) -> {
            renderer.renderTable(state, humanPlayer);
            return input.readAction(state, player);
        });

        engine.setStateUpdateListener(state -> {
        });

        engine.setMessageListener(message -> {
            renderer.renderMessage(message);
        });

        engine.setAnalysisListener(analysis -> {
            stats.recordDecision(analysis);
            renderer.renderAnalysis(analysis);
        });
    }

    private String getShortTypeName(AIStrategy strategy) {
        String name = strategy.getPlayerTypeName();
        if (name.contains("Calling")) return "CS";
        if (name.contains("Tight")) return "TP";
        if (name.contains("Loose")) return "LAG";
        return "AI";
    }

    private void recordHandResult(GameEngine.HandResult result) {
        if (humanPlayer.getHoleCards() == null) {
            return;
        }

        boolean won = result.winners().contains(humanPlayer);
        int chipsDelta = 0;

        if (won) {
            chipsDelta = result.winnings().getOrDefault(humanPlayer, 0);
        }

        stats.recordHandResult(humanPlayer.getHoleCards(), chipsDelta, won);
        stats.updateChips(humanPlayer.getChips());
    }

    private void displayFinalStats() {
        System.out.println(stats.getSessionSummary());

        System.out.println("\nСпасибо за игру! До новых встреч за столом.");
    }

    public static void main(String[] args) {
        new ConsoleUI().run();
    }
}
