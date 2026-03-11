import java.awt.Graphics2D;
import java.awt.Color;

public class Player {

    int x;
    int y;
    int speed;

    KeyHandler keyH;
    GamePanel gp;

    public Player(KeyHandler keyH, GamePanel gp){

        this.keyH = keyH;
        this.gp = gp;

        x = 100;
        y = 100;
        speed = 4;
    }

    public void update(){



        if(keyH.upPressed){
            y -= speed;
        }
        if(keyH.downPressed){
            y += speed;
        }
        if(keyH.leftPressed){
            x -= speed;
        }
        if(keyH.rightPressed){
            x += speed;
        }



    }

    public void draw(Graphics2D g2){

        g2.setColor(Color.white);
        g2.fillRect(x, y, 48, 48);

    }

}