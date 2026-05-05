package battle;

public class Move {

    private final String name;
    private final int minValue;
    private final int maxValue;
    private final int manaCost;
    private final MoveEffect effect;
    private final String description;
    private final int cooldownFrames;
    private final String soundEffectPath;

    public Move(String name, int minValue, int maxValue, int manaCost, MoveEffect effect, String description) {
        this(name, minValue, maxValue, manaCost, effect, description, 0, null);
    }

    public Move(String name, int minValue, int maxValue, int manaCost, MoveEffect effect, String description, int cooldownFrames) {
        this(name, minValue, maxValue, manaCost, effect, description, cooldownFrames, null);
    }

    public Move(String name, int minValue, int maxValue, int manaCost, MoveEffect effect, String description, int cooldownFrames, String soundEffectPath) {
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.manaCost = manaCost;
        this.effect = effect;
        this.description = description;
        this.cooldownFrames = Math.max(0, cooldownFrames);
        this.soundEffectPath = soundEffectPath;
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

    public int getCooldownFrames() {
        return cooldownFrames;
    }

    public String getSoundEffectPath() {
        return soundEffectPath;
    }
}

