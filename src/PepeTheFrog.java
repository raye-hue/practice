import java.awt.Color;

public class PepeTheFrog extends GameCharacter {

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
                        new Move("Feels Good Man", 15, 25, 10, MoveEffect.HEAL, "Heal HP"),
                        new Move("Sad Curse", 8, 15, 12, MoveEffect.DAMAGE_AND_DEBUFF, "Damage and lower enemy attack"),
                        new Move("Rare Pepe", 30, 45, 18, MoveEffect.DAMAGE_AND_CRIT_CHANCE, "30% crit chance")
                }
        );
    }
}
