import java.awt.Color;

public class GameCharacter {

    private final String name;
    private final String role;
    private final int maxHp;
    private final int maxMana;
    private final int baseAttack;
    private final int baseDefense;
    private final Color color;
    private final Move[] moves;
    private final String bio;

    public GameCharacter(
            String name,
            String role,
            int maxHp,
            int maxMana,
            int baseAttack,
            int baseDefense,
            Color color,
            Move[] moves,
            String bio

    ) {
        this.name = name;
        this.role = role;
        this.maxHp = maxHp;
        this.maxMana = maxMana;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.color = color;
        this.moves = moves;
        this.bio = bio;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public int getBaseDefense() {
        return baseDefense;
    }

    public Color getColor() {
        return color;
    }

    public Move[] getMoves() {
        return moves;
    }
    
    public String getBio(){
        return bio;
    }
}
