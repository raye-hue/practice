import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

public class FightManager {

    private final int screenWidth;
    private final int screenHeight;
    private final Random random = new Random();
    private BufferedImage battleBackground;
    private String battleBackgroundPath = "assets/backgrounds/GameBG.jpg";

    private Fighter player1Fighter;
    private Fighter player2Fighter;
    private GameCharacter player1Character;
    private GameCharacter player2Character;

    private String selectedPlayMode = "Player vs Player";
    private boolean ladderMode = false;
    private int ladderOpponentIndex = 0;
    private int ladderOpponentTotal = 0;

    private boolean fightInitialized = false;
    private boolean matchOver = false;
    private boolean returnToMenuRequested = false;

    private int roundOverDelayFrames = 0;
    private String roundResultText = "";
    private int player1RoundsWon = 0;
    private int player2RoundsWon = 0;

    private boolean paused = false;

    public FightManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        loadBattleBackground();
    }

    public void setBattleBackgroundPath(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        this.battleBackgroundPath = path;
        loadBattleBackground();
    }

    public void startMatch(GameCharacter p1Character, GameCharacter p2Character, String playMode) {
        this.player1Character = p1Character;
        this.player2Character = p2Character;
        this.selectedPlayMode = playMode;

        this.player1RoundsWon = 0;
        this.player2RoundsWon = 0;
        this.roundOverDelayFrames = 0;
        this.roundResultText = "";
        this.matchOver = false;
        this.returnToMenuRequested = false;

        initializeRound();
        this.fightInitialized = true;
    }

    public void update(KeyHandler keyH) {
        if (!fightInitialized || player1Fighter == null || player2Fighter == null) {
            return;
        }

        if (paused) {
            return; 
        }

        if (roundOverDelayFrames > 0) {
            roundOverDelayFrames--;
            if (roundOverDelayFrames == 0) {
                if (matchOver) {
                    returnToMenuRequested = true;
                } else {
                    startNextRound(keyH);
                }
            }
            return;
        }

        boolean playerVsCpu = "Player vs Computer".equals(selectedPlayMode);

        boolean p2Left;
        boolean p2Right;
        boolean p2Jump;

        if (playerVsCpu) {
            int distance = player1Fighter.getX() - player2Fighter.getX();
            p2Left = distance < -20;
            p2Right = distance > 20;
            p2Jump = random.nextInt(140) == 0;

            if (Math.abs(distance) < 100 && random.nextInt(24) == 0) {
                player2Fighter.tryAttack();
            }
        } else {
            p2Left = keyH.p2LeftPressed;
            p2Right = keyH.p2RightPressed;
            p2Jump = keyH.p2JumpPressed;
        }

        player1Fighter.updateMovement(keyH.p1LeftPressed, keyH.p1RightPressed, keyH.p1JumpPressed, 16, screenWidth - 16);
        player2Fighter.updateMovement(p2Left, p2Right, p2Jump, 16, screenWidth - 16);

        player1Fighter.faceTowards(player2Fighter);
        player2Fighter.faceTowards(player1Fighter);

        if (keyH.p1AttackPressed) {
            player1Fighter.tryAttack();
        }

        if (!playerVsCpu && keyH.p2AttackPressed) {
            player2Fighter.tryAttack();
        }

        resolveAttackHit(player1Fighter, player2Fighter);
        resolveAttackHit(player2Fighter, player1Fighter);

        if (player1Fighter.isDefeated() || player2Fighter.isDefeated()) {
            if (player1Fighter.isDefeated() && player2Fighter.isDefeated()) {
                roundResultText = "Double KO!";
            } else if (player2Fighter.isDefeated()) {
                player1RoundsWon++;
                if (player1RoundsWon >= 2) {
                    roundResultText = "Player 1 Wins The Match!";
                    matchOver = true;
                } else {
                    roundResultText = "Round to Player 1!";
                }
            } else {
                player2RoundsWon++;
                if (player2RoundsWon >= 2) {
                    roundResultText = playerVsCpu ? "CPU Wins The Match!" : "Player 2 Wins The Match!";
                    matchOver = true;
                } else {
                    roundResultText = playerVsCpu ? "Round to CPU!" : "Round to Player 2!";
                }
            }

            roundOverDelayFrames = 180;
        }
    }

    public void draw(Graphics2D g2) {
        drawBackground(g2);

        if (battleBackground == null) {
            g2.setColor(new Color(55, 55, 85));
            g2.fillRect(0, screenHeight - 44, screenWidth, 44);
        }

        if (player1Fighter != null) {
            player1Fighter.draw(g2);
        }
        if (player2Fighter != null) {
            player2Fighter.draw(g2);
        }

        drawHealthBars(g2);

        g2.setColor(Color.white);
        g2.setFont(new Font("Serif", Font.PLAIN, 16));
        String controlText = "P1: A/D move, W jump, F attack | P2: Left/Right move, Up jump, L attack";
        g2.drawString(controlText, 20, 28);

        if ("Player vs Computer".equals(selectedPlayMode)) {
            g2.drawString("CPU controls Player 2", 20, 48);
        }

        String opponentLabel = "Player vs Computer".equals(selectedPlayMode) ? "CPU" : "P2";
        g2.drawString("Best of 3 | Score: P1 " + player1RoundsWon + " - " + player2RoundsWon + " " + opponentLabel, 20, 68);

        if (ladderMode && ladderOpponentTotal > 0) {
            String ladderText = "Ladder: " + ladderOpponentIndex + "/" + ladderOpponentTotal;
            FontMetrics ladderMetrics = g2.getFontMetrics();
            g2.drawString(ladderText, screenWidth - 20 - ladderMetrics.stringWidth(ladderText), 28);
        }

        g2.drawString("Esc/Backspace to quit match", 20, screenHeight - 18);

        if (!roundResultText.isEmpty()) {
            g2.setFont(new Font("Impact", Font.BOLD, 44));
            FontMetrics resultMetrics = g2.getFontMetrics();
            int resultX = (screenWidth - resultMetrics.stringWidth(roundResultText)) / 2;
            g2.drawString(roundResultText, resultX, screenHeight / 2);

            g2.setFont(new Font("Serif", Font.PLAIN, 18));
            String sub = matchOver ? "Returning to menu..." : "Next round starting...";
            FontMetrics subMetrics = g2.getFontMetrics();
            int subX = (screenWidth - subMetrics.stringWidth(sub)) / 2;
            g2.drawString(sub, subX, screenHeight / 2 + 35);
        }

        if (paused) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.yellow);
        g2.setFont(new Font("Impact", Font.BOLD, 60));
        String pauseText = "PAUSED";
        FontMetrics pm = g2.getFontMetrics();
        g2.drawString(pauseText, (screenWidth - pm.stringWidth(pauseText)) / 2, screenHeight / 2 - 20);

        g2.setFont(new Font("Serif", Font.PLAIN, 22));
        String resume = "Press P to resume";
        FontMetrics rm = g2.getFontMetrics();
        g2.drawString(resume, (screenWidth - rm.stringWidth(resume)) / 2, screenHeight / 2 + 30);
}
    }

    public void reset() {
        player1Fighter = null;
        player2Fighter = null;
        player1Character = null;
        player2Character = null;
        selectedPlayMode = "Player vs Player";
        ladderMode = false;
        ladderOpponentIndex = 0;
        ladderOpponentTotal = 0;

        fightInitialized = false;
        matchOver = false;
        returnToMenuRequested = false;

        roundOverDelayFrames = 0;
        roundResultText = "";
        player1RoundsWon = 0;
        player2RoundsWon = 0;
    }

    public boolean consumeReturnToMenuRequest() {
        if (!returnToMenuRequested) {
            return false;
        }
        returnToMenuRequested = false;
        return true;
    }

    public void setLadderProgress(int opponentIndex, int opponentTotal) {
        ladderMode = true;
        ladderOpponentIndex = Math.max(0, opponentIndex);
        ladderOpponentTotal = Math.max(0, opponentTotal);
    }

    public boolean didPlayer1Win() {
        return player1RoundsWon >= 2;
    }

    public void togglePause() {
    paused = !paused;
    }

    public boolean isPaused() {
        return paused;
    }

    private void resolveAttackHit(Fighter attacker, Fighter defender) {
        if (!attacker.isAttacking() || attacker.hasHitInCurrentAttack() || !attacker.canHitNow()) {
            return;
        }

        if (!attacker.getAttackBox().intersects(defender.getBodyBox())) {
            return;
        }

        int attack = attacker.getBattleCharacter().getTemplate().getBaseAttack();
        int defense = defender.getBattleCharacter().getTemplate().getBaseDefense();
        int randomBonus = random.nextInt(6);
        int damage = Math.max(4, attack + randomBonus - (defense / 3));

        defender.getBattleCharacter().takeDamage(damage);
        defender.onHit();
        attacker.markAttackHit();
    }

    private void startNextRound(KeyHandler keyH) {
        initializeRound();
        roundOverDelayFrames = 0;
        roundResultText = "";
        keyH.resetKeys();
    }

    private void initializeRound() {
        int groundY = (int)(screenHeight * 0.88); // ← 80% down the screen
        player1Fighter = new Fighter(player1Character, 150, groundY, true, toSpriteFolderName(player1Character.getName()));
        player2Fighter = new Fighter(player2Character, screenWidth - 220, groundY, false, toSpriteFolderName(player2Character.getName()));

    }

    private String toSpriteFolderName(String characterName) {
        return characterName.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private void drawBackground(Graphics2D g2) {
        if (battleBackground != null) {
            g2.drawImage(battleBackground, 0, 0, screenWidth, screenHeight, null);
            return;
        }

        g2.setColor(new Color(18, 18, 30));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        g2.setColor(new Color(30, 30, 50));
        g2.fillRect(0, screenHeight / 2, screenWidth, screenHeight / 2);
    }

    private void loadBattleBackground() {
        File file = new File(battleBackgroundPath);
        if (!file.exists()) {
            file = resolveFirstExistingBackground();
            if (file == null) {
                battleBackground = null;
                return;
            }
        }

        try {
            battleBackground = ImageIO.read(file);
        } catch (IOException e) {
            battleBackground = null;
        }
    }

    private File resolveFirstExistingBackground() {
        String[] fallbackPaths = {
                "assets/backgrounds/bg.jpg",
                "assets/backgrounds/bg.png",
                "assets/backgrounds/battle.png",
                "assets/backgrounds/battle.jpg"
        };

        for (String path : fallbackPaths) {
            File candidate = new File(path);
            if (candidate.exists()) {
                battleBackgroundPath = path;
                return candidate;
            }
        }

        return null;
    }

    private void drawHealthBars(Graphics2D g2) {
        if (player1Fighter == null || player2Fighter == null) {
            return;
        }

        int barWidth = 240;
        int barHeight = 22;
        int barY = 58;

        BattleCharacter p1 = player1Fighter.getBattleCharacter();
        BattleCharacter p2 = player2Fighter.getBattleCharacter();

        int p1MaxHp = Math.max(1, p1.getTemplate().getMaxHp());
        int p2MaxHp = Math.max(1, p2.getTemplate().getMaxHp());
        int p1Fill = (int) (barWidth * (p1.getCurrentHp() / (double) p1MaxHp));
        int p2Fill = (int) (barWidth * (p2.getCurrentHp() / (double) p2MaxHp));

        g2.setColor(Color.darkGray);
        g2.fillRect(20, barY, barWidth, barHeight);
        g2.fillRect(screenWidth - 20 - barWidth, barY, barWidth, barHeight);

        g2.setColor(new Color(50, 200, 90));
        g2.fillRect(20, barY, p1Fill, barHeight);
        g2.fillRect(screenWidth - 20 - p2Fill, barY, p2Fill, barHeight);

        g2.setColor(Color.white);
        g2.drawRect(20, barY, barWidth, barHeight);
        g2.drawRect(screenWidth - 20 - barWidth, barY, barWidth, barHeight);

        g2.setFont(new Font("Serif", Font.BOLD, 16));
        g2.drawString(p1.getTemplate().getName() + " HP: " + p1.getCurrentHp(), 20, barY - 8);
        String p2Text = p2.getTemplate().getName() + " HP: " + p2.getCurrentHp();
        FontMetrics p2Metrics = g2.getFontMetrics();
        g2.drawString(p2Text, screenWidth - 20 - p2Metrics.stringWidth(p2Text), barY - 8);
    }
}
