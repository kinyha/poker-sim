package poker.ui;

import poker.model.*;
import poker.strategy.RecommendationEngine;

import java.util.Scanner;

public class InputHandler {
    private final Scanner scanner;
    private final RecommendationEngine recommendationEngine;

    public InputHandler(Scanner scanner) {
        this.scanner = scanner;
        this.recommendationEngine = new RecommendationEngine();
    }

    public Action readAction(GameState state, Player player) {
        int toCall = state.getAmountToCall(player);
        boolean canCheck = toCall == 0;

        displayAvailableActions(state, player, canCheck, toCall);

        while (true) {
            System.out.print("\nВаш ход: ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.isEmpty()) {
                continue;
            }

            Action action = parseAction(input, state, player, canCheck, toCall);
            if (action != null) {
                return action;
            }

            System.out.println("Неверная команда. Попробуйте ещё раз.");
        }
    }

    private void displayAvailableActions(GameState state, Player player, boolean canCheck, int toCall) {
        System.out.println("\nДоступные действия:");

        if (canCheck) {
            System.out.println("  [C] Чек - пропустить ход");
            System.out.println("  [B <сумма>] Бет - сделать ставку");
        } else {
            System.out.println("  [F] Фолд - сбросить карты");
            System.out.println("  [C] Колл - уравнять ставку (" + toCall + ")");
            System.out.println("  [R <сумма>] Рейз - повысить до суммы");
        }

        System.out.println("  [A] Олл-ин - поставить всё (" + player.getChips() + ")");
        System.out.println("  [H] Подсказка - показать рекомендацию");
    }

    private Action parseAction(String input, GameState state, Player player,
                               boolean canCheck, int toCall) {
        String[] parts = input.split("\\s+");
        String command = parts[0];

        switch (command) {
            case "F":
                if (canCheck) {
                    System.out.println("Можно чекнуть бесплатно, зачем фолд?");
                    return null;
                }
                return Action.fold();

            case "C":
                if (canCheck) {
                    return Action.check();
                }
                return Action.call(toCall);

            case "B":
                if (!canCheck) {
                    System.out.println("Нельзя бетить, когда есть ставка. Используйте R для рейза.");
                    return null;
                }
                if (parts.length < 2) {
                    System.out.println("Укажите размер ставки: B <сумма>");
                    return null;
                }
                try {
                    int betAmount = Integer.parseInt(parts[1]);
                    if (betAmount < state.getBigBlind()) {
                        System.out.println("Минимальная ставка: " + state.getBigBlind());
                        return null;
                    }
                    if (betAmount > player.getChips()) {
                        System.out.println("Недостаточно фишек. У вас: " + player.getChips());
                        return null;
                    }
                    return Action.bet(betAmount);
                } catch (NumberFormatException e) {
                    System.out.println("Неверный формат суммы");
                    return null;
                }

            case "R":
                if (canCheck) {
                    System.out.println("Нет ставки для рейза. Используйте B для бета.");
                    return null;
                }
                if (parts.length < 2) {
                    System.out.println("Укажите сумму рейза: R <сумма>");
                    return null;
                }
                try {
                    int raiseAmount = Integer.parseInt(parts[1]);
                    int minRaise = state.getCurrentBet() + state.getBigBlind();
                    if (raiseAmount < minRaise) {
                        System.out.println("Минимальный рейз: " + minRaise);
                        return null;
                    }
                    if (raiseAmount > player.getChips() + player.getCurrentBet()) {
                        System.out.println("Недостаточно фишек для такого рейза");
                        return null;
                    }
                    return Action.raise(raiseAmount);
                } catch (NumberFormatException e) {
                    System.out.println("Неверный формат суммы");
                    return null;
                }

            case "A":
                return Action.allIn(player.getChips());

            case "H":
                showHelp(player, state);
                return null;

            default:
                return null;
        }
    }

    private void showHelp(Player player, GameState state) {
        RecommendationEngine.Recommendation rec = recommendationEngine.getRecommendation(player, state);
        System.out.println();
        System.out.println(rec.getSummary());
    }

    public boolean askYesNo(String question) {
        System.out.print(question + " (Y/N): ");
        String input = scanner.nextLine().trim().toUpperCase();
        return input.startsWith("Y") || input.isEmpty();
    }

    public int readInt(String prompt, int defaultValue, int min, int max) {
        System.out.print(prompt + " [" + defaultValue + "]: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return defaultValue;
        }

        try {
            int value = Integer.parseInt(input);
            return Math.max(min, Math.min(max, value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public int readChoice(String prompt, int numChoices) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        try {
            int choice = Integer.parseInt(input);
            if (choice >= 1 && choice <= numChoices) {
                return choice;
            }
        } catch (NumberFormatException e) {
        }

        return 1;
    }
}
