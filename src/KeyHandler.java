import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

public class KeyHandler implements KeyListener{

    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean p1LeftPressed, p1RightPressed, p1JumpPressed, p1AttackPressed;
    public boolean p2LeftPressed, p2RightPressed, p2JumpPressed, p2AttackPressed;
    public boolean menuUpPressed, menuDownPressed, menuLeftPressed, menuRightPressed, menuSelectPressed;
    public boolean menuBackPressed;
    public boolean anyKeyPressed;
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        
        int code = e.getKeyCode();
        anyKeyPressed = true;

        if(code == KeyEvent.VK_W || code == KeyEvent.VK_UP){
            upPressed = true;
            menuUpPressed = true;
        }
         if(code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN){
            downPressed = true;
            menuDownPressed = true;
        }
         if(code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT){
            leftPressed = true;
            menuLeftPressed = true;
        }
         if(code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT){
            rightPressed = true;
            menuRightPressed = true;
        }
         if(code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE){
            menuSelectPressed = true;
         }
         if(code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_BACK_SPACE){
            menuBackPressed = true;
         }

        if(code == KeyEvent.VK_A){
            p1LeftPressed = true;
        }
        if(code == KeyEvent.VK_D){
            p1RightPressed = true;
        }
        if(code == KeyEvent.VK_W){
            p1JumpPressed = true;
        }
        if(code == KeyEvent.VK_F){
            p1AttackPressed = true;
        }

        if(code == KeyEvent.VK_LEFT){
            p2LeftPressed = true;
        }
        if(code == KeyEvent.VK_RIGHT){
            p2RightPressed = true;
        }
        if(code == KeyEvent.VK_UP){
            p2JumpPressed = true;
        }
        if(code == KeyEvent.VK_L){
            p2AttackPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        int code = e.getKeyCode();

        if(code == KeyEvent.VK_W || code == KeyEvent.VK_UP){
            upPressed = false;
        }
         if(code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN){
            downPressed = false;
        }
         if(code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT){
            leftPressed = false;
            menuLeftPressed = false;
        }
         if(code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT){
            rightPressed = false;
            menuRightPressed = false;
        }
        if(code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_BACK_SPACE){
            menuBackPressed = false;
        }

        if(code == KeyEvent.VK_A){
            p1LeftPressed = false;
        }
        if(code == KeyEvent.VK_D){
            p1RightPressed = false;
        }
        if(code == KeyEvent.VK_W){
            p1JumpPressed = false;
        }
        if(code == KeyEvent.VK_F){
            p1AttackPressed = false;
        }

        if(code == KeyEvent.VK_LEFT){
            p2LeftPressed = false;
        }
        if(code == KeyEvent.VK_RIGHT){
            p2RightPressed = false;
        }
        if(code == KeyEvent.VK_UP){
            p2JumpPressed = false;
        }
        if(code == KeyEvent.VK_L){
            p2AttackPressed = false;
        }
    }

    public void resetKeys() {
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
        p1LeftPressed = false;
        p1RightPressed = false;
        p1JumpPressed = false;
        p1AttackPressed = false;
        p2LeftPressed = false;
        p2RightPressed = false;
        p2JumpPressed = false;
        p2AttackPressed = false;
        menuUpPressed = false;
        menuDownPressed = false;
        menuLeftPressed = false;
        menuRightPressed = false;
        menuSelectPressed = false;
        menuBackPressed = false;
        anyKeyPressed = false;
    }
    
}
