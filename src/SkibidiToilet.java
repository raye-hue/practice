import java.awt.Color;

public class SkibidiToilet extends GameCharacter {

    public SkibidiToilet() {
        super(
                "Skibidi Toilet",
                "Chaos Multi-hit",
                100,
                60,
                17,
                8,
                new Color(200, 200, 200),
                new Move[]{
                        new Move("Toilet Spin", 5, 10, 10, MoveEffect.MULTI_HIT_DAMAGE, "Three random hits"),
                        new Move("Brain Rot", 10, 15, 12, MoveEffect.DAMAGE_AND_SKIP_CHANCE, "25% skip turn chance"),
                        new Move("Skibidi Overload", 35, 55, 20, MoveEffect.DAMAGE, "Random bonus effect")
                },
                "The personification of modern sensory overload. This fighter thrives on repetitive, high-speed chaos and psychological disorientation, overwhelming the opponent’s focus until their ability to react completely breaks down."
        );
    }
}
