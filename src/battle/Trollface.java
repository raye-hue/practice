package battle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;


public class Trollface extends GameCharacter {

    private static BufferedImage portrait;

        static {
            try {
            portrait = ImageIO.read(new java.io.File("assets/CharacterProfile/TrollFace.png"));
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
        return "trollface";
    }



    public Trollface() {
        super(
                "Trollface",
                "Trickster / Trap Fighter",
                155, 60, 15, 9,
                new Color(220, 220, 220),
                new Move[]{
                        new Move("Minor Trolling", 10, 18, 5, MoveEffect.DAMAGE_AND_SKIP_CHANCE, "30% miss next attack", 480, "assets/sfx/laugh.wav"),
                    new Move("Troll Logic", 30, 50, 14, MoveEffect.CONDITIONAL_DAMAGE_OR_HEAL, "Damage or heal depending on HP difference", 1080)
                },
                "A trickster who treats combat like a game of rigged logic. His prowess is built on deception and frustration, setting traps that turn the enemy's own aggression into a confusing, lose-lose situation."
        );
    }
}
