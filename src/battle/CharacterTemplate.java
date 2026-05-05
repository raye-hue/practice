package battle;

import java.awt.Color;

public interface CharacterTemplate {
    String getName();
    String getRole();
    int getMaxHp();
    int getMaxMana();
    int getBaseAttack();
    int getBaseDefense();
    Color getColor();
    Move[] getMoves();
    Move getMove(int index);
    String getBio();
    java.awt.image.BufferedImage getPortrait();
    String getSpriteFolderName();
}
