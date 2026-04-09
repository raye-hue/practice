import java.awt.Color;

public class Doge extends GameCharacter {

    public Doge() {
        super(
                "Doge",
                "Balanced Fighter",
                120,
                50,
                16,
                10,
                new Color(235, 195, 130),
                new Move[]{
                        new Move("Such Strike", 15, 25, 5, MoveEffect.DAMAGE, "Reliable damage move"),
                        new Move("Wow Guard", 0, 0, 8, MoveEffect.DEFEND, "Reduce damage by 50% next turn"),
                        new Move("Much Power", 25, 40, 15, MoveEffect.DAMAGE, "High burst damage")
                },
                "A beacon of chaotic optimism, Doge relies on high-spirited agility to overwhelm foes. Her prowess lies in finding the \"wow\" in every strike, balancing relentless energy with a surprising, grit-driven defense."
        );
    }
}
