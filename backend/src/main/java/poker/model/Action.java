package poker.model;

import java.util.Objects;

public class Action {
    private final ActionType type;
    private final int amount;

    private Action(ActionType type, int amount) {
        this.type = Objects.requireNonNull(type);
        this.amount = amount;
    }

    public static Action fold() {
        return new Action(ActionType.FOLD, 0);
    }

    public static Action check() {
        return new Action(ActionType.CHECK, 0);
    }

    public static Action call(int amount) {
        return new Action(ActionType.CALL, amount);
    }

    public static Action bet(int amount) {
        return new Action(ActionType.BET, amount);
    }

    public static Action raise(int amount) {
        return new Action(ActionType.RAISE, amount);
    }

    public static Action allIn(int amount) {
        return new Action(ActionType.ALL_IN, amount);
    }

    public ActionType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isAggressive() {
        return type == ActionType.BET || type == ActionType.RAISE || type == ActionType.ALL_IN;
    }

    public boolean isPassive() {
        return type == ActionType.CHECK || type == ActionType.CALL;
    }

    public boolean isFold() {
        return type == ActionType.FOLD;
    }

    @Override
    public String toString() {
        if (amount > 0) {
            return type.getDisplayName() + " " + amount;
        }
        return type.getDisplayName();
    }

    public String toRussianString() {
        if (amount > 0) {
            return type.getRussianName() + " " + amount;
        }
        return type.getRussianName();
    }
}
