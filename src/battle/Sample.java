package battle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Sample extends GameCharacter {

    private static BufferedImage portrait;

    static {
        try {
            portrait = ImageIO.read(new java.io.File("assets/sprites/sample/portrait.png"));
        } catch (Exception e) {
            portrait = null;
        }
    }

    @Override
    public BufferedImage getPortrait() {
        return portrait;
    }

    @Override
    public String getSpriteFolderName() {
        return "sample";
    }

    public Sample() {
        super(
                "Sample",
                "Balanced Starter",
                160,
                50,
                15,
                10,
                new Color(120, 180, 255),
                new Move[]{
                        new Move("Basic Shot", 12, 20, 5, MoveEffect.DAMAGE, "Simple projectile", 360),
                        new Move("Sample Blast", 28, 42, 14, MoveEffect.DAMAGE_AND_CRIT_CHANCE, "A stronger blast", 900)
                },
                "A simple all-rounder used as the default fighter for the roster."
        );
    }
}

