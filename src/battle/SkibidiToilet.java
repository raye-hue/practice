package battle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class SkibidiToilet extends GameCharacter {

     private static BufferedImage portrait;

        static {
            try {
            portrait = ImageIO.read(new java.io.File("assets/CharacterProfile/SkibidiToilet.png"));
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
        return "skibidi";
    }

    public SkibidiToilet() {
        super(
                "Skibidi Toilet",
                "Chaos Multi-hit",
                145,
            35,
                17,
                8,
                new Color(200, 200, 200),
                new Move[]{
                        new Move("Toilet Spin", 5, 10, 10, MoveEffect.MULTI_HIT_DAMAGE, "Three random hits", 540, "assets/sfx/splash.wav"),
                    new Move("Skibidi Overload", 35, 55, 18, MoveEffect.RANDOM_EFFECT, "Randomized overload effect", 1200)
                },
                "The personification of modern sensory overload. This fighter thrives on repetitive, high-speed chaos and psychological disorientation, overwhelming the opponent’s focus until their ability to react completely breaks down."
        );
    }
}

