import java.awt.Color;

public class CryingWojak extends GameCharacter {

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
                        new Move("Emotional Punch", 10, 15, 4, MoveEffect.DAMAGE, "Simple attack"),
                        new Move("Inner Rage", 0, 0, 8, MoveEffect.BUFF_ATTACK, "Permanent attack boost"),
                        new Move("Mental Breakdown", 30, 50, 18, MoveEffect.RAGE_SCALING, "Stronger below 40% HP")
                },
                "A fighter who transforms vulnerability into a volatile weapon. Initially fragile, his true power scales with his distress, tapping into a raw \"inner rage\" that becomes increasingly dangerous as his situation worsens."
        );
    }
}
