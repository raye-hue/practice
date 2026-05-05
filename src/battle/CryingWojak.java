package battle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class CryingWojak extends GameCharacter {

    private static BufferedImage portrait;

    static {
        try {
        portrait = ImageIO.read(new java.io.File("assets/CharacterProfile/CryingWojak.png"));
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
            return "wojak";
        }

    public CryingWojak() {
        super(
                "Crying Wojak",
                "Rage Scaling Fighter",
                110,
                50,
                14,
                9,
                new Color(165, 165, 175),
                new Move[]{
                        new Move("Inner Rage", 0, 0, 8, MoveEffect.BUFF_ATTACK, "+5 permanent attack", 480),
                    new Move("Mental Breakdown", 30, 50, 16, MoveEffect.RAGE_SCALING, "Stronger if HP is low", 1200)
                },
                "A fighter who transforms vulnerability into a volatile weapon. Initially fragile, his true power scales with his distress, tapping into a raw \"inner rage\" that becomes increasingly dangerous as his situation worsens."
        );
    }
}

