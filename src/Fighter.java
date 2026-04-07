import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class Fighter {

    private enum AnimState {
        IDLE,
        WALK,
        JUMP,
        ATTACK,
        HURT,
        KO
    }

    private static final int WIDTH = 72;
    private static final int HEIGHT = 96;
    private static final int MOVE_SPEED = 5;
    private static final int JUMP_VELOCITY = 15;
    private static final int ATTACK_TOTAL_FRAMES = 16;
    private static final int ATTACK_HIT_START = 8;
    private static final int ATTACK_HIT_END = 11;

    private final BattleCharacter battleCharacter;
    private final Color fallbackColor;
    private final Map<String, BufferedImage[]> animations = new HashMap<>();

    private int x;
    private int y;
    private final int groundY;
    private int velocityY;
    private boolean onGround;
    private boolean facingRight;

    private int attackTimer;
    private int attackCooldown;
    private int hurtTimer;
    private int animationTick;
    private boolean hasHitInCurrentAttack;
    private boolean movingHorizontally;

    public Fighter(GameCharacter template, int startX, int groundY, boolean facingRight, String spriteFolder) {
        this.battleCharacter = new BattleCharacter(template);
        this.fallbackColor = template.getColor();
        this.x = startX;
        this.groundY = groundY;
        this.y = groundY;
        this.facingRight = facingRight;
        this.onGround = true;
        loadSpriteSet(spriteFolder);
    }

    public BattleCharacter getBattleCharacter() {
        return battleCharacter;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public boolean isDefeated() {
        return battleCharacter.isDefeated();
    }

    public boolean isAttacking() {
        return attackTimer > 0;
    }

    public void faceTowards(Fighter other) {
        if (other.x > this.x) {
            facingRight = true;
        } else if (other.x < this.x) {
            facingRight = false;
        }
    }

    public void updateMovement(boolean moveLeft, boolean moveRight, boolean jumpPressed, int minX, int maxX) {
        movingHorizontally = false;

        if (isDefeated()) {
            updateTimers();
            applyGravity();
            return;
        }

        if (jumpPressed && onGround) {
            velocityY = -JUMP_VELOCITY;
            onGround = false;
        }

        if (hurtTimer == 0 && attackTimer == 0) {
            if (moveLeft && !moveRight) {
                x -= MOVE_SPEED;
                movingHorizontally = true;
            } else if (moveRight && !moveLeft) {
                x += MOVE_SPEED;
                movingHorizontally = true;
            }
        }

        x = Math.max(minX, Math.min(maxX - WIDTH, x));

        updateTimers();
        applyGravity();
    }

    public void tryAttack() {
        if (isDefeated()) {
            return;
        }
        if (attackCooldown > 0 || attackTimer > 0 || hurtTimer > 0) {
            return;
        }
        attackTimer = ATTACK_TOTAL_FRAMES;
        hasHitInCurrentAttack = false;
    }

    public boolean canHitNow() {
        return attackTimer >= ATTACK_HIT_START && attackTimer <= ATTACK_HIT_END;
    }

    public boolean hasHitInCurrentAttack() {
        return hasHitInCurrentAttack;
    }

    public void markAttackHit() {
        hasHitInCurrentAttack = true;
    }

    public void onHit() {
        if (!isDefeated()) {
            hurtTimer = 10;
        }
    }

    public Rectangle getBodyBox() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public Rectangle getAttackBox() {
        int attackWidth = 36;
        int attackHeight = 40;
        int attackY = y + 24;
        int attackX;

        if (facingRight) {
            attackX = x + WIDTH;
        } else {
            attackX = x - attackWidth;
        }

        return new Rectangle(attackX, attackY, attackWidth, attackHeight);
    }

    public void draw(Graphics2D g2) {
        BufferedImage frame = getCurrentFrame();
        if (frame == null) {
            g2.setColor(fallbackColor);
            g2.fillRect(x, y, WIDTH, HEIGHT);
            return;
        }

        if (facingRight) {
            g2.drawImage(frame, x, y, WIDTH, HEIGHT, null);
        } else {
            g2.drawImage(frame, x + WIDTH, y, -WIDTH, HEIGHT, null);
        }
    }

    private void updateTimers() {
        animationTick++;
        if (attackTimer > 0) {
            attackTimer--;
            if (attackTimer == 0) {
                attackCooldown = 14;
                hasHitInCurrentAttack = false;
            }
        }
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        if (hurtTimer > 0) {
            hurtTimer--;
        }
    }

    private void applyGravity() {
        if (!onGround) {
            velocityY += 1;
            y += velocityY;
            if (y >= groundY) {
                y = groundY;
                velocityY = 0;
                onGround = true;
            }
        }
    }

    private BufferedImage getCurrentFrame() {
        AnimState state = resolveState();
        String key = state.name().toLowerCase();
        BufferedImage[] frames = animations.get(key);
        if (frames == null || frames.length == 0) {
            return null;
        }

        int frameIndex = (animationTick / 8) % frames.length;
        if (state == AnimState.ATTACK || state == AnimState.HURT || state == AnimState.KO) {
            frameIndex = Math.min(frames.length - 1, (ATTACK_TOTAL_FRAMES - attackTimer) / 4);
        }
        return frames[frameIndex];
    }

    private AnimState resolveState() {
        if (isDefeated()) {
            return AnimState.KO;
        }
        if (hurtTimer > 0) {
            return AnimState.HURT;
        }
        if (attackTimer > 0) {
            return AnimState.ATTACK;
        }
        if (!onGround) {
            return AnimState.JUMP;
        }
        if (movingHorizontally) {
            return AnimState.WALK;
        }
        return AnimState.IDLE;
    }

    private void loadSpriteSet(String spriteFolder) {
        if (spriteFolder == null || spriteFolder.isEmpty()) {
            return;
        }

        loadStateFrames("idle", spriteFolder);
        loadStateFrames("walk", spriteFolder);
        loadStateFrames("jump", spriteFolder);
        loadStateFrames("attack", spriteFolder);
        loadStateFrames("hurt", spriteFolder);
        loadStateFrames("ko", spriteFolder);
    }

    private void loadStateFrames(String state, String spriteFolder) {
        BufferedImage[] loaded = new BufferedImage[8];
        int count = 0;

        for (int i = 1; i <= 8; i++) {
            File file = new File("assets/sprites/" + spriteFolder + "/" + state + "_" + i + ".png");
            if (!file.exists()) {
                continue;
            }
            try {
                loaded[count++] = ImageIO.read(file);
            } catch (IOException ignored) {
                // Keep fallback rendering when an image cannot be loaded.
            }
        }

        if (count == 0) {
            return;
        }

        BufferedImage[] frames = new BufferedImage[count];
        System.arraycopy(loaded, 0, frames, 0, count);
        animations.put(state, frames);
    }
}
