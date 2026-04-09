import java.awt.Color;

public class Trollface extends GameCharacter {

    public Trollface() {
        super(
                "Trollface",
                "Trickster / Trap Fighter",
                110,
                60,
                15,
                9,
                new Color(220, 220, 220),
                new Move[]{
                        new Move("Minor Trolling", 10, 18, 5, MoveEffect.DAMAGE_AND_DEBUFF, "30% miss chance next attack"),
                        new Move("Hidden Trap", 20, 35, 12, MoveEffect.TRAP, "Trap triggers if enemy attacks"),
                        new Move("Troll Logic", 30, 50, 22, MoveEffect.CONDITIONAL_DAMAGE_OR_HEAL, "Damage or heal based on HP difference")
                },
                "A trickster who treats combat like a game of rigged logic. His prowess is built on deception and frustration, setting traps that turn the enemy's own aggression into a confusing, lose-lose situation."
        );
    }
}
