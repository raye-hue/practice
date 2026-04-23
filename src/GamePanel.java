import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


public class GamePanel extends JPanel implements Runnable{
    
    private java.awt.image.BufferedImage menuBackground;

    enum GameState {
        MENU,
        MODE_SELECT,
        PVP_SELECT,
        PVP_P1_SELECT,
        PVP_P2_SELECT,
        AI_SELECT,
        PLAY,
        ARCADE,
        ARCADE_GAME_OVER,
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
    private final float titleMusicVolumePercent = 30f;
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
    boolean arcadeLadderActive = false;
    GameCharacter arcadeLadderPlayerCharacter = null;
    GameCharacter[] arcadeLadderOpponents = new GameCharacter[0];
    int arcadeLadderOpponentIndex = 0;
    String arcadeGameOverText = "";
    FightManager fightManager = new FightManager(screenWidth, screenHeight);
    public GamePanel(){
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        player.applyCharacter(roster[selectedCharacterIndex]);
        audioManager.setMusicVolumePercent(titleMusicVolumePercent);

         try {
                menuBackground = javax.imageio.ImageIO.read(new java.io.File("assets/backgrounds/GameMenuBG.png"));
            } catch (Exception e) {
                menuBackground = null;
            }
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
                if (arcadeLadderActive) {
                    if (fightManager.didPlayer1Win()) {
                        arcadeLadderOpponentIndex++;
                        if (arcadeLadderOpponentIndex < arcadeLadderOpponents.length) {
                            startNextArcadeLadderFight();
                        } else {
                            endArcadeLadderVictory();
                        }
                    } else {
                        gameState = GameState.ARCADE_GAME_OVER;
                        arcadeGameOverText = "You lost to " + arcadeLadderOpponents[arcadeLadderOpponentIndex].getName() + "!";
                    }
                } else {
                    fightManager.reset();
                    gameState = GameState.MENU;
                    keyH.resetKeys();
                }
            }
            return;
        }

        if (gameState == GameState.ARCADE_GAME_OVER) {
            handleArcadeGameOverInput();
            return;
        }

        if (gameState == GameState.MODE_SELECT) {
            handleModeSelectInput();
            return;
        }

        if(gameState == GameState.AI_SELECT){
            handleAiCharacterSelectInput();
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
            if (arcadeLadderActive) {
                endArcadeLadder();
            } else {
                fightManager.reset();
                gameState = GameState.MENU;
                keyH.resetKeys();
            }
            return true;
        }

