package battle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class PepeTheFrog extends GameCharacter {

     private static BufferedImage portrait;

        static {
            try {
            portrait = ImageIO.read(new java.io.File("assets/CharacterProfile/Pepe.png"));
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
            return "pepe";
        }


    public PepeTheFrog() {
        super(
                "Pepe the Frog",
                "Status / Sustain",
                110,
                55,
                14,
                9,
                new Color(103, 168, 74),
                new Move[]{
                        new Move("Feels Good Man", 15, 25, 10, MoveEffect.HEAL, "Heal HP", 480),
                    new Move("Rare Pepe", 30, 45, 16, MoveEffect.DAMAGE_AND_CRIT_CHANCE, "30% crit chance", 1320)
                },
                "A master of emotional range, Pepe channels his internal state to manipulate the flow of battle. He can manifest tranquil healing when \"feeling good\" or weaponize a rare, sudden intensity that catches enemies off-guard."
        );
    }
}

