package world;

import java.awt.image.BufferedImage;

public class Tile {
    public BufferedImage image;
    private boolean isWall;
    private int x;
    private int y;
    private int size;

    public Tile(boolean isWall) {
        this.isWall = isWall;
    }

    public Tile() {
        this.isWall = false;
    }

    public boolean getIsWall() {
        return this.isWall;
    }

    public void setIsWall(boolean isWall) {
        this.isWall = isWall;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return this.x;
    }

    public void setY(int y) { this.y = y; }

    public int getY() {
        return this.y;
    }

    public int getX2() {
        return this.x + this.size;
    }

    public int getY2() {
        return this.y + this.size;
    }

    public Collision getCollision(int x, int y, int width, int height) {
        if(!this.getIsWall()) {
            return new Collision();
        }

        int x2 = x + width;
        int y2 = y + height;
        if(getX2() < x || getX() > x2 || getY2() < y || getY() > y2) {
            return new Collision();
        }

        Collision collision = new Collision();
        collision.collided = true;

        int overlapLeft = x2 - getX();   // Player's right edge past Tile's left
        int overlapRight = getX2() - x;  // Tile's right edge past Player's left
        int overlapTop = y2 - getY();    // Player's bottom edge past Tile's top
        int overlapBottom = getY2() - y; // Tile's bottom edge past Player's top

        int min = Math.min(Math.min(Math.min(overlapLeft, overlapRight), overlapTop), overlapBottom);

        if(min == overlapLeft) {
            collision.left = getX() - width - 1;
        } else if(min == overlapRight) {
            collision.right = getX2() + 1;
        } else if (min == overlapTop) {
            collision.top = getY() - height - 1;
        } else {
            collision.bottom = getY2() + 1;
        }

        return collision;
    }



    public String toString() {
        return "x:" + getX() + ", y:"+ getY();
    }
}