        if (gameState == GameState.ARCADE_GAME_OVER) {
            gameState = GameState.ARCADE;
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

        if (gameState == GameState.AI_SELECT) {
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
        if (gameState == GameState.AI_SELECT) {
            drawAiCharacterSelect(g2);
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

        if (gameState == GameState.ARCADE_GAME_OVER) {
            drawArcadeGameOver(g2);
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

       

    }   /*g2.setFont(new Font("Impact", Font.BOLD, 42));
        FontMetrics titleMetrics = g2.getFontMetrics();
        String title = "CHOOSE YOUR MODE";
        int titleX = (screenWidth - titleMetrics.stringWidth(title)) / 2;
        // Title alternative layout
        Font titleFont = new Font("Impact", Font.BOLD, 44);
        g2.setFont(titleFont);
        String title = "SIGMA SHOWDOWN";
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleX = (screenWidth - titleMetrics.stringWidth(title)) / 2;
        int titleY = screenHeight / 4;

        // Red shadow (4 directions)
        g2.setColor(new Color(180, 20, 0));
        g2.drawString(title, titleX + 3, titleY + 3);
        g2.drawString(title, titleX - 3, titleY + 3);
        g2.drawString(title, titleX + 3, titleY - 3);
        g2.drawString(title, titleX - 3, titleY - 3);

        // Gold title
        g2.setColor(new Color(245, 184, 0));
        g2.drawString(title, titleX, titleY);
        */

        private void drawMenu(Graphics2D g2) {
        if (menuBackground != null) {
        g2.drawImage(menuBackground, 0, 0, screenWidth, screenHeight, null);
        } else {
            g2.setColor(Color.black);
            g2.fillRect(0, 0, screenWidth, screenHeight);
        }

        // Semi-transparent overlay so text stays readable
        GradientPaint centerDarken = new GradientPaint(
            screenWidth / 2, 0, new Color(0, 0, 0, 60),
            screenWidth / 2, screenHeight, new Color(0, 0, 0, 180));
    
            g2.setPaint(centerDarken);
            g2.fillRect(0, 0, screenWidth, screenHeight);
            g2.setPaint(null);

        // Title
       Font titleFont = new Font("Impact", Font.BOLD, 60);
        g2.setFont(titleFont);
        String title = "SIGMA SHOWDOWN";
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleX = (screenWidth - titleMetrics.stringWidth(title)) / 2;
        int titleY = screenHeight / 6;

        // Red shadow (4 directions)
        g2.setColor(new Color(180, 20, 0));
        g2.drawString(title, titleX + 3, titleY + 3);
        g2.drawString(title, titleX - 3, titleY + 3);
        g2.drawString(title, titleX + 3, titleY - 3);
        g2.drawString(title, titleX - 3, titleY - 3);

        // Gold title
        g2.setColor(new Color(245, 184, 0));
        g2.drawString(title, titleX, titleY);

        // Menu items
        g2.setFont(new Font("Impact", Font.PLAIN, 28));
        FontMetrics menuMetrics = g2.getFontMetrics();
        int menuStartY = screenHeight / 2 + 45;
        int boxWidth = 220;
        int boxHeight = 44;
        int boxX = (screenWidth - boxWidth) / 2;

        for (int i = 0; i < menuItems.length; i++) {
            int boxY = menuStartY + (i * 55) - 32;

            if (i == menuIndex) {
                g2.setColor(new Color(30, 30, 0));
                g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
                g2.setColor(new Color(255, 215, 0));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
            } else {
                g2.setColor(new Color(15, 15, 15));
                g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
                g2.setColor(new Color(136, 102, 0));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
            }

            String item = menuItems[i];
            int textX = (screenWidth - menuMetrics.stringWidth(item)) / 2;
            int textY = boxY + (boxHeight + menuMetrics.getAscent() - menuMetrics.getDescent()) / 2;
            g2.setColor(i == menuIndex ? new Color(255, 215, 0) : new Color(200, 160, 0));
            g2.drawString(item, textX, textY);
        }

        // Reset stroke back to default after drawing boxes
        g2.setStroke(new BasicStroke(1));

        // Prompt text
        g2.setColor(new Color(255, 215, 0));
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
            endArcadeLadder();
            gameState = GameState.MODE_SELECT;
        } else if (menuIndex == 1) {
            fightManager.reset();
            endArcadeLadder();
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
            gameState = GameState.AI_SELECT;
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
     private void handleAiCharacterSelectInput() {
        if (keyH.menuUpPressed) {
            p1CursorRow = (p1CursorRow - 1 + characterGridRows) % characterGridRows;
            keyH.menuUpPressed = false;
        }
        if (keyH.menuDownPressed) {
            p1CursorRow = (p1CursorRow + 1) % characterGridRows;
            keyH.menuDownPressed = false;
        }
        if (keyH.menuLeftPressed) {
            p1CursorCol = (p1CursorCol - 1 + characterGridCols) % characterGridCols;
            keyH.menuLeftPressed = false;
        }
        if (keyH.menuRightPressed) {
            p1CursorCol = (p1CursorCol + 1) % characterGridCols;
            keyH.menuRightPressed = false;
        }
        if (!keyH.menuSelectPressed) return;

        int selectedIndex = p1CursorRow * characterGridCols + p1CursorCol;
        if (selectedIndex >= roster.length) return;

        player1Character = roster[selectedIndex];

        int aiIndex;
        do {
            aiIndex = (int)(Math.random() * roster.length);
        } while (aiIndex == selectedIndex);
        player2Character = roster[aiIndex];

        fightManager.reset();
        fightManager.startMatch(player1Character, player2Character, "Player vs Computer");
        gameState = GameState.PLAY;
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

        startArcadeLadder(roster[selectedCharacterIndex]);
        keyH.resetKeys();
    }

    private void drawAiCharacterSelect(Graphics2D g2) {
        g2.setColor(Color.black);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        int currentRow = p1CursorRow;
        int currentCol = p1CursorCol;

        g2.setColor(Color.yellow);
        g2.setFont(new Font("Impact", Font.BOLD, 44));
        String title = "SIGMA SHOWDOWN";
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleX = (screenWidth - titleMetrics.stringWidth(title)) / 2;
        g2.drawString(title, titleX, 50);

        g2.setColor(Color.green);
        g2.setFont(new Font("Impact", Font.BOLD, 28));
        String playerText = "CHOOSE YOUR FIGHTER";
        FontMetrics playerMetrics = g2.getFontMetrics();
        int playerX = (screenWidth - playerMetrics.stringWidth(playerText)) / 2;
        g2.drawString(playerText, playerX, 90);

        int boxSize = 110;
        int spacing = 35;
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
                java.awt.image.BufferedImage chrPortrait = chr.getPortrait();
                if (chrPortrait != null) {
                    g2.drawImage(chrPortrait, x, y, x + boxSize, y + boxSize, 0, 0, 90, 90, null);
                } else {
                    g2.setColor(chr.getColor());
                    g2.fillRect(x, y, boxSize, boxSize);
                }

                g2.setColor(Color.white);
                g2.setFont(new Font("Serif", Font.BOLD, 14));
                FontMetrics nameMetrics = g2.getFontMetrics();
                int nameX = x + (boxSize - nameMetrics.stringWidth(chr.getName())) / 2;
                g2.drawString(chr.getName(), nameX, y + boxSize + 20);

                if (row == currentRow && col == currentCol) {
                    g2.setColor(Color.green);
                    g2.setStroke(new BasicStroke(4));
                    g2.drawRect(x - 3, y - 3, boxSize + 6, boxSize + 6);
                }
            }
        }

        int selectedIndex = currentRow * characterGridCols + currentCol;
        if (selectedIndex < roster.length) {
            GameCharacter selected = roster[selectedIndex];
            int panelX = 80, panelY = screenHeight - 175;
            int panelW = screenWidth - 160, panelH = 130;
            g2.setColor(new Color(20, 20, 20));
            g2.fillRoundRect(panelX, panelY, panelW, panelH, 10, 10);
            g2.setColor(new Color(255, 215, 0));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(panelX, panelY, panelW, panelH, 10, 10);
            g2.setStroke(new BasicStroke(1));

            java.awt.image.BufferedImage portrait = selected.getPortrait();
            if (portrait != null) {
                g2.drawImage(portrait, panelX + 12, panelY + 12, panelX + 102, panelY + 102, 0, 0, 90, 90, null);
            } else {
                g2.setColor(selected.getColor());
                g2.fillRoundRect(panelX + 12, panelY + 12, 90, 90, 6, 6);
            }

            g2.setColor(new Color(255, 215, 0));
            g2.setFont(new Font("Impact", Font.PLAIN, 20));
            g2.drawString(selected.getName(), panelX + 115, panelY + 30);
            g2.setColor(new Color(180, 180, 180));
            g2.setFont(new Font("Serif", Font.PLAIN, 13));
            g2.drawString(selected.getRole() + "  |  HP: " + selected.getMaxHp(), panelX + 115, panelY + 52);
            g2.setColor(new Color(210, 210, 210));
            g2.setFont(new Font("Serif", Font.PLAIN, 13));
            drawWrappedText(g2, selected.getBio(), panelX + 115, panelY + 75, panelW - 200, 18);
        }

        g2.setColor(Color.white);
        g2.setFont(new Font("Serif", Font.PLAIN, 16));
        String prompt = "Arrow Keys/WASD to move - Enter/Space to select - Esc/Backspace to go back";
        FontMetrics promptMetrics = g2.getFontMetrics();
        int promptX = (screenWidth - promptMetrics.stringWidth(prompt)) / 2;
        g2.drawString(prompt, promptX, screenHeight - 10);
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

        int boxSize = 110;
        int spacing = 35;
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
                java.awt.image.BufferedImage chrPortrait = chr.getPortrait();
                if (chrPortrait != null) {
                    g2.drawImage(chrPortrait, x, y, x + boxSize, y + boxSize,
                                0, 0, 92, 92, null);
                } else {
                    g2.setColor(chr.getColor());
                    g2.fillRect(x, y, boxSize, boxSize);
}

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

        int panelX = 80, panelY = screenHeight - 175;
        int panelW = screenWidth - 160, panelH = 130;
        g2.setColor(new Color(20, 20, 20));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 10, 10);
        g2.setColor(new Color(255, 215, 0));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 10, 10);
        g2.setStroke(new BasicStroke(1));

        // Image placeholder
        java.awt.image.BufferedImage portrait = selected.getPortrait();
        if (portrait != null) {
            g2.drawImage(portrait, panelX + 12, panelY + 12, panelX + 102, panelY + 102,
                        0, 0, 92, 92, null);
        } else {
            g2.setColor(selected.getColor());
            g2.fillRoundRect(panelX + 12, panelY + 12, 90, 90, 6, 6);
        }
        g2.setColor(new Color(136, 102, 0));
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(panelX + 12, panelY + 12, 90, 90, 6, 6);

        // Character name
        g2.setColor(new Color(255, 215, 0));
        g2.setFont(new Font("Impact", Font.PLAIN, 20));
        g2.drawString(selected.getName(), panelX + 115, panelY + 30);

        // Role + HP
        g2.setColor(new Color(180, 180, 180));
        g2.setFont(new Font("Serif", Font.PLAIN, 13));
        g2.drawString(selected.getRole() + "  |  HP: " + selected.getMaxHp(), panelX + 115, panelY + 52);

        // Bio
        g2.setColor(new Color(210, 210, 210));
        g2.setFont(new Font("Serif", Font.PLAIN, 13));
        drawWrappedText(g2, selected.getBio(), panelX + 115, panelY + 75, panelW - 200, 18);
    }

