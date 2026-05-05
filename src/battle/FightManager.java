package battle;

import core.AudioManager;
import core.KeyHandler;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;

public class FightManager {

    public enum AIDifficulty {
        EASY, MEDIUM, HARD
    }

    private static final int TARGET_FPS = 60;
    private static final int ROUND_SECONDS = 60;
    private static final int ROUND_TIME_FRAMES = TARGET_FPS * ROUND_SECONDS;
    private static final MoveBalanceTable MOVE_BALANCES = new MoveBalanceTable("assets/balance/move_balance.txt");

    private final int screenWidth;
    private final int screenHeight;
    private final Random random = new Random();
    private BufferedImage battleBackground;
    private String battleBackgroundPath = "assets/backgrounds/Battleground.jpg";

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
    private boolean rematchMenuActive = false;
    private int rematchMenuIndex = 0;

    private int roundOverDelayFrames = 0;
    private int roundTimeFrames = ROUND_TIME_FRAMES;
    private String roundResultText = "";
    private int player1RoundsWon = 0;
    private int player2RoundsWon = 0;

    private boolean paused = false;
    private int pauseMenuIndex = 0;
    private AIDifficulty aiDifficulty = AIDifficulty.MEDIUM;
    private final List<SkillProjectile> activeProjectiles = new ArrayList<>();
    private final Map<String, BufferedImage[]> projectileSpriteCache = new HashMap<>();
    // Round announcer/text timing
    private int currentRoundNumber = 1;
    private int announceFrames = 0;
    private String announceText = "";

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
        this.roundTimeFrames = ROUND_TIME_FRAMES;
        this.roundResultText = "";
        this.matchOver = false;
        this.returnToMenuRequested = false;

