package core;

import battle.GameCharacter;
import java.awt.Graphics2D;
import java.awt.Color;
import world.Collision;

public class Player {

    int x;
    int y;
    int width;
    int height;
    int speed;
    Color color;

    KeyHandler keyH;
    GamePanel gp;

    public Player(KeyHandler keyH, GamePanel gp){

        this.keyH = keyH;
        this.gp = gp;

        x = 100;
        y = 100;
        width = 48;
        height = 48;
        speed = 10;
        color = Color.white;
    }

    public void applyCharacter(GameCharacter character) {
        if (character == null) {
            return;
        }

        color = character.getColor();
        speed = Math.max(6, character.getBaseAttack() / 2);
    }

    public void update(){
        int x = this.x;
        int y = this.y;

        if(keyH.leftPressed){
            x -= speed;
        } else if(keyH.rightPressed){
            x += speed;
        }

        if(keyH.upPressed){
            y -= speed;
        } else if(keyH.downPressed){
            y += speed;
        }

        Collision collision = gp.getTileManager().getCollision(x, y, width, height);

        if(collision.collided) {
            if(collision.left > -1) {
                this.x = collision.left;
            } else if(collision.right > -1) {
                this.x = collision.right;
            } else if(collision.top > -1) {
                this.y = collision.top;
            } else if(collision.bottom > -1) {
                this.y = collision.bottom;
            }
            return;
        }

        this.x = x;
        this.y = y;
    }

    public void draw(Graphics2D g2){

        g2.setColor(color);
        g2.fillRect(x, y, width, height);

    }

}