        g2.setFont(new Font("Serif", Font.PLAIN, 16));
        String prompt = "Arrow Keys/WASD to move - Enter/Space to select - Esc/Backspace to go back";
        FontMetrics promptMetrics = g2.getFontMetrics();
        int promptX = (screenWidth - promptMetrics.stringWidth(prompt)) / 2;
        g2.drawString(prompt, promptX, screenHeight - 10);
    }
    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth, int lineHeight) {
        if (text == null || text.isEmpty()) return;
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            if (fm.stringWidth(word) > maxWidth) {
                if (currentLine.length() > 0) {
                    g2.drawString(currentLine.toString(), x, y);
                    y += lineHeight;
                    currentLine = new StringBuilder();
                }
                g2.drawString(word, x, y);
                y += lineHeight;
                continue;
            }
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (fm.stringWidth(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (currentLine.length() > 0) {
                    g2.drawString(currentLine.toString(), x, y);
                    y += lineHeight;
                }
                currentLine = new StringBuilder(word);
            }
        }
        if (currentLine.length() > 0) {
            g2.drawString(currentLine.toString(), x, y);
        }
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

        // Title - Impact Bold, yellow/gold with red shadow
        g2.setFont(new Font("Impact", Font.BOLD, 42));
        FontMetrics titleMetrics = g2.getFontMetrics();
        String title = "CHOOSE YOUR MODE";
        int titleX = (screenWidth - titleMetrics.stringWidth(title)) / 2;

        // Red shadow
        g2.setColor(new Color(180, 20, 0));
        g2.drawString(title, titleX + 3, 153);
        g2.drawString(title, titleX - 3, 153);
        g2.drawString(title, titleX + 3, 147);
        g2.drawString(title, titleX - 3, 147);

        // Gold title
        g2.setColor(new Color(245, 184, 0));
        g2.drawString(title, titleX, 150);

        // Button dimensions
        g2.setFont(new Font("Impact", Font.BOLD, 28));
        FontMetrics modeMetrics = g2.getFontMetrics();
        
        // Button size adjustments
        int btnWidth = 270;  
        int btnHeight = 50;
        int btnX = (screenWidth - btnWidth) / 2; 
        int startY = 240;
        int gap = 65;

        for (int i = 0; i < playModeItems.length; i++) {
            String item = playModeItems[i];
            int btnY = startY + (i * gap);

         if (i == playModeIndex) {
                g2.setColor(new Color(30, 30, 0));
                g2.fillRoundRect(btnX, btnY, btnWidth, btnHeight, 10, 10);
                g2.setColor(new Color(255, 215, 0));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(btnX, btnY, btnWidth, btnHeight, 10, 10);
            } else {
                g2.setColor(new Color(15, 15, 15));
                g2.fillRoundRect(btnX, btnY, btnWidth, btnHeight, 10, 10);
                g2.setColor(new Color(136, 102, 0));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(btnX, btnY, btnWidth, btnHeight, 10, 10);
            }
            // Center text inside button
            int textX = btnX + (btnWidth - modeMetrics.stringWidth(item)) / 2;
            int textY = btnY + (btnHeight + modeMetrics.getAscent() - modeMetrics.getDescent()) / 2;
            g2.drawString(item, textX, textY);
        }

        // Hint text
        g2.setFont(new Font("Serif", Font.PLAIN, 18));
        g2.setColor(new Color(200, 160, 0));
        String prompt = "W/S or Arrow Keys to move - Enter/Space to select - Esc/Backspace to go back";
        FontMetrics promptMetrics = g2.getFontMetrics();
        int promptX = (screenWidth - promptMetrics.stringWidth(prompt)) / 2;
        g2.drawString(prompt, promptX, screenHeight - 40);
    }

    private void drawArcade(Graphics2D g2) {
        g2.setColor(Color.black);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        GameCharacter selected = roster[selectedCharacterIndex];
        int opponentCount = Math.max(0, roster.length - 1);

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
        String prompt = "W/S to cycle characters - Enter/Space to start ladder - Esc/Backspace to go back";
        FontMetrics promptMetrics = g2.getFontMetrics();
        int promptX = (screenWidth - promptMetrics.stringWidth(prompt)) / 2;
        g2.drawString(prompt, promptX, screenHeight - 40);

        g2.setFont(new Font("Serif", Font.PLAIN, 16));
        String ladderText = "Choose your fighter, then clear the ladder one opponent at a time.";
        FontMetrics ladderMetrics = g2.getFontMetrics();
        int ladderX = (screenWidth - ladderMetrics.stringWidth(ladderText)) / 2;
        g2.drawString(ladderText, ladderX, screenHeight - 58);
    }

    private void startArcadeLadder(GameCharacter playerCharacter) {
        arcadeLadderActive = true;
        arcadeLadderPlayerCharacter = playerCharacter;
        arcadeLadderOpponents = buildArcadeLadderOpponents(playerCharacter);
        arcadeLadderOpponentIndex = 0;
        selectedPlayMode = "Player vs Computer";
        startNextArcadeLadderFight();
    }

    private GameCharacter[] buildArcadeLadderOpponents(GameCharacter playerCharacter) {
        ArrayList<GameCharacter> opponents = new ArrayList<>();
        for (GameCharacter character : roster) {
            if (character != playerCharacter) {
                opponents.add(character);
            }
        }
        return opponents.toArray(new GameCharacter[0]);
    }

    private void startNextArcadeLadderFight() {
        if (arcadeLadderOpponentIndex >= arcadeLadderOpponents.length) {
            endArcadeLadder();
            return;
        }

        GameCharacter opponent = arcadeLadderOpponents[arcadeLadderOpponentIndex];
        fightManager.reset();
        fightManager.setLadderProgress(arcadeLadderOpponentIndex + 1, arcadeLadderOpponents.length);
        fightManager.startMatch(arcadeLadderPlayerCharacter, opponent, selectedPlayMode);
        gameState = GameState.PLAY;
    }

    private void endArcadeLadder() {
        arcadeLadderActive = false;
        arcadeLadderPlayerCharacter = null;
        arcadeLadderOpponents = new GameCharacter[0];
        arcadeLadderOpponentIndex = 0;
        fightManager.reset();
        gameState = GameState.MENU;
        keyH.resetKeys();
    }

    private void endArcadeLadderVictory() {
        arcadeLadderActive = false;
        arcadeLadderPlayerCharacter = null;
        arcadeLadderOpponents = new GameCharacter[0];
        arcadeLadderOpponentIndex = 0;
        fightManager.reset();
        gameState = GameState.MENU;
        keyH.resetKeys();
    }

    private void handleArcadeGameOverInput() {
        if (keyH.menuSelectPressed) {
            fightManager.reset();
            arcadeLadderOpponentIndex = 0;
            startNextArcadeLadderFight();
            keyH.resetKeys();
        } else if (keyH.menuBackPressed) {
            endArcadeLadder();
        }
    }

    private void drawArcadeGameOver(Graphics2D g2) {
        g2.setColor(Color.black);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.red);
        g2.setFont(new Font("Impact", Font.BOLD, 60));
        String gameOver = "GAME OVER";
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleX = (screenWidth - titleMetrics.stringWidth(gameOver)) / 2;
        g2.drawString(gameOver, titleX, screenHeight / 3);

        g2.setColor(Color.white);
        g2.setFont(new Font("Serif", Font.PLAIN, 32));
        FontMetrics messageMetrics = g2.getFontMetrics();
        int messageX = (screenWidth - messageMetrics.stringWidth(arcadeGameOverText)) / 2;
        g2.drawString(arcadeGameOverText, messageX, screenHeight / 2 - 30);

        g2.setFont(new Font("Serif", Font.PLAIN, 24));
        String retryText = "Press ENTER to try again";
        FontMetrics retryMetrics = g2.getFontMetrics();
        int retryX = (screenWidth - retryMetrics.stringWidth(retryText)) / 2;
        g2.drawString(retryText, retryX, screenHeight / 2 + 40);

        String exitText = "Press ESC to exit";
        FontMetrics exitMetrics = g2.getFontMetrics();
        int exitX = (screenWidth - exitMetrics.stringWidth(exitText)) / 2;
        g2.drawString(exitText, exitX, screenHeight / 2 + 80);
    }
}