        this.currentRoundNumber = 1;
        initializeRound();
        this.fightInitialized = true;
    }

    public void update(KeyHandler keyH) {
        if (!fightInitialized || player1Fighter == null || player2Fighter == null) {
            return;
        }

        if (paused) {
            handlePauseInput(keyH);
            return;
        }

        if (roundOverDelayFrames > 0) {
            roundOverDelayFrames--;
            if (roundOverDelayFrames == 0) {
                if (matchOver) {
                    // In ladder mode, automatically signal return (so caller can progress to next opponent)
                    if (ladderMode) {
                        returnToMenuRequested = true;
                    } else {
                        // Otherwise show rematch/exit prompt
                        rematchMenuActive = true;
                        rematchMenuIndex = 0;
                    }
                } else {
                    startNextRound(keyH);
                }
            }
            return;
        }

        // If rematch menu is active, handle its input and skip normal match updates.
        if (rematchMenuActive) {
            handleRematchInput(keyH);
            return;
        }

        boolean playerVsCpu = "Player vs Computer".equals(selectedPlayMode);

        boolean p2Left;
        boolean p2Right;
        boolean p2Jump;
        boolean p2Block;
        boolean p1UsedAction = false;
        boolean p2UsedAction = false;

        if (playerVsCpu) {
            int distance = player1Fighter.getX() - player2Fighter.getX();
            switch (aiDifficulty) {
                case EASY:
                    p2Left = distance < -20;
                    p2Right = distance > 20;
                    p2Jump = random.nextInt(300) == 0;
                    p2Block = player1Fighter.isAttacking() && Math.abs(distance) < 70 && random.nextInt(3) == 0;
                    break;
                case HARD:
                    p2Left = distance < -10;
                    p2Right = distance > 10;
                    p2Jump = random.nextInt(60) == 0;
                    p2Block = (player1Fighter.isAttacking() && Math.abs(distance) < 100 && random.nextInt(2) == 0)
                            || (player2Fighter.getBattleCharacter().getCurrentHp() < player2Fighter.getBattleCharacter().getTemplate().getMaxHp() / 3
                            && Math.abs(distance) < 120 && random.nextInt(3) == 0);
                    break;
                case MEDIUM:
                default:
                    p2Left = distance < -20;
                    p2Right = distance > 20;
                    p2Jump = random.nextInt(140) == 0;
                    p2Block = (player1Fighter.isAttacking() && Math.abs(distance) < 90 && random.nextInt(2) == 0)
                            || (player2Fighter.getBattleCharacter().getCurrentHp() < player2Fighter.getBattleCharacter().getTemplate().getMaxHp() / 3
                            && Math.abs(distance) < 110 && random.nextInt(4) == 0);
                    break;
            }
        } else {
            p2Left = keyH.p2LeftPressed;
            p2Right = keyH.p2RightPressed;
            p2Jump = keyH.p2JumpPressed;
            p2Block = keyH.p2BlockPressed;
        }

        player1Fighter.updateMovement(keyH.p1LeftPressed, keyH.p1RightPressed, keyH.p1JumpPressed, keyH.p1BlockPressed, 16, screenWidth - 16);
        player2Fighter.updateMovement(p2Left, p2Right, p2Jump, p2Block, 16, screenWidth - 16);

        player1Fighter.faceTowards(player2Fighter);
        player2Fighter.faceTowards(player1Fighter);

        if (keyH.p1UltimatePressed) {
            p1UsedAction = tryUseMove(player1Fighter, player2Fighter, 1);
            keyH.p1UltimatePressed = false;
        }
        if (!p1UsedAction && keyH.p1Skill1Pressed) {
            p1UsedAction = tryUseMove(player1Fighter, player2Fighter, 0);
            keyH.p1Skill1Pressed = false;
        }

        if (!p1UsedAction && keyH.p1AttackPressed) {
            attemptBasicAttack(player1Fighter, player2Fighter);
        }

        if (playerVsCpu) {
            p2UsedAction = tryCpuSkillUsage(player2Fighter, player1Fighter);
            if (!p2UsedAction && Math.abs(player1Fighter.getX() - player2Fighter.getX()) < 100 && random.nextInt(24) == 0) {
                attemptBasicAttack(player2Fighter, player1Fighter);
            }
        } else {
            if (keyH.p2UltimatePressed) {
                p2UsedAction = tryUseMove(player2Fighter, player1Fighter, 1);
                keyH.p2UltimatePressed = false;
            }
            if (!p2UsedAction && keyH.p2Skill1Pressed) {
                p2UsedAction = tryUseMove(player2Fighter, player1Fighter, 0);
                keyH.p2Skill1Pressed = false;
            }
            if (!p2UsedAction && keyH.p2AttackPressed) {
                attemptBasicAttack(player2Fighter, player1Fighter);
            }
        }

        resolveAttackHit(player1Fighter, player2Fighter);
        resolveAttackHit(player2Fighter, player1Fighter);

        updateProjectiles();

        if (roundTimeFrames > 0) {
            roundTimeFrames--;
        }

        if (announceFrames > 0) {
            announceFrames--;
            if (announceFrames == 0) {
                announceText = "";
            }
        }

        if (player1Fighter.isDefeated() || player2Fighter.isDefeated()) {
            if (player1Fighter.isDefeated() && player2Fighter.isDefeated()) {
                finishRound("Double KO!");
            } else if (player2Fighter.isDefeated()) {
                awardRoundToPlayer1();
            } else {
                awardRoundToPlayer2(playerVsCpu);
            }

            return;
        }

        if (roundTimeFrames == 0) {
            int p1Hp = player1Fighter.getBattleCharacter().getCurrentHp();
            int p2Hp = player2Fighter.getBattleCharacter().getCurrentHp();

            if (p1Hp > p2Hp) {
                awardRoundToPlayer1("Time Up! Round to Player 1!");
            } else if (p2Hp > p1Hp) {
                awardRoundToPlayer2(
                        playerVsCpu ? "Time Up! Round to CPU!" : "Time Up! Round to Player 2!",
                        playerVsCpu
                );
            } else {
                finishRound("Time Up! Draw Round!");
            }
        }
    }

    private void handlePauseInput(KeyHandler keyH) {
        // Two options: Resume (0), Exit Match (1)
        if (keyH.menuUpPressed) {
            pauseMenuIndex = (pauseMenuIndex - 1 + 2) % 2;
            keyH.menuUpPressed = false;
        }
        if (keyH.menuDownPressed) {
            pauseMenuIndex = (pauseMenuIndex + 1) % 2;
            keyH.menuDownPressed = false;
        }

        if (keyH.menuBackPressed) {
            // Back acts like resume while paused
            paused = false;
            keyH.menuBackPressed = false;
            pauseMenuIndex = 0;
            return;
        }

        if (keyH.menuSelectPressed) {
            keyH.menuSelectPressed = false;
            if (pauseMenuIndex == 0) {
                // Resume
                paused = false;
                pauseMenuIndex = 0;
            } else {
                // Exit match: request return to menu
                returnToMenuRequested = true;
            }
            // Clear other action keys to avoid accidental inputs after resume/exit
            keyH.resetKeys();
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

        drawProjectiles(g2);

        drawHealthBars(g2);

        // Controls are shown on the pause panel instead of the HUD


        String opponentLabel = "Player vs Computer".equals(selectedPlayMode) ? "CPU" : "P2";
        g2.setFont(new Font("Serif", Font.BOLD, 22));
        String scoreText = "Score: P1 " + player1RoundsWon + " - " + player2RoundsWon + " " + opponentLabel;
        FontMetrics scoreMetrics = g2.getFontMetrics();
        int scoreX = (screenWidth - scoreMetrics.stringWidth(scoreText)) / 2;
        drawOutlinedString(g2, scoreText, scoreX, 48, Color.white, Color.black);

        g2.setFont(new Font("Serif", Font.BOLD, 22));
        String timerText = "Time: " + getRoundSecondsRemaining();
        FontMetrics timerMetrics = g2.getFontMetrics();
        int timerX = (screenWidth - timerMetrics.stringWidth(timerText)) / 2;
        drawOutlinedString(g2, timerText, timerX, 68, Color.white, Color.black);

        drawMoveStatus(g2, player1Fighter, 20, 112, true);
        drawMoveStatus(g2, player2Fighter, screenWidth - 20, 112, false);

        if (ladderMode && ladderOpponentTotal > 0) {
            String ladderText = "Ladder: " + ladderOpponentIndex + "/" + ladderOpponentTotal;
            FontMetrics ladderMetrics = g2.getFontMetrics();
            g2.drawString(ladderText, screenWidth - 20 - ladderMetrics.stringWidth(ladderText), 28);
        }

        g2.drawString("P to PAUSE", 20, screenHeight - 18);

        if (!roundResultText.isEmpty()) {
            g2.setFont(new Font("Impact", Font.BOLD, 44));
            FontMetrics resultMetrics = g2.getFontMetrics();
            int resultX = (screenWidth - resultMetrics.stringWidth(roundResultText)) / 2;
            g2.drawString(roundResultText, resultX, screenHeight / 2);

            g2.setFont(new Font("Serif", Font.PLAIN, 18));
            if (matchOver && rematchMenuActive) {
                // Draw rematch menu
                String[] options = new String[]{"Rematch", "Exit to Menu"};
                FontMetrics fm = g2.getFontMetrics();
                int panelW = 360;
                int panelH = 120;
                int panelX = (screenWidth - panelW) / 2;
                int panelY = screenHeight / 2 + 10;
                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRoundRect(panelX, panelY, panelW, panelH, 10, 10);
                g2.setColor(Color.white);
                g2.setFont(new Font("Serif", Font.PLAIN, 18));
                for (int i = 0; i < options.length; i++) {
                    String opt = options[i];
                    int optX = panelX + 28;
                    int optY = panelY + 36 + i * 36;
                    if (i == rematchMenuIndex) {
                        g2.setColor(new Color(255, 215, 0));
                    } else {
                        g2.setColor(Color.white);
                    }
                    g2.drawString(opt, optX, optY);
                }
            } else {
                String sub = matchOver ? "Match over..." : "Next round starting...";
                FontMetrics subMetrics = g2.getFontMetrics();
                int subX = (screenWidth - subMetrics.stringWidth(sub)) / 2;
                g2.drawString(sub, subX, screenHeight / 2 + 35);
            }
        }

        if (paused) {
            // Dim background
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRect(0, 0, screenWidth, screenHeight);

            // Pause title
            g2.setColor(Color.yellow);
            g2.setFont(new Font("Impact", Font.BOLD, 56));
            String pauseText = "PAUSED";
            FontMetrics pm = g2.getFontMetrics();
            int titleX = (screenWidth - pm.stringWidth(pauseText)) / 2;
            g2.drawString(pauseText, titleX, screenHeight / 2 - 90);

            // Controls panel
            g2.setFont(new Font("Serif", Font.PLAIN, 16));
            g2.setColor(new Color(30, 30, 30, 220));
            int panelW = Math.min(860, screenWidth - 120);
            int panelH = 180;
            int panelX = (screenWidth - panelW) / 2;
            int panelY = screenHeight / 2 - 70;
            g2.fillRoundRect(panelX, panelY, panelW, panelH, 10, 10);
            g2.setColor(new Color(200, 200, 200));
            g2.setFont(new Font("Serif", Font.PLAIN, 14));
            String controlText = "P1: A/D move, W jump, F attack, G block, Q skill 1, E ultimate";
            String controlText2 = "P2: Left/Right move, Up jump, L attack, K block, J skill 1, I ultimate";
            g2.drawString(controlText, panelX + 16, panelY + 32);
            g2.drawString(controlText2, panelX + 16, panelY + 56);

            // Pause menu options
            String[] options = new String[]{"Resume Match", "Exit Match"};
            g2.setFont(new Font("Serif", Font.BOLD, 20));
            for (int i = 0; i < options.length; i++) {
                String opt = options[i];
                int optX = panelX + panelW - 220;
                int optY = panelY + 40 + (i * 36);
                if (i == pauseMenuIndex) {
                    g2.setColor(new Color(255, 215, 0));
                } else {
                    g2.setColor(new Color(200, 200, 200));
                }
                g2.drawString(opt, optX, optY);
            }
        }

        // Draw round announcer text when active
        if (announceFrames > 0 && announceText != null && !announceText.isEmpty()) {
            g2.setFont(new Font("Impact", Font.BOLD, 42));
            FontMetrics fm = g2.getFontMetrics();
            int ax = (screenWidth - fm.stringWidth(announceText)) / 2;
            int ay = screenHeight / 2 - 140;
            // Glow/outline
            g2.setColor(new Color(0, 0, 0, 160));
            g2.drawString(announceText, ax + 2, ay + 2);
            g2.setColor(new Color(255, 220, 80));
            g2.drawString(announceText, ax, ay);
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
        rematchMenuActive = false;
        rematchMenuIndex = 0;

        roundOverDelayFrames = 0;
        roundTimeFrames = ROUND_TIME_FRAMES;
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

     public void setAIDifficulty(AIDifficulty difficulty) {
        this.aiDifficulty = difficulty;
    }

    private void resolveAttackHit(Fighter attacker, Fighter defender) {
        if (!attacker.isAttacking() || attacker.hasHitInCurrentAttack() || !attacker.canHitNow()) {
            return;
        }

        if (!attacker.getAttackBox().intersects(defender.getBodyBox())) {
            return;
        }

        int attack = attacker.getBattleCharacter().getTemplate().getBaseAttack() + attacker.getBattleCharacter().getAttackBonus();
        int defense = defender.getBattleCharacter().getTemplate().getBaseDefense();
        int randomBonus = random.nextInt(6);
        int damage = Math.max(4, attack + randomBonus - (defense / 3));

        defender.getBattleCharacter().takeDamage(damage);
        if (!defender.isBlocking()) {
            defender.onHit();
        }
        AudioManager.playSoundEffect("assets/sfx/punch.wav");
        attacker.markAttackHit();
    }

    private void handleRematchInput(KeyHandler keyH) {
        // Two options: Rematch (0), Exit (1)
        if (keyH.menuUpPressed) {
            rematchMenuIndex = (rematchMenuIndex - 1 + 2) % 2;
            keyH.menuUpPressed = false;
        }
        if (keyH.menuDownPressed) {
            rematchMenuIndex = (rematchMenuIndex + 1) % 2;
            keyH.menuDownPressed = false;
        }

        if (keyH.menuBackPressed) {
            // Back acts like exiting to menu
            rematchMenuActive = false;
            returnToMenuRequested = true;
            keyH.menuBackPressed = false;
            return;
        }

        if (keyH.menuSelectPressed) {
            keyH.menuSelectPressed = false;
            if (rematchMenuIndex == 0) {
                // Rematch
                startRematch();
            } else {
                // Exit to menu
                rematchMenuActive = false;
                returnToMenuRequested = true;
            }
            // Clear other action keys to avoid accidental inputs
            keyH.resetKeys();
        }
    }

    private void startRematch() {
        // Reset round counts and state, keep selected characters and mode
        player1RoundsWon = 0;
        player2RoundsWon = 0;
        roundResultText = "";
        roundOverDelayFrames = 0;
        roundTimeFrames = ROUND_TIME_FRAMES;
        matchOver = false;
        rematchMenuActive = false;
        rematchMenuIndex = 0;
        currentRoundNumber = 1;
        initializeRound();
    }

    private void attemptBasicAttack(Fighter attacker, Fighter defender) {
        BattleCharacter defenderCharacter = defender.getBattleCharacter();
        if (defenderCharacter.hasTrapArmed()) {
            int trapDamage = defenderCharacter.consumeTrapDamage();
            attacker.getBattleCharacter().takeDamage(trapDamage);
            attacker.onHit();
            if (attacker.isDefeated()) {
                return;
            }
        }

        attacker.tryAttack();
    }

    private boolean tryUseMove(Fighter attacker, Fighter defender, int moveIndex) {
        BattleCharacter attackerCharacter = attacker.getBattleCharacter();
        BattleCharacter defenderCharacter = defender.getBattleCharacter();
        Move move = attackerCharacter.getTemplate().getMove(moveIndex);
        if (move == null || !attackerCharacter.canUseMove(moveIndex)) {
            return false;
        }

        if (moveIndex == 0) {
            attacker.playSkill1Animation();
        } else {
            attacker.playUltimateAnimation();
        }

        if (move.getSoundEffectPath() != null && !move.getSoundEffectPath().isEmpty()) {
            if (move.getSoundEffectPath().endsWith("splash.wav")) {
                AudioManager.playSoundEffect(move.getSoundEffectPath(), 1.12f);
            } else {
                AudioManager.playSoundEffect(move.getSoundEffectPath());
            }
        }

        spawnProjectile(attacker, defender, moveIndex);
        attackerCharacter.spendMana(getBalancedManaCost(attackerCharacter.getTemplate(), move));
        attackerCharacter.startMoveCooldown(moveIndex, getBalancedCooldownFrames(attackerCharacter.getTemplate(), move));
        return true;
    }

    private void spawnProjectile(Fighter attacker, Fighter defender, int moveIndex) {
        String spriteFolder = getSpriteFolderName(attacker.getBattleCharacter().getTemplate());
        BufferedImage[] projectileFrames = loadProjectileSprites(spriteFolder, moveIndex);
        BufferedImage projectileSprite = projectileFrames.length > 0 ? projectileFrames[0] : null;

        int direction = attacker.isFacingRight() ? 1 : -1;
        int speed = moveIndex == 1 ? 14 : 10;
        if (moveIndex == 0 && "skibidi".equals(spriteFolder)) {
            speed = 12;
        }
        int projectileWidth = moveIndex == 1 ? 32 : 22;
        int projectileHeight = moveIndex == 1 ? 18 : 14;
        if (projectileSprite != null) {
            projectileWidth = Math.max(14, Math.min(72, projectileSprite.getWidth()));
            projectileHeight = Math.max(10, Math.min(48, projectileSprite.getHeight()));
        }

        int startX = attacker.isFacingRight() ? attacker.getX() + attacker.getWidth() - 8 : attacker.getX() + 8;
        int startY = attacker.getY() + (attacker.getHeight() / 2) - (projectileHeight / 2);
        int trailLength = moveIndex == 1 ? 9 : 6;
        int maxDistance = Math.abs(defender.getX() - attacker.getX()) + defender.getWidth() + 80;
        activeProjectiles.add(new SkillProjectile(startX, startY, direction, speed, projectileWidth, projectileHeight, trailLength, maxDistance, moveIndex, attacker, defender, projectileFrames));
    }

    private BufferedImage[] loadProjectileSprites(String spriteFolder, int moveIndex) {
        if (spriteFolder == null || spriteFolder.isEmpty()) {
            return new BufferedImage[0];
        }

        String cacheKey = spriteFolder + "#" + moveIndex;
        if (projectileSpriteCache.containsKey(cacheKey)) {
            return projectileSpriteCache.get(cacheKey);
        }

        BufferedImage[] sprites = new BufferedImage[0];
        if (moveIndex == 1) {
            BufferedImage blastSprite = readFirstExistingImage(
                    "assets/sprites/" + spriteFolder + "/blast.png",
                    "assets/sprites/" + spriteFolder + "/blast.jpg",
                    "assets/sprites/" + spriteFolder + "/blast.jpeg",
                    "assets/" + spriteFolder + "/blast.png",
                    "assets/" + spriteFolder + "/blast.jpg",
                    "assets/" + spriteFolder + "/blast.jpeg",
                    "assets/sprites/" + spriteFolder + "/ultimate.png",
                    "assets/" + spriteFolder + "/ultimate.png"
            );
            if (blastSprite != null) {
                sprites = new BufferedImage[]{blastSprite};
            }
        } else {
            BufferedImage blastSprite = readFirstExistingImage(
                    "assets/sprites/" + spriteFolder + "/blast_skill1.png",
                    "assets/sprites/" + spriteFolder + "/blast.png",
                    "assets/sprites/" + spriteFolder + "/blast.jpg",
                    "assets/sprites/" + spriteFolder + "/blast.jpeg",
                    "assets/" + spriteFolder + "/blast_skill1.png",
                    "assets/" + spriteFolder + "/blast.png",
                    "assets/" + spriteFolder + "/blast.jpg",
                    "assets/" + spriteFolder + "/blast.jpeg"
            );
            if (blastSprite != null) {
                sprites = new BufferedImage[]{blastSprite};
            }
        }

        projectileSpriteCache.put(cacheKey, sprites);
        return sprites;
    }

    private BufferedImage readFirstExistingImage(String... paths) {
        for (String path : paths) {
            File file = new File(path);
            if (!file.exists()) {
                continue;
            }
            try {
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    return image;
                }
            } catch (IOException ignored) {
                // Try next candidate.
            }
        }
        return null;
    }

    private void updateProjectiles() {
        Iterator<SkillProjectile> iterator = activeProjectiles.iterator();
        while (iterator.hasNext()) {
            SkillProjectile projectile = iterator.next();
            projectile.update();
            if (projectile.checkHit()) {
                iterator.remove();
                continue;
            }
            if (projectile.isExpired()) {
                iterator.remove();
            }
        }
    }

    private void drawProjectiles(Graphics2D g2) {
        if (activeProjectiles.isEmpty()) {
            return;
        }

        Object prevInterpolation = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        Object prevAntialias = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            for (SkillProjectile projectile : activeProjectiles) {
                projectile.draw(g2);
            }
        } finally {
            if (prevInterpolation != null) {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterpolation);
            }
            if (prevAntialias != null) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevAntialias);
            }
        }
    }

    private boolean tryCpuSkillUsage(Fighter cpu, Fighter opponent) {
        BattleCharacter cpuCharacter = cpu.getBattleCharacter();
        BattleCharacter opponentCharacter = opponent.getBattleCharacter();
        int distance = Math.abs(cpu.getX() - opponent.getX());
        boolean lowHp = cpuCharacter.getCurrentHp() <= cpuCharacter.getTemplate().getMaxHp() / 3;

        if (cpuCharacter.canUseMove(1) && (lowHp || opponentCharacter.getCurrentHp() <= opponentCharacter.getTemplate().getMaxHp() / 3 || random.nextInt(120) == 0)) {
            return tryUseMove(cpu, opponent, 1);
        }

        if (distance < 120 && cpuCharacter.canUseMove(0) && random.nextInt(3) == 0) {
            return tryUseMove(cpu, opponent, 0);
        }

        return false;
    }

    private void applyMoveEffect(BattleCharacter attacker, BattleCharacter defender, Move move) {
        int moveRoll = rollMoveValue(move);
        int balancedDamageRoll = getBalancedDamageRoll(attacker.getTemplate(), move, moveRoll);
        int attackBonus = attacker.getAttackBonus();

        switch (move.getEffect()) {
            case DAMAGE:
                dealDamage(defender, balancedDamageRoll + attackBonus);
                break;
            case HEAL:
                attacker.heal(moveRoll);
                break;
            case DEFEND:
                attacker.applyTimedDamageReduction(parsePercent(move.getDescription(), 55), TARGET_FPS * 4);
                break;
            case DAMAGE_AND_DEBUFF:
                dealDamage(defender, balancedDamageRoll + attackBonus);
                defender.addAttackBonus(-3);
                break;
            case DAMAGE_AND_CRIT_CHANCE:
                int critDamage = balancedDamageRoll + attackBonus;
                if (random.nextInt(100) < parsePercent(move.getDescription(), 30)) {
                    critDamage *= 2;
                }
                dealDamage(defender, critDamage);
                break;
            case MULTI_HIT_DAMAGE:
                for (int i = 0; i < 3; i++) {
                    dealDamage(defender, balancedDamageRoll + attackBonus);
                }
                break;
            case DAMAGE_AND_SKIP_CHANCE:
                dealDamage(defender, balancedDamageRoll + attackBonus);
                if (random.nextInt(100) < parsePercent(move.getDescription(), 25)) {
                    defender.addAttackSkipCount(1);
                }
                break;
            case BUFF_ATTACK:
                attacker.addAttackBonus(5);
                break;
            case DAMAGE_AND_SELF_DAMAGE:
                dealDamage(defender, balancedDamageRoll + attackBonus);
                attacker.takeDamage(10);
                break;
            case RAGE_SCALING:
                double hpRatio = attacker.getCurrentHp() / (double) Math.max(1, attacker.getTemplate().getMaxHp());
                int rageDamage = balancedDamageRoll + attackBonus;
                if (hpRatio <= 0.4) {
                    rageDamage = (int) Math.round(rageDamage * 1.6);
                } else if (hpRatio <= 0.7) {
                    rageDamage = (int) Math.round(rageDamage * 1.2);
                }
                dealDamage(defender, rageDamage);
                break;
            case TRAP:
                defender.armTrap(balancedDamageRoll + attackBonus);
                break;
            case CONDITIONAL_DAMAGE_OR_HEAL:
                if (attacker.getCurrentHp() < defender.getCurrentHp()) {
                    attacker.heal(15);
                } else {
                    dealDamage(defender, balancedDamageRoll + attackBonus);
                }
                break;
            case RANDOM_EFFECT:
                int randomEffect = random.nextInt(4);
                if (randomEffect == 0) {
                    dealDamage(defender, balancedDamageRoll + attackBonus);
                } else if (randomEffect == 1) {
                    attacker.heal(15);
                } else if (randomEffect == 2) {
                    attacker.addAttackBonus(4);
                } else {
                    attacker.takeDamage(8);
                }
                break;
            default:
                dealDamage(defender, balancedDamageRoll + attackBonus);
                break;
        }
    }

    private int getBalancedDamageRoll(GameCharacter character, Move move, int baseRoll) {
        double multiplier = MOVE_BALANCES.getDamageMultiplier(character.getName(), move.getName());
        return Math.max(0, (int) Math.round(baseRoll * multiplier));
    }

    private int getBalancedManaCost(GameCharacter character, Move move) {
        double multiplier = MOVE_BALANCES.getManaMultiplier(character.getName(), move.getName());
        return Math.max(0, (int) Math.round(move.getManaCost() * multiplier));
    }

    private int getBalancedCooldownFrames(GameCharacter character, Move move) {
        double multiplier = MOVE_BALANCES.getCooldownMultiplier(character.getName(), move.getName());
        return Math.max(0, (int) Math.round(move.getCooldownFrames() * multiplier));
    }

    private void dealDamage(BattleCharacter defender, int rawDamage) {
        int defense = defender.getTemplate().getBaseDefense();
        int damage = Math.max(1, rawDamage - (defense / 4));
        defender.takeDamage(damage);
    }

    private int rollMoveValue(Move move) {
        int min = Math.min(move.getMinValue(), move.getMaxValue());
        int max = Math.max(move.getMinValue(), move.getMaxValue());
        if (max <= min) {
            return Math.max(0, max);
        }
        return min + random.nextInt((max - min) + 1);
    }

    private int parsePercent(String text, int fallback) {
        if (text == null) {
            return fallback;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)\\s*%").matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return fallback;
    }

    private void drawMoveStatus(Graphics2D g2, Fighter fighter, int anchorX, int startY, boolean leftAligned) {
        if (fighter == null) {
            return;
        }

        BattleCharacter character = fighter.getBattleCharacter();
        GameCharacter template = character.getTemplate();
        Move skill1 = template.getSkill1();
        Move ultimate = template.getUltimate();

        g2.setFont(new Font("Serif", Font.PLAIN, 12));
        String manaText = "Mana: " + character.getCurrentMana() + "/" + template.getMaxMana();
        
        g2.setFont(new Font("Serif", Font.BOLD, 18));
        String skill1Text = buildMoveTimerText("S1", skill1, character.getMoveCooldownRemainingFrames(0));
        String ultimateText = buildMoveTimerText("ULT", ultimate, character.getMoveCooldownRemainingFrames(1));

        FontMetrics metrics = g2.getFontMetrics();
        int manaX = leftAligned ? anchorX : anchorX - metrics.stringWidth(manaText);
        
        FontMetrics boldMetrics = g2.getFontMetrics();
        int skill1X = leftAligned ? anchorX : anchorX - boldMetrics.stringWidth(skill1Text);
        int ultimateX = leftAligned ? anchorX : anchorX - boldMetrics.stringWidth(ultimateText);

        g2.setFont(new Font("Serif", Font.PLAIN, 12));
        g2.drawString(manaText, manaX, startY);
        
        g2.setFont(new Font("Serif", Font.BOLD, 18));
        drawOutlinedString(g2, skill1Text, skill1X, startY + 18, Color.white, Color.black);
        drawOutlinedString(g2, ultimateText, ultimateX, startY + 36, Color.white, Color.black);
    }

    private void drawOutlinedString(Graphics2D g2, String text, int x, int y, Color fillColor, Color outlineColor) {
        Color originalColor = g2.getColor();
        g2.setColor(outlineColor);
        g2.drawString(text, x - 1, y - 1);
        g2.drawString(text, x, y - 1);
        g2.drawString(text, x + 1, y - 1);
        g2.drawString(text, x - 1, y);
        g2.drawString(text, x + 1, y);
        g2.drawString(text, x - 1, y + 1);
        g2.drawString(text, x, y + 1);
        g2.drawString(text, x + 1, y + 1);
        g2.setColor(fillColor);
        g2.drawString(text, x, y);
        g2.setColor(originalColor);
    }

    private String buildMoveTimerText(String prefix, Move move, int cooldownRemainingFrames) {
        if (move == null) {
            return prefix + ": --";
        }
        return prefix + ": " + move.getName() + " [" + formatCooldown(cooldownRemainingFrames) + "]";
    }

    private String formatCooldown(int frames) {
        if (frames <= 0) {
            return "READY";
        }
        int seconds = (frames + TARGET_FPS - 1) / TARGET_FPS;
        return seconds + "s";
    }

    private void startNextRound(KeyHandler keyH) {
        initializeRound();
        roundOverDelayFrames = 0;
        roundTimeFrames = ROUND_TIME_FRAMES;
        roundResultText = "";
        keyH.resetKeys();
    }
     private int getRoundSecondsRemaining() {
        return Math.max(0, (roundTimeFrames + TARGET_FPS - 1) / TARGET_FPS);
    }

    private void awardRoundToPlayer1() {
        awardRoundToPlayer1("Round to Player 1!");
    }

    private void awardRoundToPlayer1(String roundWinText) {
        player1RoundsWon++;
        if (player2Fighter != null) {
            player2Fighter.knockOut();
        }
        if (player1RoundsWon >= 2) {
            finishRound("Player 1 Wins The Match!");
            matchOver = true;
            return;
        }

        finishRound(roundWinText);
    }

    private void awardRoundToPlayer2(boolean playerVsCpu) {
        awardRoundToPlayer2(playerVsCpu ? "Round to CPU!" : "Round to Player 2!", playerVsCpu);
    }

    private void awardRoundToPlayer2(String roundWinText, boolean playerVsCpu) {
        player2RoundsWon++;
        if (player1Fighter != null) {
            player1Fighter.knockOut();
        }
        if (player2RoundsWon >= 2) {
            finishRound(playerVsCpu ? "CPU Wins The Match!" : "Player 2 Wins The Match!");
            matchOver = true;
            return;
        }

        finishRound(roundWinText);
    }

    private void finishRound(String resultText) {
        roundResultText = resultText;
        roundOverDelayFrames = 180;
    }

    private void initializeRound() {
        int groundY = screenHeight;
        player1Fighter = new Fighter(player1Character, 150, groundY, true, getSpriteFolderName(player1Character));
        player2Fighter = new Fighter(player2Character, screenWidth - 220, groundY, false, getSpriteFolderName(player2Character));

        // Start each match with ultimates already on cooldown so they cannot be used immediately.
        if (player1Fighter.getBattleCharacter().getTemplate().getUltimate() != null) {
            player1Fighter.getBattleCharacter().startMoveCooldown(1);
        }
        if (player2Fighter.getBattleCharacter().getTemplate().getUltimate() != null) {
            player2Fighter.getBattleCharacter().startMoveCooldown(1);
        }

        // Determine current round number based on rounds already won
        currentRoundNumber = Math.max(1, player1RoundsWon + player2RoundsWon + 1);

        // Set announcer text and play appropriate sound when starting a round
        announceText = "ROUND " + currentRoundNumber + ", FIGHT!";
        // Show text for ~1.5 seconds (90 frames at 60 FPS)
        announceFrames = TARGET_FPS * 3 / 2;
        if (currentRoundNumber == 1) {
            AudioManager.playSoundEffect("assets/sfx/round1.wav");
        } else if (currentRoundNumber == 2) {
            AudioManager.playSoundEffect("assets/sfx/round2.wav");
        } else {
            // Default announce (use round2.wav for later rounds if roundN files missing)
            AudioManager.playSoundEffect("assets/sfx/round2.wav");
        }
    }

    private String getSpriteFolderName(GameCharacter character) {
        if (character == null) {
            return null;
        }

        String explicitFolder = character.getSpriteFolderName();
        if (explicitFolder != null && !explicitFolder.isEmpty()) {
            return explicitFolder;
        }

        String characterName = character.getName();
        if (characterName == null) {
            return null;
        }
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
            "assets/backgrounds/Battleground.jpg",
            "assets/backgrounds/GameBG.jpg",
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
        int manaBarY = barY + barHeight + 6;
        int manaBarHeight = 8;

        BattleCharacter p1 = player1Fighter.getBattleCharacter();
        BattleCharacter p2 = player2Fighter.getBattleCharacter();

        int p1MaxHp = Math.max(1, p1.getTemplate().getMaxHp());
        int p2MaxHp = Math.max(1, p2.getTemplate().getMaxHp());
        int p1Fill = (int) (barWidth * (p1.getCurrentHp() / (double) p1MaxHp));
        int p2Fill = (int) (barWidth * (p2.getCurrentHp() / (double) p2MaxHp));
        int p1MaxMana = Math.max(1, p1.getTemplate().getMaxMana());
        int p2MaxMana = Math.max(1, p2.getTemplate().getMaxMana());
        int p1ManaFill = (int) (barWidth * (p1.getCurrentMana() / (double) p1MaxMana));
        int p2ManaFill = (int) (barWidth * (p2.getCurrentMana() / (double) p2MaxMana));

        g2.setColor(Color.darkGray);
        g2.fillRect(20, barY, barWidth, barHeight);
        g2.fillRect(screenWidth - 20 - barWidth, barY, barWidth, barHeight);

        g2.setColor(new Color(35, 60, 110));
        g2.fillRect(20, manaBarY, barWidth, manaBarHeight);
        g2.fillRect(screenWidth - 20 - barWidth, manaBarY, barWidth, manaBarHeight);

        g2.setColor(new Color(50, 200, 90));
        g2.fillRect(20, barY, p1Fill, barHeight);
        g2.fillRect(screenWidth - 20 - p2Fill, barY, p2Fill, barHeight);

        g2.setColor(new Color(80, 150, 255));
        g2.fillRect(20, manaBarY, p1ManaFill, manaBarHeight);
        g2.fillRect(screenWidth - 20 - p2ManaFill, manaBarY, p2ManaFill, manaBarHeight);

        g2.setColor(Color.white);
        g2.drawRect(20, barY, barWidth, barHeight);
        g2.drawRect(screenWidth - 20 - barWidth, barY, barWidth, barHeight);
        g2.drawRect(20, manaBarY, barWidth, manaBarHeight);
        g2.drawRect(screenWidth - 20 - barWidth, manaBarY, barWidth, manaBarHeight);

        g2.setFont(new Font("Serif", Font.BOLD, 16));
        g2.drawString(p1.getTemplate().getName() + " HP: " + p1.getCurrentHp(), 20, barY - 8);
        String p2Text = p2.getTemplate().getName() + " HP: " + p2.getCurrentHp();
        FontMetrics p2Metrics = g2.getFontMetrics();
        g2.drawString(p2Text, screenWidth - 20 - p2Metrics.stringWidth(p2Text), barY - 8);

        g2.setFont(new Font("Serif", Font.PLAIN, 13));
        g2.drawString("Mana: " + p1.getCurrentMana() + "/" + p1.getTemplate().getMaxMana(), 20, manaBarY - 4);
        String p2ManaText = "Mana: " + p2.getCurrentMana() + "/" + p2.getTemplate().getMaxMana();
        FontMetrics p2ManaMetrics = g2.getFontMetrics();
        g2.drawString(p2ManaText, screenWidth - 20 - p2ManaMetrics.stringWidth(p2ManaText), manaBarY - 4);
    }

    private final class SkillProjectile {
        private final int direction;
        private final int speed;
        private final int width;
        private final int height;
        private final int trailLength;
        private final int maxDistance;
        private final int moveIndex;
        private final Fighter owner;
        private final Fighter target;
        private final BufferedImage[] sprites;
        private final Color coreColor;
        private final Color trailColor;
        private int x;
        private final int y;
        private int traveled;
        private int animationTick;
        private boolean expired;
        private boolean hit;

        private SkillProjectile(int x, int y, int direction, int speed, int width, int height, int trailLength, int maxDistance, int moveIndex, Fighter owner, Fighter target, BufferedImage[] sprites) {
            this.x = x;
            this.y = y;
            this.direction = direction;
            this.speed = speed;
            this.width = width;
            this.height = height;
            this.trailLength = trailLength;
            this.maxDistance = maxDistance;
            this.moveIndex = moveIndex;
            this.owner = owner;
            this.target = target;
            this.sprites = sprites == null ? new BufferedImage[0] : sprites;
            this.coreColor = moveIndex == 1 ? new Color(255, 120, 220) : new Color(255, 220, 70);
            this.trailColor = moveIndex == 1 ? new Color(255, 190, 240) : new Color(255, 245, 160);
        }

        private void update() {
            x += speed * direction;
            traveled += speed;
            animationTick++;
            if (traveled >= maxDistance) {
                expired = true;
            }
        }

        private boolean isExpired() {
            return expired;
        }

        private boolean checkHit() {
            if (expired || hit || target == null || target.isDefeated()) {
                return false;
            }

            java.awt.Rectangle projectileBox = new java.awt.Rectangle(x, y, width, height);
            if (!projectileBox.intersects(target.getBodyBox())) {
                return false;
            }

            // Dodge chance: higher if the target is moving horizontally.
            double dodgeChance = target.isMovingHorizontally() ? 0.5 : 0.15;
            if (random.nextDouble() < dodgeChance) {
                // Dodged: projectile continues.
                return false;
            }

            // Apply the move effect when the projectile hits instead of on-press.
            int beforeHp = target.getBattleCharacter().getCurrentHp();
            Move move = owner.getBattleCharacter().getTemplate().getMove(moveIndex);
            if (move != null) {
                applyMoveEffect(owner.getBattleCharacter(), target.getBattleCharacter(), move);
            }
            int afterHp = target.getBattleCharacter().getCurrentHp();
            if (afterHp < beforeHp && !target.isBlocking()) {
                target.onHit();
                AudioManager.playSoundEffect("assets/sfx/punch.wav");
            }
            hit = true;
            expired = true;
            return true;
        }

        private void draw(Graphics2D g2) {
            int coreX = x;
            int coreY = y;

            if (sprites.length > 0) {
                BufferedImage frame = sprites[(animationTick / 4) % sprites.length];
                if (direction > 0) {
                    g2.drawImage(frame, coreX, coreY, width, height, null);
                } else {
                    g2.drawImage(frame, coreX + width, coreY, -width, height, null);
                }
                return;
            }

            g2.setColor(trailColor);
            for (int i = 1; i <= trailLength; i++) {
                int trailX = coreX - (direction * i * 4);
                int trailSize = Math.max(2, height - i);
                g2.fillOval(trailX, coreY + 2, trailSize, trailSize);
            }

            g2.setColor(coreColor);
            g2.fillOval(coreX, coreY, width, height);
            g2.setColor(Color.BLACK);
            g2.drawOval(coreX, coreY, width, height);

            int tipX = direction > 0 ? coreX + width : coreX - 4;
            int tipY = coreY + (height / 2) - 1;
            g2.fillRect(tipX, tipY, 4, 2);
        }
    }
}

