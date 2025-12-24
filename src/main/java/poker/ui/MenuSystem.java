package poker.ui;

import poker.ai.*;
import poker.model.Position;

import java.util.*;

public class MenuSystem {
    private final Scanner scanner;

    public MenuSystem(Scanner scanner) {
        this.scanner = scanner;
    }

    public GameConfig getGameConfig() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║              НАСТРОЙКА ИГРЫ                               ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        int playerCount = getPlayerCount();
        int position = getPlayerPosition(playerCount);
        List<AIStrategy> aiStrategies = getAIStrategies(playerCount - 1);
        int startingChips = getStartingChips();
        int bigBlind = getBigBlind();

        return new GameConfig(
            playerCount,
            position,
            aiStrategies,
            startingChips,
            bigBlind / 2,
            bigBlind
        );
    }

    private int getPlayerCount() {
        System.out.println("Количество игроков за столом:");
        System.out.println("  2 - Heads-up (1 на 1)");
        System.out.println("  6 - Короткий стол");
        System.out.println("  9 - Полный стол");
        System.out.print("Выбор (2-9) [6]: ");

        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return 6;
        }
        try {
            int count = Integer.parseInt(input);
            return Math.max(2, Math.min(9, count));
        } catch (NumberFormatException e) {
            return 6;
        }
    }

    private int getPlayerPosition(int playerCount) {
        System.out.println("\nВыберите вашу позицию:");
        List<Position> positions = Position.getPositionsForPlayerCount(playerCount);

        for (int i = 0; i < positions.size(); i++) {
            Position pos = positions.get(i);
            String marker = "";
            if (pos == Position.BUTTON) {
                marker = " (Лучшая!)";
            } else if (pos.isEarly()) {
                marker = " (Сложная)";
            }
            System.out.printf("  %d. %s - %s%s%n",
                i + 1, pos.getAbbreviation(), pos.getRussianName(), marker);
        }

        System.out.print("Выбор [" + positions.size() + "]: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return positions.size() - 1;
        }

        try {
            int choice = Integer.parseInt(input);
            return Math.max(0, Math.min(playerCount - 1, choice - 1));
        } catch (NumberFormatException e) {
            return positions.size() - 1;
        }
    }

    private List<AIStrategy> getAIStrategies(int count) {
        System.out.println("\nТип AI оппонентов:");
        System.out.println("  1. Calling Stations - коллируют всё (самый простой)");
        System.out.println("  2. Смешанный - разные типы новичков");
        System.out.println("  3. С маньяками - включает агрессивных игроков");
        System.out.print("Выбор [1]: ");

        String input = scanner.nextLine().trim();
        int choice = 1;
        if (!input.isEmpty()) {
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                choice = 1;
            }
        }

        List<AIStrategy> strategies = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            AIStrategy strategy = switch (choice) {
                case 1 -> new CallingStationAI();
                case 2 -> random.nextBoolean()
                    ? new CallingStationAI()
                    : new TightPassiveAI();
                case 3 -> switch (random.nextInt(3)) {
                    case 0 -> new CallingStationAI();
                    case 1 -> new TightPassiveAI();
                    default -> new LooseAggressiveAI();
                };
                default -> new CallingStationAI();
            };
            strategies.add(strategy);
        }

        return strategies;
    }

    private int getStartingChips() {
        System.out.print("\nНачальные фишки [1000]: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return 1000;
        }

        try {
            return Math.max(100, Integer.parseInt(input));
        } catch (NumberFormatException e) {
            return 1000;
        }
    }

    private int getBigBlind() {
        System.out.print("Большой блайнд [20]: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return 20;
        }

        try {
            return Math.max(2, Integer.parseInt(input));
        } catch (NumberFormatException e) {
            return 20;
        }
    }

    public boolean askContinue() {
        System.out.print("\nСыграть ещё руку? (Y/N) [Y]: ");
        String input = scanner.nextLine().trim().toUpperCase();
        return input.isEmpty() || input.startsWith("Y");
    }

    public void displayWelcome() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                                                          ║");
        System.out.println("║        ПОКЕРНЫЙ ТРЕНАЖЁР - TEXAS HOLD'EM                 ║");
        System.out.println("║                                                          ║");
        System.out.println("║  Практикуй покер против AI-новичков!                     ║");
        System.out.println("║  Получай подсказки и анализ решений.                     ║");
        System.out.println("║                                                          ║");
        System.out.println("║  Команды: F=Фолд, C=Колл/Чек, B=Бет, R=Рейз, H=Помощь    ║");
        System.out.println("║                                                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.print("Нажмите Enter для начала...");
        scanner.nextLine();
    }

    public record GameConfig(
        int playerCount,
        int humanPosition,
        List<AIStrategy> aiStrategies,
        int startingChips,
        int smallBlind,
        int bigBlind
    ) {}
}
