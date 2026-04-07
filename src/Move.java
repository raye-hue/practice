public class Move {

    private final String name;
    private final int minValue;
    private final int maxValue;
    private final int manaCost;
    private final MoveEffect effect;
    private final String description;

    public Move(String name, int minValue, int maxValue, int manaCost, MoveEffect effect, String description) {
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.manaCost = manaCost;
        this.effect = effect;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getManaCost() {
        return manaCost;
    }

    public MoveEffect getEffect() {
        return effect;
    }

    public String getDescription() {
        return description;
    }
}
