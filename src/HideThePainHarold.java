import java.awt.Color;

public class HideThePainHarold extends GameCharacter {

    public HideThePainHarold() {
        super(
                "Hide the Pain Harold",
                "Counter / Defense",
                125,
                50,
                15,
                12,
                new Color(210, 175, 140),
                new Move[]{
                        new Move("Forced Smile", 0, 0, 8, MoveEffect.DEFEND, "Reduce damage by 60%"),
                        new Move("Hidden Suffering", 15, 22, 6, MoveEffect.DAMAGE, "Solid damage"),
                        new Move("Burnout Burst", 40, 55, 20, MoveEffect.DAMAGE_AND_SELF_DAMAGE, "Big hit with self damage")
                },
                "The ultimate specialist in stoic endurance. Harold excels at absorbing immense pressure behind a brave face, only to unleash a lifetime of suppressed suffering in a single, explosive burst of self-sacrificial power."
        );
    }
}
