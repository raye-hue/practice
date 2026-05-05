package battle;

import java.awt.Color;

public class GameCharacter implements CharacterTemplate {

    private final String name;
    private final String role;
    private final int maxHp;
    private final int maxMana;
    private final int baseAttack;
    private final int baseDefense;
    private final Color color;
    private final Move[] moves;
    private final String bio;

    public GameCharacter(String name, String role, int maxHp, int maxMana, int baseAttack, int baseDefense, Color color, Move[] moves, String bio) {
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getRole() {
        return role;
    }

    @Override
    public int getMaxHp() {
        return maxHp;
    }

    @Override
    public int getMaxMana() {
        return maxMana;
    }

    @Override
    public int getBaseAttack() {
        return baseAttack;
    }

    @Override
    public int getBaseDefense() {
        return baseDefense;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public Move[] getMoves() {
        return moves;
    }

    public Move[] getSkills() {
        return moves;
    }

    @Override
    public Move getMove(int index) {
        if (moves == null || index < 0 || index >= moves.length) {
            return null;
        }
        return moves[index];
    }

    public Move getSkill1() {
        return getMove(0);
    }

    public Move getUltimate() {
        return getMove(1);
    }
    
    @Override
    public String getBio(){
        return bio;
    }

    @Override
    public java.awt.image.BufferedImage getPortrait() { 
        return null;
     }

    @Override
    public String getSpriteFolderName() {
        if (name == null) {
            return null;
        }
        return name.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
 

}

