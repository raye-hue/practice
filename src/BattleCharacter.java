public class BattleCharacter {

    private final GameCharacter template;
    private int currentHp;
    private int currentMana;
    private int attackBonus;
    private int damageReductionPercent;

    public BattleCharacter(GameCharacter template) {
        this.template = template;
        this.currentHp = template.getMaxHp();
        this.currentMana = template.getMaxMana();
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
        return damageReductionPercent;
    }

    public boolean canUseMove(Move move) {
        return currentMana >= move.getManaCost();
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
        int reducedDamage = amount * (100 - damageReductionPercent) / 100;
        currentHp = Math.max(0, currentHp - Math.max(0, reducedDamage));
    }

    public void addAttackBonus(int amount) {
        attackBonus += amount;
    }

    public void setDamageReductionPercent(int value) {
        damageReductionPercent = Math.max(0, Math.min(100, value));
    }

    public boolean isDefeated() {
        return currentHp <= 0;
    }
}
