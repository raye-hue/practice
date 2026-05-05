package util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PixelArtGenerator {
    private static final int WIDTH = 72;
    private static final int HEIGHT = 96;
    private static final int PORTRAIT_SIZE = 90;

    public static void main(String[] args) throws IOException {
        File dir = new File("assets/sprites/sample");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dogeDir = new File("assets/doge");
        if (!dogeDir.exists()) {
            dogeDir.mkdirs();
        }

        // Generate block pose
        saveImage("assets/sprites/sample/block.png", generateBlockPose());

        // Generate skill1 frames (6 frames)
        for (int i = 0; i < 6; i++) {
            saveImage("assets/sprites/sample/skill1_" + i + ".png", generateSkill1Frame(i));
        }
        // Generate skill1 sheet
        BufferedImage skill1Sheet = generateSkill1Sheet();
        saveImage("assets/sprites/sample/skill1_sheet.png", skill1Sheet);

        // Generate ultimate frames (8 frames)
        for (int i = 0; i < 8; i++) {
            saveImage("assets/sprites/sample/ultimate_" + i + ".png", generateUltimateFrame(i));
        }
        // Generate ultimate sheet
        BufferedImage ultimateSheet = generateUltimateSheet();
        saveImage("assets/sprites/sample/ultimate_sheet.png", ultimateSheet);

        // Generate portrait
        saveImage("assets/sprites/sample/portrait.png", generatePortrait());

        // Generate idle pose
        saveImage("assets/sprites/sample/idle.png", generateIdlePose());

        // Generate walk frames (4 frames)
        for (int i = 0; i < 4; i++) {
            saveImage("assets/sprites/sample/walk_" + i + ".png", generateWalkFrame(i));
        }

        // Generate jump pose
        saveImage("assets/sprites/sample/jump.png", generateJumpPose());

        // Generate attack frames (4 frames)
        for (int i = 0; i < 4; i++) {
            saveImage("assets/sprites/sample/attack_" + i + ".png", generateAttackFrame(i));
        }

        // Generate hurt pose
        saveImage("assets/sprites/sample/hurt.png", generateHurtPose());

        // Generate KO pose
        saveImage("assets/sprites/sample/ko.png", generateKOPose());

        // Generate Doge ultimate frames
        for (int i = 0; i < 8; i++) {
            saveImage("assets/doge/ultimate_" + i + ".png", generateDogeUltimateFrame(i));
        }
        saveImage("assets/doge/ultimate_sheet.png", generateDogeUltimateSheet());
        saveImage("assets/doge/ultimate.png", generateDogeUltimateFrame(7));

        System.out.println("Pixel-art sprites generated in assets/sprites/sample/");
    }

    private static BufferedImage generateBlockPose() {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        fillTransparent(img);

        // Simple blocking character: shield-like pose
        Color bodyColor = new Color(200, 100, 255);
        Color shieldColor = new Color(100, 200, 255);
        Color outlineColor = Color.BLACK;

        // Draw body (vertical rectangle)
        drawFilledRect(img, 28, 30, 16, 40, bodyColor);
        drawOutlineRect(img, 28, 30, 16, 40, outlineColor);

        // Draw head (circle-ish)
        drawFilledCircle(img, 36, 20, 8, bodyColor);

        // Draw shield (large rectangle in front)
        drawFilledRect(img, 44, 25, 18, 35, shieldColor);
        drawOutlineRect(img, 44, 25, 18, 35, outlineColor);

        // Draw legs (two small rectangles)
        drawFilledRect(img, 30, 70, 6, 15, bodyColor);
        drawFilledRect(img, 42, 70, 6, 15, bodyColor);

        return img;
    }

    private static BufferedImage generateSkill1Frame(int frame) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        fillTransparent(img);

        Color bodyColor = new Color(200, 100, 255);
        Color effectColor = new Color(255, 200, 0); // Yellow effect
        Color trajectoryColor = new Color(255, 255, 100); // Light yellow trail
        Color trailColor = new Color(255, 220, 50); // Darker trail
        Color outlineColor = Color.BLACK;

        // Animated position based on frame
        int offsetX = (frame * 2) - 5;

        // Draw body
        drawFilledRect(img, 28 + offsetX, 30, 16, 40, bodyColor);
        drawOutlineRect(img, 28 + offsetX, 30, 16, 40, outlineColor);

        // Draw head
        drawFilledCircle(img, 36 + offsetX, 20, 8, bodyColor);

        // Effect position (travels right)
        int startX = 44 + offsetX;
        int startY = 35;
        int effectX = 50 + offsetX + (frame * 3);
        int endY = 35;

        // Draw motion trail from character to effect (with fading dots)
        int trailSteps = (frame * 3) / 3 + 1;
        for (int i = 0; i < trailSteps; i++) {
            int trailX = startX + (i * 3);
            if (trailX < effectX) {
                // Fade trail opacity effect by using alternating pattern
                if (i % 2 == 0) {
                    drawFilledCircle(img, trailX, endY, 2, trailColor);
                }
            }
        }

        // Draw main trajectory line
        drawLine(img, startX, startY, effectX, endY, trajectoryColor);

        // Draw directional arrow at effect endpoint
        drawArrow(img, effectX, endY, 3, trajectoryColor);

        // Draw skill effect (expanding particles)
        int effectRadius = 4 + (frame * 2);
        drawFilledCircle(img, effectX, endY, effectRadius, effectColor);
        drawOutlineCircle(img, effectX, endY, effectRadius, outlineColor);

        // Draw legs
        drawFilledRect(img, 30 + offsetX, 70, 6, 15, bodyColor);
        drawFilledRect(img, 42 + offsetX, 70, 6, 15, bodyColor);

        return img;
    }

    private static BufferedImage generateSkill1Sheet() {
        BufferedImage sheet = new BufferedImage(WIDTH * 6, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        fillTransparent(sheet);

        for (int i = 0; i < 6; i++) {
            BufferedImage frame = generateSkill1Frame(i);
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    int rgb = frame.getRGB(x, y);
                    sheet.setRGB(i * WIDTH + x, y, rgb);
                }
            }
        }
        return sheet;
    }

    private static BufferedImage generateUltimateFrame(int frame) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        fillTransparent(img);

        Color bodyColor = new Color(200, 100, 255);
        Color effectColor = new Color(255, 100, 200); // Pink/magenta effect
        Color outlineColor = Color.BLACK;

        // Draw body (grows then shrinks)
        int scale = (frame < 4) ? 8 + (frame * 2) : 24 - ((frame - 4) * 2);
        scale = Math.max(8, scale);

        drawFilledRect(img, 36 - (scale / 2), 30, scale, 40, bodyColor);
        drawOutlineRect(img, 36 - (scale / 2), 30, scale, 40, outlineColor);

        // Draw head
        drawFilledCircle(img, 36, 20, 8, bodyColor);

        // Draw expanding/shrinking aura
        int auraRadius = 6 + (frame * 3);
        drawOutlineCircle(img, 36, 40, auraRadius, effectColor);

        // Draw legs
        drawFilledRect(img, 30, 70, 6, 15, bodyColor);
        drawFilledRect(img, 42, 70, 6, 15, bodyColor);

        return img;
    }

    private static BufferedImage generateDogeUltimateFrame(int frame) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        fillTransparent(img);

        Color furLight = new Color(238, 209, 165);
        Color furDark = new Color(191, 150, 97);
        Color tonguePink = new Color(255, 120, 170);
        Color auraYellow = new Color(255, 220, 80);
        Color auraOrange = new Color(255, 160, 50);
        Color outlineColor = Color.BLACK;

        int pulse = frame < 4 ? frame : 7 - frame;
        int auraRadius = 10 + (pulse * 4);
        int auraX = 36;
        int auraY = 42;

        drawFilledCircle(img, auraX, auraY, auraRadius, auraYellow);
        drawOutlineCircle(img, auraX, auraY, auraRadius, auraOrange);

        for (int i = 0; i < 6; i++) {
            int sparkX = auraX + (int) (Math.cos((frame * 35 + i * 60) * Math.PI / 180.0) * (auraRadius + 2));
            int sparkY = auraY + (int) (Math.sin((frame * 35 + i * 60) * Math.PI / 180.0) * (auraRadius + 2));
            drawFilledRect(img, sparkX, sparkY, 2, 2, auraOrange);
        }

        int faceX = 18 + pulse;
        int faceY = 18;
        drawFilledCircle(img, faceX + 16, faceY + 16, 15, furLight);
        drawOutlineCircle(img, faceX + 16, faceY + 16, 15, outlineColor);

        drawFilledRect(img, faceX + 10, faceY + 10, 8, 5, furDark);
        drawFilledRect(img, faceX + 18, faceY + 10, 8, 5, furDark);
        drawFilledRect(img, faceX + 8, faceY + 18, 24, 8, furLight);
        drawFilledRect(img, faceX + 6, faceY + 24, 28, 10, furLight);
        drawOutlineRect(img, faceX + 8, faceY + 18, 24, 8, outlineColor);
        drawOutlineRect(img, faceX + 6, faceY + 24, 28, 10, outlineColor);

        drawFilledCircle(img, faceX + 12, faceY + 17, 2, outlineColor);
        drawFilledCircle(img, faceX + 21, faceY + 17, 2, outlineColor);
        drawFilledRect(img, faceX + 15, faceY + 21, 4, 2, outlineColor);
        drawFilledRect(img, faceX + 13, faceY + 25, 8, 4, tonguePink);
        drawOutlineRect(img, faceX + 13, faceY + 25, 8, 4, outlineColor);

        drawFilledRect(img, faceX + 7, faceY + 37, 7, 10, furDark);
        drawFilledRect(img, faceX + 24, faceY + 37, 7, 10, furDark);
        drawOutlineRect(img, faceX + 7, faceY + 37, 7, 10, outlineColor);
        drawOutlineRect(img, faceX + 24, faceY + 37, 7, 10, outlineColor);

        int blastLength = 10 + (frame * 4);
        drawLine(img, faceX + 30, faceY + 28, faceX + 30 + blastLength, faceY + 28, auraOrange);
        drawLine(img, faceX + 30, faceY + 27, faceX + 30 + blastLength, faceY + 27, auraYellow);
        drawArrow(img, faceX + 30 + blastLength, faceY + 28, 4, auraYellow);

        int blastTip = faceX + 30 + blastLength;
        drawFilledCircle(img, blastTip, faceY + 28, 5 + pulse, auraYellow);
        drawOutlineCircle(img, blastTip, faceY + 28, 5 + pulse, outlineColor);

        return img;
    }

    private static BufferedImage generateDogeUltimateSheet() {
        BufferedImage sheet = new BufferedImage(WIDTH * 8, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        fillTransparent(sheet);

        for (int i = 0; i < 8; i++) {
            BufferedImage frame = generateDogeUltimateFrame(i);
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    sheet.setRGB(i * WIDTH + x, y, frame.getRGB(x, y));
                }
            }
        }

        return sheet;
    }

    private static BufferedImage generateUltimateSheet() {
        BufferedImage sheet = new BufferedImage(WIDTH * 8, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        fillTransparent(sheet);

        for (int i = 0; i < 8; i++) {
            BufferedImage frame = generateUltimateFrame(i);
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    int rgb = frame.getRGB(x, y);
                    sheet.setRGB(i * WIDTH + x, y, rgb);
                }
            }
        }
        return sheet;
    }

    private static BufferedImage generatePortrait() {
        BufferedImage img = new BufferedImage(PORTRAIT_SIZE, PORTRAIT_SIZE, BufferedImage.TYPE_INT_ARGB);
        fillTransparent(img);

        Color bodyColor = new Color(200, 100, 255);
        Color outlineColor = Color.BLACK;

        // Draw simple face
        drawFilledCircle(img, 45, 35, 20, bodyColor);
        drawOutlineCircle(img, 45, 35, 20, outlineColor);

        // Draw eyes
        drawFilledRect(img, 35, 28, 4, 4, Color.WHITE);
        drawFilledRect(img, 55, 28, 4, 4, Color.WHITE);
        drawFilledRect(img, 36, 29, 2, 2, Color.BLACK);
        drawFilledRect(img, 56, 29, 2, 2, Color.BLACK);

        return img;
    }

    private static void fillTransparent(BufferedImage img) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                img.setRGB(x, y, 0x00000000);
            }
        }
    }

    private static void drawFilledRect(BufferedImage img, int x, int y, int w, int h, Color color) {
        int rgb = color.getRGB();
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                int px = x + dx;
                int py = y + dy;
                if (px >= 0 && px < img.getWidth() && py >= 0 && py < img.getHeight()) {
                    img.setRGB(px, py, rgb);
                }
            }
        }
    }

    private static void drawOutlineRect(BufferedImage img, int x, int y, int w, int h, Color color) {
        int rgb = color.getRGB();
        for (int dx = 0; dx < w; dx++) {
            if (y >= 0 && y < img.getHeight() && x + dx >= 0 && x + dx < img.getWidth()) {
                img.setRGB(x + dx, y, rgb);
            }
            if (y + h - 1 >= 0 && y + h - 1 < img.getHeight() && x + dx >= 0 && x + dx < img.getWidth()) {
                img.setRGB(x + dx, y + h - 1, rgb);
            }
        }
        for (int dy = 0; dy < h; dy++) {
            if (x >= 0 && x < img.getWidth() && y + dy >= 0 && y + dy < img.getHeight()) {
                img.setRGB(x, y + dy, rgb);
            }
            if (x + w - 1 >= 0 && x + w - 1 < img.getWidth() && y + dy >= 0 && y + dy < img.getHeight()) {
                img.setRGB(x + w - 1, y + dy, rgb);
            }
        }
    }

    private static void drawFilledCircle(BufferedImage img, int cx, int cy, int radius, Color color) {
        int rgb = color.getRGB();
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    int px = cx + x;
                    int py = cy + y;
                    if (px >= 0 && px < img.getWidth() && py >= 0 && py < img.getHeight()) {
                        img.setRGB(px, py, rgb);
                    }
                }
            }
        }
    }

    private static void drawOutlineCircle(BufferedImage img, int cx, int cy, int radius, Color color) {
        int rgb = color.getRGB();
        for (int angle = 0; angle < 360; angle += 5) {
            double rad = Math.toRadians(angle);
            int x = cx + (int) (radius * Math.cos(rad));
            int y = cy + (int) (radius * Math.sin(rad));
            if (x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight()) {
                img.setRGB(x, y, rgb);
            }
        }
    }

    private static void drawLine(BufferedImage img, int x0, int y0, int x1, int y1, Color color) {
        int rgb = color.getRGB();
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int x = x0;
        int y = y0;

        while (true) {
            if (x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight()) {
                img.setRGB(x, y, rgb);
            }
            if (x == x1 && y == y1) {
                break;
            }
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    private static void drawArrow(BufferedImage img, int x, int y, int size, Color color) {
        // Draw a simple right-pointing arrow
        // Main tip point
        drawFilledCircle(img, x, y, 1, color);
        // Left fletches
        drawFilledRect(img, x - (size / 2), y - 2, 2, 1, color);
        drawFilledRect(img, x - (size / 2), y + 2, 2, 1, color);
        // Arrow shaft indicator
        drawLine(img, x, y, x - size, y, color);
    }

    private static void saveImage(String path, BufferedImage img) throws IOException {
        ImageIO.write(img, "PNG", new File(path));
        System.out.println("Generated: " + path);
    }

    private static BufferedImage generateIdlePose() {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        fillTransparent(img);

        Color bodyColor = new Color(200, 100, 255);
        Color outlineColor = Color.BLACK;

        // Draw body (standing upright)
        drawFilledRect(img, 30, 30, 12, 35, bodyColor);
        drawOutlineRect(img, 30, 30, 12, 35, outlineColor);

        // Draw head
        drawFilledCircle(img, 36, 20, 8, bodyColor);

        // Draw arms
        drawFilledRect(img, 20, 35, 10, 4, bodyColor);
        drawFilledRect(img, 42, 35, 10, 4, bodyColor);

        // Draw legs
        drawFilledRect(img, 31, 65, 4, 15, bodyColor);
        drawFilledRect(img, 41, 65, 4, 15, bodyColor);

        return img;
    }

    private static BufferedImage generateWalkFrame(int frame) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        fillTransparent(img);

        Color bodyColor = new Color(200, 100, 255);
        Color outlineColor = Color.BLACK;

        // Offset based on frame for walking motion
        int offsetX = (frame % 2) == 0 ? -2 : 2;

        // Draw body
        drawFilledRect(img, 30 + offsetX, 30, 12, 35, bodyColor);
        drawOutlineRect(img, 30 + offsetX, 30, 12, 35, outlineColor);

        // Draw head
        drawFilledCircle(img, 36 + offsetX, 20, 8, bodyColor);

        // Draw arms (swinging)
        int armOffset = (frame % 2) == 0 ? -3 : 3;
        drawFilledRect(img, 20 + offsetX, 35 + armOffset, 10, 4, bodyColor);
        drawFilledRect(img, 42 + offsetX, 35 - armOffset, 10, 4, bodyColor);

        // Draw legs (stepping)
        int leg1Y = 65 + ((frame % 2) == 0 ? -2 : 0);
        int leg2Y = 65 + ((frame % 2) == 0 ? 0 : -2);
        drawFilledRect(img, 31 + offsetX, leg1Y, 4, 15, bodyColor);
        drawFilledRect(img, 41 + offsetX, leg2Y, 4, 15, bodyColor);

        return img;
    }

    private static BufferedImage generateJumpPose() {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        fillTransparent(img);

        Color bodyColor = new Color(200, 100, 255);
        Color outlineColor = Color.BLACK;

        // Draw body (jumping, higher up)
        drawFilledRect(img, 30, 10, 12, 35, bodyColor);
        drawOutlineRect(img, 30, 10, 12, 35, outlineColor);

        // Draw head
        drawFilledCircle(img, 36, 0, 7, bodyColor);

        // Draw arms (raised)
        drawFilledRect(img, 20, 18, 10, 4, bodyColor);
        drawFilledRect(img, 42, 18, 10, 4, bodyColor);

        // Draw legs (tucked)
        drawFilledRect(img, 32, 45, 3, 8, bodyColor);
        drawFilledRect(img, 41, 45, 3, 8, bodyColor);

        return img;
    }

    private static BufferedImage generateAttackFrame(int frame) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        fillTransparent(img);

        Color bodyColor = new Color(200, 100, 255);
        Color strikeColor = new Color(255, 150, 0);
        Color outlineColor = Color.BLACK;

        // Draw body (leaning forward)
        drawFilledRect(img, 28 + (frame * 2), 30, 12, 35, bodyColor);
        drawOutlineRect(img, 28 + (frame * 2), 30, 12, 35, outlineColor);

        // Draw head
        drawFilledCircle(img, 34 + (frame * 2), 20, 8, bodyColor);

        // Draw attacking arm (extended punch)
        int punchLength = 5 + (frame * 3);
        drawFilledRect(img, 40 + (frame * 2), 35, punchLength, 4, strikeColor);

        // Draw legs (braced)
        drawFilledRect(img, 29 + (frame * 2), 65, 5, 15, bodyColor);
        drawFilledRect(img, 39 + (frame * 2), 65, 5, 15, bodyColor);

        return img;
    }

    private static BufferedImage generateHurtPose() {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        fillTransparent(img);

        Color bodyColor = new Color(200, 100, 255);
        Color outlineColor = Color.BLACK;

        // Draw body (bent over)
        drawFilledRect(img, 32, 40, 12, 25, bodyColor);
        drawOutlineRect(img, 32, 40, 12, 25, outlineColor);

        // Draw head (lower)
        drawFilledCircle(img, 38, 28, 8, bodyColor);

        // Draw arms (holding sides)
        drawFilledRect(img, 24, 42, 8, 4, bodyColor);
        drawFilledRect(img, 44, 42, 8, 4, bodyColor);

        // Draw legs
        drawFilledRect(img, 33, 65, 4, 15, bodyColor);
        drawFilledRect(img, 43, 65, 4, 15, bodyColor);

        return img;
    }

    private static BufferedImage generateKOPose() {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        fillTransparent(img);

        Color bodyColor = new Color(200, 100, 255);
        Color outlineColor = Color.BLACK;

        // Draw body (lying down)
        drawFilledRect(img, 20, 50, 30, 10, bodyColor);
        drawOutlineRect(img, 20, 50, 30, 10, outlineColor);

        // Draw head (to the side)
        drawFilledCircle(img, 52, 45, 8, bodyColor);

        // Draw arms (splayed)
        drawFilledRect(img, 10, 48, 10, 4, bodyColor);
        drawFilledRect(img, 50, 48, 10, 4, bodyColor);

        // Draw legs (splayed)
        drawFilledRect(img, 20, 60, 4, 15, bodyColor);
        drawFilledRect(img, 46, 60, 4, 15, bodyColor);

        return img;
    }
}

