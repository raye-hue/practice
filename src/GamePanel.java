import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable{

    enum GameState {
        MENU,
        MODE_SELECT,
        PVP_SELECT,
        PVP_P1_SELECT,
        PVP_P2_SELECT,
        PLAY,
        ARCADE,
        PAUSE
    }

    final int originalTileSize = 26;
    final int scale = 2;

    final int tileSize = originalTileSize * scale;
    final int maxScreenCol = 16;
    final int maxScreenRow = 12;
    final int screenWidth = tileSize * maxScreenCol;
    final int screenHeight = tileSize * maxScreenRow;

    KeyHandler keyH = new KeyHandler();
    Player player = new Player(keyH, this);
    TileManager tileM = new TileManager(this);
    Thread gameThread;
    GameCharacter[] roster = CharacterRoster.createDefaultRoster();
    int selectedCharacterIndex = 0;

    GameState gameState = GameState.MENU;
    int menuIndex = 0;
    String[] menuItems = {"Play", "Arcade", "Options", "Quit"};
    int playModeIndex = 0;
    String[] playModeItems = {"Player vs Player", "Player vs Computer"};
    String selectedPlayMode = "Player vs Player";
    private final AudioManager audioManager = new AudioManager();
    private final String titleMusicPath = "assets/music/title.wav";
    private GameState previousGameState = null;
    int playerX = 100;
    int playerY = 100;
    int playerSpeed = 4;
    int pvpSelectStep = 0;
    int p1CharacterIndex = 0;
    int p2CharacterIndex = 0;
    GameCharacter player1Character = null;
    GameCharacter player2Character = null;
    int p1CursorRow = 0, p1CursorCol = 0;
    int p2CursorRow = 0, p2CursorCol = 0;
    int characterGridCols = 4;
    int characterGridRows = 2;
    FightManager fightManager = new FightManager(screenWidth, screenHeight);
    public GamePanel(){
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        player.applyCharacter(roster[selectedCharacterIndex]);


    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    public void startGameThread(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    public TileManager getTileManager() {
        return this.tileM;
    }

    @Override
    public void run() {
        this.tileM.loadMap();

        double drawInterval = 1000000000/60;
        double nextDrawTime = System.nanoTime() + drawInterval;
        while(gameThread != null){
            //System.out.println("the game loop is running");
            update();
            repaint();

            try{
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = remainingTime/1000000;

                if(remainingTime < 0){
                    remainingTime = 0;
                }

                Thread.sleep((long)remainingTime);

                nextDrawTime += drawInterval;
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
    public void update(){
        updateMenuMusic();

        if (handleBackInput()) {
            return;
        }

        if (gameState == GameState.MENU) {
            handleMenuInput();
            return;
        }

        if (gameState == GameState.PLAY) {
            fightManager.update(keyH);
            if (fightManager.consumeReturnToMenuRequest()) {
                fightManager.reset();
                gameState = GameState.MENU;
                keyH.resetKeys();
            }
            return;
        }

        if (gameState == GameState.MODE_SELECT) {
            handleModeSelectInput();
            return;
        }

        if (gameState == GameState.PVP_P1_SELECT || gameState == GameState.PVP_P2_SELECT) {
            handlePvpCharacterSelectInput();
            return;
        }

        if (gameState == GameState.ARCADE) {
            handleCharacterSelectInput();
            return;
        }

        if (gameState == GameState.PAUSE && keyH.menuSelectPressed) {
            gameState = GameState.PLAY;
            keyH.resetKeys();
        }
    }

    private void updateMenuMusic() {
        if (previousGameState == gameState) {
            return;
        }

        if (gameState == GameState.MENU) {
            audioManager.playMusicLoop(titleMusicPath);
        } else {
            audioManager.stopMusic();
        }

        previousGameState = gameState;
    }

    private boolean handleBackInput() {
        if (!keyH.menuBackPressed) {
            return false;
        }

        if (gameState == GameState.PLAY) {
            fightManager.reset();
            gameState = GameState.MENU;
            keyH.resetKeys();
            return true;
        }

        if (gameState == GameState.MODE_SELECT || gameState == GameState.ARCADE || gameState == GameState.PAUSE) {
            gameState = GameState.MENU;
            keyH.resetKeys();
            return true;
        }

        if (gameState == GameState.PVP_P1_SELECT) {
            gameState = GameState.MODE_SELECT;
            keyH.resetKeys();
            return true;
        }

        if (gameState == GameState.PVP_P2_SELECT) {
            gameState = GameState.PVP_P1_SELECT;
            keyH.resetKeys();
            return true;
        }

        return false;
    }



    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(Color.white);

        if (gameState == GameState.MENU) {
            drawMenu(g2);
            g2.dispose();
            return;
        }

        if (gameState == GameState.MODE_SELECT) {
            drawModeSelect(g2);
            g2.dispose();
            return;
        }

        if (gameState == GameState.PVP_P1_SELECT || gameState == GameState.PVP_P2_SELECT) {
            drawPvpCharacterSelect(g2);
            g2.dispose();
            return;
        }

        if (gameState == GameState.PAUSE) {
            drawPause(g2);
            g2.dispose();
            return;
        }

        if (gameState == GameState.ARCADE) {
            drawArcade(g2);
            g2.dispose();
            return;
        }

        if (gameState == GameState.PLAY) {
            fightManager.draw(g2);
            g2.dispose();
            return;
        }

        tileM.draw(g2);
        player.draw(g2);

        g2.setColor(Color.white);
        g2.setFont(new Font("Serif", Font.PLAIN, 18));
        g2.drawString("Mode: " + selectedPlayMode, 16, 28);

        g2.dispose();

       

    }

    private void drawMenu(Graphics2D g2) {
        g2.setColor(Color.black);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.white);
        Font titleFont = new Font("Impact", Font.BOLD, 44);
        g2.setFont(titleFont);
        String title = "SIGMA SHOWDOWN";
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleX = (screenWidth - titleMetrics.stringWidth(title)) / 2;
        int titleY = screenHeight / 4;
        g2.drawString(title, titleX, titleY);

        g2.setFont(new Font("Serif", Font.PLAIN, 28));
        FontMetrics menuMetrics = g2.getFontMetrics();
        int menuStartY = screenHeight / 2;

        for (int i = 0; i < menuItems.length; i++) {
            String prefix = i == menuIndex ? "> " : "  ";
            String item = prefix + menuItems[i];
            int itemX = (screenWidth - menuMetrics.stringWidth(item)) / 2;
            g2.drawString(item, itemX, menuStartY + (i * 45));
        }

        g2.setFont(new Font("Serif", Font.PLAIN, 18));
        String text = "W/S or Arrow Keys to move - Enter/Space to select";
        FontMetrics promptMetrics = g2.getFontMetrics();
        int promptX = (screenWidth - promptMetrics.stringWidth(text)) / 2;
        g2.drawString(text, promptX, screenHeight - 40);
    }

    private void handleMenuInput() {
        if (keyH.menuUpPressed) {
            menuIndex = (menuIndex - 1 + menuItems.length) % menuItems.length;
            keyH.menuUpPressed = false;
        }

        if (keyH.menuDownPressed) {
            menuIndex = (menuIndex + 1) % menuItems.length;
            keyH.menuDownPressed = false;
        }

        if (!keyH.menuSelectPressed) {
            return;
        }

        if (menuIndex == 0) {
            fightManager.reset();
            gameState = GameState.MODE_SELECT;
        } else if (menuIndex == 1) {
            fightManager.reset();
            gameState = GameState.ARCADE;
        } else if (menuIndex == 2) {
            gameState = GameState.PAUSE;
        } else if (menuIndex == 3) {
            audioManager.stopMusic();
            System.exit(0);
        }

        keyH.resetKeys();
    }

    private void handleModeSelectInput() {
        if (keyH.menuUpPressed) {
            playModeIndex = (playModeIndex - 1 + playModeItems.length) % playModeItems.length;
            keyH.menuUpPressed = false;
        }

        if (keyH.menuDownPressed) {
            playModeIndex = (playModeIndex + 1) % playModeItems.length;
            keyH.menuDownPressed = false;
        }

        if (!keyH.menuSelectPressed) {
            return;
        }

        selectedPlayMode = playModeItems[playModeIndex];
        if (selectedPlayMode.equals("Player vs Player")) {
            fightManager.reset();
            gameState = GameState.PVP_P1_SELECT;
            pvpSelectStep = 0;
            p1CharacterIndex = 0;
            p2CharacterIndex = 0;
            player1Character = null;
            player2Character = null;
        } else {
            fightManager.reset();
            player1Character = roster[0];
            player2Character = roster[Math.min(1, roster.length - 1)];
            fightManager.startMatch(player1Character, player2Character, selectedPlayMode);
            gameState = GameState.PLAY;
        }
        keyH.resetKeys();
    }

    private void handlePvpCharacterSelectInput() {
        int currentRow = (gameState == GameState.PVP_P1_SELECT) ? p1CursorRow : p2CursorRow;
        int currentCol = (gameState == GameState.PVP_P1_SELECT) ? p1CursorCol : p2CursorCol;
        
        if (keyH.menuUpPressed) {
            currentRow = (currentRow - 1 + characterGridRows) % characterGridRows;
            keyH.menuUpPressed = false;
        }

        if (keyH.menuDownPressed) {
            currentRow = (currentRow + 1) % characterGridRows;
            keyH.menuDownPressed = false;
        }

        if (keyH.menuLeftPressed) {
            currentCol = (currentCol - 1 + characterGridCols) % characterGridCols;
            keyH.menuLeftPressed = false;
        }

        if (keyH.menuRightPressed) {
            currentCol = (currentCol + 1) % characterGridCols;
            keyH.menuRightPressed = false;
        }

        if (!keyH.menuSelectPressed) {
            if (gameState == GameState.PVP_P1_SELECT) {
                p1CursorRow = currentRow;
                p1CursorCol = currentCol;
            } else {
                p2CursorRow = currentRow;
                p2CursorCol = currentCol;
            }
            return;
        }

        int selectedIndex = currentRow * characterGridCols + currentCol;
        if (selectedIndex >= roster.length) {
            return;
        }

        if (gameState == GameState.PVP_P1_SELECT) {
            player1Character = roster[selectedIndex];
            gameState = GameState.PVP_P2_SELECT;
        } else {
            player2Character = roster[selectedIndex];
            fightManager.reset();
            fightManager.startMatch(player1Character, player2Character, selectedPlayMode);
            gameState = GameState.PLAY;
        }
        keyH.resetKeys();
    }

    private void handleCharacterSelectInput() {
        if (keyH.menuUpPressed) {
            selectedCharacterIndex = (selectedCharacterIndex - 1 + roster.length) % roster.length;
            keyH.menuUpPressed = false;
        }

        if (keyH.menuDownPressed) {
            selectedCharacterIndex = (selectedCharacterIndex + 1) % roster.length;
            keyH.menuDownPressed = false;
        }

        if (!keyH.menuSelectPressed) {
            return;
        }

        player.applyCharacter(roster[selectedCharacterIndex]);
        gameState = GameState.MENU;
        keyH.resetKeys();
    }

    private void drawPvpCharacterSelect(Graphics2D g2) {
        g2.setColor(Color.black);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        boolean isPlayer1 = (gameState == GameState.PVP_P1_SELECT);
        int currentRow = isPlayer1 ? p1CursorRow : p2CursorRow;
        int currentCol = isPlayer1 ? p1CursorCol : p2CursorCol;

        g2.setColor(Color.yellow);
        g2.setFont(new Font("Impact", Font.BOLD, 44));
        String title = "SIGMA SHOWDOWN";
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleX = (screenWidth - titleMetrics.stringWidth(title)) / 2;
        g2.drawString(title, titleX, 50);

        g2.setColor(isPlayer1 ? Color.blue : Color.red);
        g2.setFont(new Font("Impact", Font.BOLD, 28));
        String playerText = isPlayer1 ? "PLAYER 1 - CHOOSE YOUR FIGHTER" : "PLAYER 2 - CHOOSE YOUR FIGHTER";
        FontMetrics playerMetrics = g2.getFontMetrics();
        int playerX = (screenWidth - playerMetrics.stringWidth(playerText)) / 2;
        g2.drawString(playerText, playerX, 90);

        int boxSize = 90;
        int spacing = 20;
        int totalWidth = characterGridCols * boxSize + (characterGridCols - 1) * spacing;
        int totalHeight = characterGridRows * boxSize + (characterGridRows - 1) * spacing;
        int startX = (screenWidth - totalWidth) / 2;
        int startY = (screenHeight - totalHeight) / 2 - 50;

        for (int row = 0; row < characterGridRows; row++) {
            for (int col = 0; col < characterGridCols; col++) {
                int index = row * characterGridCols + col;
                if (index >= roster.length) continue;

                int x = startX + col * (boxSize + spacing);
                int y = startY + row * (boxSize + spacing);

                GameCharacter chr = roster[index];
                g2.setColor(chr.getColor());
                g2.fillRect(x, y, boxSize, boxSize);

                g2.setColor(Color.white);
                g2.setFont(new Font("Serif", Font.BOLD, 14));
                FontMetrics nameMetrics = g2.getFontMetrics();
                int nameX = x + (boxSize - nameMetrics.stringWidth(chr.getName())) / 2;
                g2.drawString(chr.getName(), nameX, y + boxSize + 20);

                if (isPlayer1 && row == currentRow && col == currentCol) {
                    g2.setColor(Color.green);
                    g2.setStroke(new BasicStroke(4));
                    g2.drawRect(x - 3, y - 3, boxSize + 6, boxSize + 6);
                } else if (!isPlayer1 && row == currentRow && col == currentCol) {
                    g2.setColor(Color.orange);
                    g2.setStroke(new BasicStroke(4));
                    g2.drawRect(x - 3, y - 3, boxSize + 6, boxSize + 6);
                }
            }
        }

        int selectedIndex = currentRow * characterGridCols + currentCol;
        if (selectedIndex < roster.length) {
            GameCharacter selected = roster[selectedIndex];
            g2.setColor(Color.white);
            g2.setFont(new Font("Serif", Font.BOLD, 24));
            String selectedText = "Selected: " + selected.getName();
            FontMetrics selectedMetrics = g2.getFontMetrics();
            int selectedX = (screenWidth - selectedMetrics.stringWidth(selectedText)) / 2;
            g2.drawString(selectedText, selectedX, screenHeight - 80);
            g2.setFont(new Font("Serif", Font.PLAIN, 18));
            String statsText = selected.getRole() + " | HP: " + selected.getMaxHp();
            FontMetrics statsMetrics = g2.getFontMetrics();
            int statsX = (screenWidth - statsMetrics.stringWidth(statsText)) / 2;
            g2.drawString(statsText, statsX, screenHeight - 50);
        }

        g2.setFont(new Font("Serif", Font.PLAIN, 16));
        String prompt = "Arrow Keys/WASD to move - Enter/Space to select - Esc/Backspace to go back";
        FontMetrics promptMetrics = g2.getFontMetrics();
        int promptX = (screenWidth - promptMetrics.stringWidth(prompt)) / 2;
        g2.drawString(prompt, promptX, screenHeight - 20);
    }

    private void drawPause(Graphics2D g2) {
        g2.setColor(Color.black);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.white);
        g2.setFont(new Font("Serif", Font.BOLD, 40));
        g2.drawString("OPTIONS", 220, 220);

        g2.setFont(new Font("Serif", Font.PLAIN, 22));
        g2.drawString("Placeholder screen", 210, 280);
        g2.drawString("Enter/Space to resume match", 165, 330);
        g2.drawString("Esc/Backspace to return to menu", 150, 365);
    }

    private void drawModeSelect(Graphics2D g2) {
        g2.setColor(Color.black);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.white);
        g2.setFont(new Font("Impact", Font.BOLD, 42));
        String title = "CHOOSE YOUR MODE";
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleX = (screenWidth - titleMetrics.stringWidth(title)) / 2;
        g2.drawString(title, titleX, 150);

        g2.setFont(new Font("Serif", Font.PLAIN, 30));
        FontMetrics modeMetrics = g2.getFontMetrics();
        int startY = 270;
        for (int i = 0; i < playModeItems.length; i++) {
            String prefix = i == playModeIndex ? "> " : "  ";
            String item = prefix + playModeItems[i];
            int itemX = (screenWidth - modeMetrics.stringWidth(item)) / 2;
            g2.drawString(item, itemX, startY + (i * 55));
        }

        g2.setFont(new Font("Serif", Font.PLAIN, 18));
        String prompt = "W/S or Arrow Keys to move - Enter/Space to select - Esc/Backspace to go back";
        FontMetrics promptMetrics = g2.getFontMetrics();
        int promptX = (screenWidth - promptMetrics.stringWidth(prompt)) / 2;
        g2.drawString(prompt, promptX, screenHeight - 40);
    }

    private void drawArcade(Graphics2D g2) {
        g2.setColor(Color.black);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        GameCharacter selected = roster[selectedCharacterIndex];

        g2.setColor(Color.white);
        g2.setFont(new Font("Impact", Font.BOLD, 40));
        String title = "CHARACTER SELECT";
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleX = (screenWidth - titleMetrics.stringWidth(title)) / 2;
        g2.drawString(title, titleX, 120);

        g2.setColor(selected.getColor());
        g2.fillRect(90, 155, 90, 90);

        g2.setColor(Color.white);
        g2.setFont(new Font("Serif", Font.BOLD, 30));
        g2.drawString(selected.getName(), 210, 195);

        g2.setFont(new Font("Serif", Font.PLAIN, 22));
        g2.drawString("Role: " + selected.getRole(), 210, 230);
        g2.drawString("HP: " + selected.getMaxHp(), 210, 240);

        g2.setFont(new Font("Serif", Font.PLAIN, 18));
        String prompt = "W/S to cycle characters - Enter/Space to confirm - Esc/Backspace to go back";
        FontMetrics promptMetrics = g2.getFontMetrics();
        int promptX = (screenWidth - promptMetrics.stringWidth(prompt)) / 2;
        g2.drawString(prompt, promptX, screenHeight - 40);
    }
}
