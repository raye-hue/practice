import java.awt.Color;

public class Gigachad extends GameCharacter {

    public Gigachad() {
        super(
                "Gigachad",
                "High Damage Boss",
                150,
                60,
                20,
                13,
                new Color(110, 110, 110),
                new Move[]{
                        new Move("Sigma Punch", 20, 30, 6, MoveEffect.DAMAGE, "Heavy basic move"),
                        new Move("Alpha Aura", 0, 0, 12, MoveEffect.BUFF_ATTACK, "Increase attack for 2 turns"),
                        new Move("Chad Slam", 45, 65, 22, MoveEffect.DAMAGE, "Massive damage")
                }
        );
    }
}
