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

    private static final int BASE_WIDTH = 72;
    private static final int BASE_HEIGHT = 96;
    private static final int MOVE_SPEED = 5;
    private static final int JUMP_VELOCITY = 15;
    private static final int ATTACK_TOTAL_FRAMES = 16;
    private static final int ATTACK_HIT_START = 8;
    private static final int ATTACK_HIT_END = 11;

    private final BattleCharacter battleCharacter;
    private final Color fallbackColor;
    private final Map<String, BufferedImage[]> animations = new HashMap<>();
    private final int drawWidth;
    private final int drawHeight;

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
        float scale = resolveSizeScale(template);
        this.drawWidth = Math.round(BASE_WIDTH * scale);
        this.drawHeight = Math.round(BASE_HEIGHT * scale);
        this.x = startX;
        this.groundY = groundY;
        this.y = groundY - this.drawHeight;
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
        return drawWidth;
    }

    public int getHeight() {
        return drawHeight;
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

        x = Math.max(minX, Math.min(maxX - getWidth(), x));

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
        return new Rectangle(x, y, getWidth(), getHeight());
    }

    public Rectangle getAttackBox() {
        int attackWidth = 36;
        int attackHeight = 40;
        int attackY = y + (getHeight() / 4);
        int attackX;

        if (facingRight) {
            attackX = x + getWidth();
        } else {
            attackX = x - attackWidth;
        }

        return new Rectangle(attackX, attackY, attackWidth, attackHeight);
    }

    public void draw(Graphics2D g2) {
        BufferedImage frame = getCurrentFrame();
        if (frame == null) {
            g2.setColor(fallbackColor);
            g2.fillRect(x, y, getWidth(), getHeight());
            return;
        }

        if (facingRight) {
            g2.drawImage(frame, x, y, getWidth(), getHeight(), null);
        } else {
            g2.drawImage(frame, x + getWidth(), y, -getWidth(), getHeight(), null);
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
        if (y + drawHeight >= groundY) { 
            y = groundY - drawHeight;     
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
            frames = getFallbackFrames();
            if (frames == null || frames.length == 0) {
                return null;
            }
        }

        int frameIndex = (animationTick / 8) % frames.length;
        if (state == AnimState.ATTACK || state == AnimState.HURT || state == AnimState.KO) {
            frameIndex = Math.min(frames.length - 1, (ATTACK_TOTAL_FRAMES - attackTimer) / 4);
        }
        return frames[frameIndex];
    }

    private BufferedImage[] getFallbackFrames() {
        BufferedImage[] preferred = animations.get("idle");
        if (preferred != null && preferred.length > 0) {
            return preferred;
        }

        preferred = animations.get("walk");
        if (preferred != null && preferred.length > 0) {
            return preferred;
        }

        preferred = animations.get("attack");
        if (preferred != null && preferred.length > 0) {
            return preferred;
        }

        preferred = animations.get("jump");
        if (preferred != null && preferred.length > 0) {
            return preferred;
        }

        preferred = animations.get("hurt");
        if (preferred != null && preferred.length > 0) {
            return preferred;
        }

        preferred = animations.get("ko");
        if (preferred != null && preferred.length > 0) {
            return preferred;
        }

        return null;
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

        if (!animations.containsKey("walk") && animations.containsKey("idle")) {
            animations.put("walk", animations.get("idle"));
        }
    }

    private void loadStateFrames(String state, String spriteFolder) {
        String[] aliases = getStateAliases(state);

        BufferedImage[] frames = new BufferedImage[0];
        for (String alias : aliases) {
            frames = loadNumberedFrames(alias, spriteFolder);
            if (frames.length > 0) {
                break;
            }
        }

        if (frames.length == 0) {
            for (String alias : aliases) {
                frames = loadSingleFrame(alias, spriteFolder);
                if (frames.length > 0) {
                    break;
                }
            }
        }

        if (frames.length == 0) {
            for (String alias : aliases) {
                frames = loadSheetFrames(alias, spriteFolder);
                if (frames.length > 0) {
                    break;
                }
            }
        }

        if (frames.length > 0) {
            animations.put(state, frames);
        }
    }

    private String[] getStateAliases(String state) {
        if ("idle".equals(state)) {
            return new String[]{"idle", "stand"};
        }
        if ("attack".equals(state)) {
            return new String[]{"attack", "punch"};
        }
        if ("hurt".equals(state)) {
            return new String[]{"hurt", "hit"};
        }
        return new String[]{state};
    }

    private BufferedImage[] loadNumberedFrames(String state, String spriteFolder) {
        BufferedImage[] loaded = new BufferedImage[9];
        int count = 0;

        for (int i = 0; i <= 8; i++) {
            BufferedImage frame = readFirstExisting(
                    "assets/sprites/" + spriteFolder + "/" + state + "_" + i + ".png",
                    "assets/sprites/" + spriteFolder + "/" + state + i + ".png",
                    "assets/" + spriteFolder + "/" + state + "_" + i + ".png",
                    "assets/" + spriteFolder + "/" + state + i + ".png"
            );

            if (frame != null) {
                loaded[count++] = frame;
            }
        }

        BufferedImage[] frames = new BufferedImage[count];
        System.arraycopy(loaded, 0, frames, 0, count);
        return frames;
    }

    private BufferedImage[] loadSheetFrames(String state, String spriteFolder) {
        BufferedImage sheet = readFirstExisting(
                "assets/sprites/" + spriteFolder + "/" + state + ".png",
                "assets/sprites/" + spriteFolder + "/" + state + ".jpg",
                "assets/sprites/" + spriteFolder + "/" + state + ".jpeg",
                "assets/sprites/" + spriteFolder + "/" + state + "_sheet.png",
                "assets/sprites/" + spriteFolder + "/" + state + "_sheet.jpg",
            "assets/sprites/" + spriteFolder + "/" + state + "_sheet.jpeg",
            "assets/" + spriteFolder + "/" + state + ".png",
            "assets/" + spriteFolder + "/" + state + ".jpg",
            "assets/" + spriteFolder + "/" + state + ".jpeg",
            "assets/" + spriteFolder + "/" + state + "_sheet.png",
            "assets/" + spriteFolder + "/" + state + "_sheet.jpg",
            "assets/" + spriteFolder + "/" + state + "_sheet.jpeg"
        );

        if (sheet == null) {
            return new BufferedImage[0];
        }

        int frameWidth = sheet.getHeight();
        if (frameWidth <= 0 || sheet.getWidth() < frameWidth || (sheet.getWidth() % frameWidth) != 0) {
            if (BASE_WIDTH > 0 && (sheet.getWidth() % BASE_WIDTH) == 0) {
                frameWidth = BASE_WIDTH;
            } else {
                return new BufferedImage[]{sheet};
            }
        }

        int frameCount = sheet.getWidth() / frameWidth;
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, sheet.getHeight());
        }
        return frames;
    }

    private BufferedImage[] loadSingleFrame(String state, String spriteFolder) {
        BufferedImage frame = readFirstExisting(
                "assets/sprites/" + spriteFolder + "/" + state + ".png",
                "assets/sprites/" + spriteFolder + "/" + state + ".jpg",
                "assets/sprites/" + spriteFolder + "/" + state + ".jpeg",
                "assets/" + spriteFolder + "/" + state + ".png",
                "assets/" + spriteFolder + "/" + state + ".jpg",
                "assets/" + spriteFolder + "/" + state + ".jpeg"
        );

        if (frame == null) {
            return new BufferedImage[0];
        }

        return new BufferedImage[]{frame};
    }

    private BufferedImage readFirstExisting(String... paths) {
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
                // Try the next file candidate.
            }
        }
        return null;
    }

    private float resolveSizeScale(GameCharacter template) {
        if (template == null || template.getName() == null) {
            return 1.15f;
        }

        if ("gigachad".equalsIgnoreCase(template.getName())) {
            return 2.0f;
        }

        if ("doge".equalsIgnoreCase(template.getName())) {
            return 1.35f;
        }

        if ("trollface".equalsIgnoreCase(template.getName())) {
            return 1.6f;
        }

        if ("pepe the frog".equalsIgnoreCase(template.getName())) {
            return 1.6f;
        }

        if ("skibidi toilet".equalsIgnoreCase(template.getName())) {
            return 1.7f;
        }

        if ("crying wojak".equalsIgnoreCase(template.getName())) {
            return 1.6f;
        }
        
        return 1.15f;
    }
}
