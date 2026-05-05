package battle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class GrumpyCat extends GameCharacter {

    private static BufferedImage portrait;

        static {
            try {
            portrait = ImageIO.read(new java.io.File("assets/CharacterProfile/GrumpyCat.png"));
            } catch (Exception e) {
                portrait = null;
            }
        }
        @Override
        public BufferedImage getPortrait() {
            return portrait;
        }

    public GrumpyCat() {
        super(
                "Grumpy Cat",
                "Tank / Debuff",
                190,
                40,
                12,
                14,
                new Color(170, 150, 130),
                new Move[]{
                    new Move("Nope Swipe", 10, 18, 4, MoveEffect.DAMAGE, "Fast low-cost hit", 360, "assets/sfx/grumpskill1.wav"),
                    new Move("Eternal Grump", 35, 50, 15, MoveEffect.DAMAGE, "Heavy grump damage", 900, "assets/sfx/grumpult.wav")
                },
                "A fortress of pure rejection who wins by simply being unimpressed. She uses a cold, judgmental presence to slow the world down, sapping the opponent's momentum until they are crushed by the weight of her apathy."
        );
    }
}

