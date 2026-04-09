import java.awt.Color;

public class GrumpyCat extends GameCharacter {

    public GrumpyCat() {
        super(
                "Grumpy Cat",
                "Tank / Debuff",
                140,
                40,
                12,
                14,
                new Color(170, 150, 130),
                new Move[]{
                        new Move("Nope Swipe", 10, 18, 4, MoveEffect.DAMAGE, "Fast low-cost hit"),
                        new Move("Disapproval Aura", 5, 10, 10, MoveEffect.DAMAGE_AND_DEBUFF, "Damage and slow"),
                        new Move("Eternal Grump", 35, 50, 20, MoveEffect.DAMAGE, "Heavy strike")
                },
                "A fortress of pure rejection who wins by simply being unimpressed. She uses a cold, judgmental presence to slow the world down, sapping the opponent's momentum until they are crushed by the weight of her apathy."
        );
    }
}
