package battle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Gigachad extends GameCharacter {


     private static BufferedImage portrait;

        static {
            try {
            portrait = ImageIO.read(new java.io.File("assets/CharacterProfile/Gigachad.png"));
            } catch (Exception e) {
                portrait = null;
            }
        }
        @Override
        public BufferedImage getPortrait() {
            return portrait;
        }



    public Gigachad() {
        super(
                "Gigachad",
                "High Damage Boss",
                200,
            35,
                20,
                13,
                new Color(255, 204, 255),
                new Move[]{
                        new Move("Sigma Punch", 20, 30, 6, MoveEffect.DAMAGE, "Heavy basic move", 480),
                    new Move("Chad Slam", 45, 65, 18, MoveEffect.DAMAGE, "Massive power slam", 1080)
                },
                "An absolute pinnacle of discipline who dictates the battlefield through sheer confidence. He dominates by sheer presence, buffing his resolve before delivering heavy, calculated strikes that define peak performance."
        );
    }
}

