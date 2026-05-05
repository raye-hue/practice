package battle;

public class BattleCharacter {

    private final GameCharacter template;
    private int currentHp;
    private int currentMana;
    private int attackBonus;
    private int timedDamageReductionPercent;
    private int timedDamageReductionRemainingFrames;
    private int blockDamageReductionPercent;
    private int blockDamageReductionRemainingFrames;
    private int attackSkipCount;
    private int trappedDamage;
    private final int[] moveCooldownRemainingFrames;
    private int manaRegenFrameCounter;

    public BattleCharacter(GameCharacter template) {
        this.template = template;
        this.currentHp = template.getMaxHp();
        this.currentMana = template.getMaxMana();
        this.moveCooldownRemainingFrames = new int[Math.max(0, template.getMoves().length)];
    }

    public GameCharacter getTemplate() {
        return template;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getCurrentMana() {
        return currentMana;
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    public int getDamageReductionPercent() {
        return Math.max(timedDamageReductionPercent, blockDamageReductionPercent);
    }

    public int getMoveCooldownRemainingFrames(int index) {
        if (index < 0 || index >= moveCooldownRemainingFrames.length) {
            return 0;
        }
        return moveCooldownRemainingFrames[index];
    }

    public boolean canUseMove(int index) {
        Move move = template.getMove(index);
        if (move == null) {
            return false;
        }
        return currentMana >= move.getManaCost() && getMoveCooldownRemainingFrames(index) == 0;
    }

    public void spendMana(int amount) {
        currentMana = Math.max(0, currentMana - amount);
    }

    public void restoreMana(int amount) {
        currentMana = Math.min(template.getMaxMana(), currentMana + amount);
    }

    public void heal(int amount) {
        currentHp = Math.min(template.getMaxHp(), currentHp + amount);
    }

    public void takeDamage(int amount) {
        int reductionPercent = Math.max(timedDamageReductionPercent, blockDamageReductionPercent);
        int reducedDamage = amount * (100 - reductionPercent) / 100;
        currentHp = Math.max(0, currentHp - Math.max(0, reducedDamage));
    }

    public void knockOut() {
        currentHp = 0;
    }

    public void applyTimedDamageReduction(int percent, int durationFrames) {
        timedDamageReductionPercent = Math.max(0, Math.min(100, percent));
        timedDamageReductionRemainingFrames = Math.max(0, durationFrames);
    }

    public void applyBlockDamageReduction(int percent, int durationFrames) {
        blockDamageReductionPercent = Math.max(0, Math.min(100, percent));
        blockDamageReductionRemainingFrames = Math.max(0, durationFrames);
    }

    public void addAttackBonus(int amount) {
        attackBonus += amount;
    }

    public void setDamageReductionPercent(int value) {
        timedDamageReductionPercent = Math.max(0, Math.min(100, value));
        timedDamageReductionRemainingFrames = 0;
    }

    public void addAttackSkipCount(int amount) {
        attackSkipCount = Math.max(0, attackSkipCount + amount);
    }

    public boolean consumeAttackSkip() {
        if (attackSkipCount <= 0) {
            return false;
        }
        attackSkipCount--;
        return true;
    }

    public void armTrap(int damage) {
        trappedDamage = Math.max(trappedDamage, damage);
    }

    public int consumeTrapDamage() {
        int damage = trappedDamage;
        trappedDamage = 0;
        return damage;
    }

    public boolean hasTrapArmed() {
        return trappedDamage > 0;
    }

    public void startMoveCooldown(int index) {
        Move move = template.getMove(index);
        if (move == null || index < 0 || index >= moveCooldownRemainingFrames.length) {
            return;
        }
        startMoveCooldown(index, move.getCooldownFrames());
    }

    public void startMoveCooldown(int index, int cooldownFrames) {
        if (index < 0 || index >= moveCooldownRemainingFrames.length) {
            return;
        }
        moveCooldownRemainingFrames[index] = Math.max(0, cooldownFrames);
    }

    public void tickStatusEffects() {
        if (timedDamageReductionRemainingFrames > 0 && --timedDamageReductionRemainingFrames == 0) {
            timedDamageReductionPercent = 0;
        }
        if (blockDamageReductionRemainingFrames > 0 && --blockDamageReductionRemainingFrames == 0) {
            blockDamageReductionPercent = 0;
        }

        if (currentMana < template.getMaxMana()) {
            manaRegenFrameCounter++;
            if (manaRegenFrameCounter >= 60) {
                restoreMana(1);
                manaRegenFrameCounter = 0;
            }
        } else {
            manaRegenFrameCounter = 0;
        }

        for (int i = 0; i < moveCooldownRemainingFrames.length; i++) {
            if (moveCooldownRemainingFrames[i] > 0) {
                moveCooldownRemainingFrames[i]--;
            }
        }
    }

    public boolean isDefeated() {
        return currentHp <= 0;
    }
}

