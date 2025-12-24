package poker.model;

public class Pot {
    private int total;

    public Pot() {
        this.total = 0;
    }

    public Pot(int initial) {
        this.total = initial;
    }

    public void add(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot add negative amount to pot");
        }
        this.total += amount;
    }

    public int getTotal() {
        return total;
    }

    public void reset() {
        this.total = 0;
    }

    public int takeAll() {
        int amount = total;
        total = 0;
        return amount;
    }

    @Override
    public String toString() {
        return "$" + total;
    }
}
