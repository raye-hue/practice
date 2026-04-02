import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

public class KeyHandler implements KeyListener{

    public boolean upPressed, downPressed, leftPressed, rightPressed;
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        
        int code = e.getKeyCode();

        if(code == KeyEvent.VK_W || code == 38){
            upPressed = true;
        }
         if(code == KeyEvent.VK_S || code == 40){
            downPressed = true;
        }
         if(code == KeyEvent.VK_A || code == 37){
            leftPressed = true;
        }
         if(code == KeyEvent.VK_D || code == 39){
            rightPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        int code = e.getKeyCode();

        if(code == KeyEvent.VK_W || code == 38){
            upPressed = false;
        }
         if(code == KeyEvent.VK_S || code == 40){
            downPressed = false;
        }
         if(code == KeyEvent.VK_A || code == 37){
            leftPressed = false;
        }
         if(code == KeyEvent.VK_D || code == 39){
            rightPressed = false;
        }
    }
    
}
