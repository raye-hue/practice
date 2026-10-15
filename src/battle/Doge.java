package battle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Doge extends GameCharacter {

    private static BufferedImage portrait;

        static {
            try {
            portrait = ImageIO.read(new java.io.File("assets/CharacterProfile/Doge.png"));
            } catch (Exception e) {
                portrait = null;
            }
        }
        @Override
        public BufferedImage getPortrait() {
            return portrait;
        }

    public Doge() {
        super(
                "Dogee",
                "Balanced Fighter",
                165,
            30,
                16,
                10,
                new Color(235, 195, 130),
                new Move[]{
                        new Move("Such Strike", 15, 25, 5, MoveEffect.DAMAGE, "Reliable damage move", 420, "assets/sfx/woof.wav"),
                    new Move("Much Power", 25, 40, 14, MoveEffect.DAMAGE, "Heavy burst damage", 960)
                },
                "A beacon of chaotic optimism, Doge relies on high-spirited agility to overwhelm foes. Her prowess lies in finding the \"wow\" in every strike, balancing relentless energy with a surprising, grit-driven defense."
        );
    }
}

