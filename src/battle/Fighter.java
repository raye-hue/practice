package battle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
        SKILL1,
        ULTIMATE,
        BLOCK,
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
    private static final int BLOCK_DAMAGE_REDUCTION_PERCENT = 70;
    private static final int SPECIAL_ACTION_SKILL1_FRAMES = 18;
    private static final int SPECIAL_ACTION_ULTIMATE_FRAMES = 28;
    private static final String WALK_SFX_PATH = "assets/sfx/walk.wav";

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
    private boolean blocking;
    private AnimState specialActionState = AnimState.IDLE;
    private int specialActionTimer;

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

    public void knockOut() {
        battleCharacter.knockOut();
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

    public void updateMovement(boolean moveLeft, boolean moveRight, boolean jumpPressed, boolean blockPressed, int minX, int maxX) {
        movingHorizontally = false;
        blocking = blockPressed && !isDefeated() && attackTimer == 0 && hurtTimer == 0 && specialActionTimer == 0;
        if (blocking) {
            battleCharacter.applyBlockDamageReduction(BLOCK_DAMAGE_REDUCTION_PERCENT, 2);
        }

        if (isDefeated()) {
            updateTimers();
            applyGravity();
            return;
        }

        if (!blocking && specialActionTimer == 0 && jumpPressed && onGround) {
            velocityY = -JUMP_VELOCITY;
            onGround = false;
        }

        if (!blocking && specialActionTimer == 0 && hurtTimer == 0 && attackTimer == 0) {
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

        // Play footstep sound while moving: spaced to avoid spamming.
        if (movingHorizontally && !isDefeated()) {
            if (animationTick % 12 == 0) {
                core.AudioManager.playSoundEffect(WALK_SFX_PATH);
            }
        }
    }

    public void tryAttack() {
        if (isDefeated()) {
            return;
        }
        if (blocking || specialActionTimer > 0 || attackCooldown > 0 || attackTimer > 0 || hurtTimer > 0) {
            return;
        }
        if (battleCharacter.consumeAttackSkip()) {
            return;
        }
        attackTimer = ATTACK_TOTAL_FRAMES;
        hasHitInCurrentAttack = false;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public boolean isMovingHorizontally() {
        return movingHorizontally;
    }

    public void playSkill1Animation() {
        if (!isDefeated()) {
            specialActionState = AnimState.SKILL1;
            specialActionTimer = SPECIAL_ACTION_SKILL1_FRAMES;
        }
    }

    public void playUltimateAnimation() {
        if (!isDefeated()) {
            specialActionState = AnimState.ULTIMATE;
            specialActionTimer = SPECIAL_ACTION_ULTIMATE_FRAMES;
        }
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
            hurtTimer = 16;
        }
    }

    public Rectangle getBodyBox() {
        return new Rectangle(x, y, getWidth(), getHeight());
    }

    public Rectangle getAttackBox() {
        float scale = drawWidth / (float) BASE_WIDTH;

        int attackWidth = Math.round(20 * scale);
        int attackHeight = Math.round(30 * scale);
        int attackY = y + (getHeight() / 4);
        int attackX;

        if (facingRight) {
            attackX = x + getWidth();
        } else {
            attackX = x - attackWidth;
        }

        return new Rectangle(attackX, attackY, attackWidth, attackHeight);
    }

        private int getVisualOffsetY() {
        String name = battleCharacter.getTemplate().getName();
        
        if ("gigachad".equalsIgnoreCase(name)) {
            return 0;
        }
        if ("hide the pain harold".equalsIgnoreCase(name)) {
            return 20;
        }
        if ("skibidi toilet".equalsIgnoreCase(name)) {
            return 15;
        }
        if ("crying wojak".equalsIgnoreCase(name)) {
            return -15;
        }
        if ("trollface".equalsIgnoreCase(name)) {
            return 15;
        }
        if ("doge".equalsIgnoreCase(name)) {
            return 0;
        }
        if ("grumpy cat".equalsIgnoreCase(name)) {
            return 0;
        }
        if ("pepe the frog".equalsIgnoreCase(name)) {
            return 0;
        }
        return 0;
    }

    
        public void draw(Graphics2D g2) {
        BufferedImage frame = getCurrentFrame();
        int offsetY = getVisualOffsetY();

        int shadowWidth = Math.max(28, getWidth() - 16);
        int shadowHeight = Math.max(8, getHeight() / 9);
        int shadowX = x + (getWidth() - shadowWidth) / 2;
        int shadowY = y + getHeight() - shadowHeight / 2;

        java.awt.Color prevColor = g2.getColor();
        g2.setColor(new java.awt.Color(0, 0, 0, isDefeated() ? 60 : 90));
        g2.fillOval(shadowX, shadowY, shadowWidth, shadowHeight);
        g2.setColor(prevColor);

        if (frame == null) {
            g2.setColor(fallbackColor);
            g2.fillRect(x, y + offsetY, getWidth(), getHeight());
            return;
        }

        // Use nearest-neighbor interpolation for pixel-art sprites to keep edges crisp.
        Object prevInterpolation = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        Object prevAntialias = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            if (facingRight) {
                g2.drawImage(frame, x, y + offsetY, getWidth(), getHeight(), null);
            } else {
                g2.drawImage(frame, x + getWidth(), y + offsetY, -getWidth(), getHeight(), null);
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

    private void updateTimers() {
        animationTick++;
        battleCharacter.tickStatusEffects();
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
        if (specialActionTimer > 0) {
            specialActionTimer--;
            if (specialActionTimer == 0) {
                specialActionState = AnimState.IDLE;
            }
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
        } else if (state == AnimState.SKILL1 || state == AnimState.ULTIMATE) {
            frameIndex = Math.min(frames.length - 1, animationTick / 4);
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
        if (specialActionTimer > 0) {
            return specialActionState;
        }
        if (blocking) {
            return AnimState.BLOCK;
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
        loadStateFrames("skill1", spriteFolder);
        loadStateFrames("ultimate", spriteFolder);
        loadStateFrames("block", spriteFolder);
        loadStateFrames("hurt", spriteFolder);
        loadStateFrames("ko", spriteFolder);

        if (!animations.containsKey("walk") && animations.containsKey("idle")) {
            animations.put("walk", animations.get("idle"));
        }
    }

    private void loadStateFrames(String state, String spriteFolder) {
        String[] aliases = getStateAliases(state);

        if ("ultimate".equals(state)) {
            BufferedImage[] preferredUltimateFrames = loadPreferredUltimateFrames(spriteFolder);
            if (preferredUltimateFrames.length > 0) {
                animations.put(state, preferredUltimateFrames);
                return;
            }
        }

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

    private BufferedImage[] loadPreferredUltimateFrames(String spriteFolder) {
        BufferedImage[] loaded = new BufferedImage[6];
        int count = 0;

        BufferedImage base = readFirstExisting(
                "assets/sprites/" + spriteFolder + "/ultimate.png",
                "assets/sprites/" + spriteFolder + "/ultimate.jpg",
                "assets/sprites/" + spriteFolder + "/ultimate.jpeg",
                "assets/" + spriteFolder + "/ultimate.png",
                "assets/" + spriteFolder + "/ultimate.jpg",
                "assets/" + spriteFolder + "/ultimate.jpeg"
        );
        if (base != null) {
            loaded[count++] = base;
        }

        for (int i = 1; i <= 5; i++) {
            BufferedImage frame = readFirstExisting(
                    "assets/sprites/" + spriteFolder + "/ultimate" + i + ".png",
                    "assets/sprites/" + spriteFolder + "/ultimate" + i + ".jpg",
                    "assets/sprites/" + spriteFolder + "/ultimate" + i + ".jpeg",
                    "assets/" + spriteFolder + "/ultimate" + i + ".png",
                    "assets/" + spriteFolder + "/ultimate" + i + ".jpg",
                    "assets/" + spriteFolder + "/ultimate" + i + ".jpeg"
            );
            if (frame != null) {
                loaded[count++] = frame;
            }
        }

        BufferedImage[] frames = new BufferedImage[count];
        System.arraycopy(loaded, 0, frames, 0, count);
        return frames;
    }

    private String[] getStateAliases(String state) {
        if ("idle".equals(state)) {
            return new String[]{"idle", "stand"};
        }
        if ("attack".equals(state)) {
            return new String[]{"attack", "punch"};
        }
        if ("skill1".equals(state)) {
            return new String[]{"skill1"};
        }
        if ("ultimate".equals(state)) {
            return new String[]{"ultimate"};
        }
        if ("block".equals(state)) {
            return new String[]{"block", "guard"};
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

        if ("pepe the frog".equalsIgnoreCase(template.getName())) {
            return 2.0f;
        }

        if ("gigachad".equalsIgnoreCase(template.getName())) {
            return 2.2f;
        }

        if ("doge".equalsIgnoreCase(template.getName())) {
            return 1.50f;
        }

        if ("hide the pain harold".equalsIgnoreCase(template.getName())) {
            return 2.4f;
        }

        if ("grumpy cat".equalsIgnoreCase(template.getName())) {
            return 1.6f;
        }
        if ("skibidi toilet".equalsIgnoreCase(template.getName())) {
            return 2.2f;
        }
        if ("crying wojak".equalsIgnoreCase(template.getName())) {
            return 2.0f;
        }
        if ("trollface".equalsIgnoreCase(template.getName())) {
            return 2.0f;
        }


        return 1.0f;
    }
}

