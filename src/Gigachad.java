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
                150,
                60,
                20,
                13,
                new Color(255, 204, 255),
                new Move[]{
                        new Move("Sigma Punch", 20, 30, 6, MoveEffect.DAMAGE, "Heavy basic move"),
                        new Move("Alpha Aura", 0, 0, 12, MoveEffect.BUFF_ATTACK, "Increase attack for 2 turns"),
                        new Move("Chad Slam", 45, 65, 22, MoveEffect.DAMAGE, "Massive damage")
                },
                "An absolute pinnacle of discipline who dictates the battlefield through sheer confidence. He dominates by sheer presence, buffing his resolve before delivering heavy, calculated strikes that define peak performance."
        );
    }
}
