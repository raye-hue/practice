package core;

import battle.CharacterRoster;
import battle.FightManager;
import battle.GameCharacter;
import java.awt.Color;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import world.TileManager;


public class GamePanel extends JPanel implements Runnable{
    
    private java.awt.image.BufferedImage menuBackground;

    enum GameState {
        MENU,
        MODE_SELECT,
        PVP_SELECT,
        PVP_P1_SELECT,
        PVP_P2_SELECT,
        AI_SELECT,
        AI_DIFFICULTY,
        MAP_SELECT,
        PLAY,
        ARCADE,
        ARCADE_GAME_OVER,
        PAUSE,
        CREDITS
    }

    final int originalTileSize = 26;
    final int scale = 2;

    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 18;
    public final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    KeyHandler keyH = new KeyHandler();
    Player player = new Player(keyH, this);
    TileManager tileM = new TileManager(this);
    Thread gameThread;
    GameCharacter[] roster = CharacterRoster.createDefaultRoster();
    int selectedCharacterIndex = 0;

    GameState gameState = GameState.MENU;
    int menuIndex = 0;
    String[] menuItems = {"Play", "Arcade", "Credits", "Quit"};
    int playModeIndex = 0;
    String[] playModeItems = {"Player vs Player", "Player vs Computer"};
    String selectedPlayMode = "Player vs Player";
    int difficultyIndex = 0;
    String[] difficultyItems = {"Easy", "Medium", "Hard"};
    FightManager.AIDifficulty selectedDifficulty = FightManager.AIDifficulty.MEDIUM;
    private final AudioManager audioManager = new AudioManager();
    private final String titleMusicPath = "assets/music/title.wav";
    private final float titleMusicVolumePercent = 30f;
    private final String fightMusicPath = "assets/music/fightbg.wav";
    private final float fightMusicVolumePercent = 18f;
        private final String[] mapPaths = new String[]{
            "assets/backgrounds/Battleground.jpg",
            "assets/backgrounds/GameBG.jpg"
        };
            private final String[] mapNames = new String[]{
                "Ohio Outskirts",
                "Edge-Lord's Alley"
            };
        private java.awt.image.BufferedImage[] mapPreviews = null;
        private int selectedMapIndex = 0;

        // Pending match info while selecting a map
        private GameCharacter pendingP1 = null;
        private GameCharacter pendingP2 = null;
        private String pendingPlayMode = null;
        private boolean pendingArcade = false;
        private int pendingArcadeIndex = 0;
        private int pendingArcadeTotal = 0;
    private GameState previousGameState = null;
        private final String[] battleMapPaths = {
            "assets/backgrounds/Battleground.jpg",
            "assets/backgrounds/GameBG.jpg",
            "assets/backgrounds/bgpbackrooms.png"
        };
        private final String[] battleMapNames = {
            "The Ohio Outskirts",
            "Edge-Lord's Alley",
            "The Backrooms"
        };
        private java.awt.image.BufferedImage[] battleMapPreviews = null;
        private int battleMapIndex = 0;
        private GameState battleMapReturnState = GameState.MENU;
        private GameCharacter pendingFightP1 = null;
        private GameCharacter pendingFightP2 = null;
        private String pendingFightMode = "Player vs Player";
        private boolean pendingFightIsLadder = false;
        private int pendingFightLadderIndex = 0;
        private int pendingFightLadderTotal = 0;
    private float creditsScrollY = 0;
    private float[] starX, starY, starSpeed;
    private boolean creditsInitialized = false;
    private int creditsPauseTimer = 0;
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
                menuBackground = javax.imageio.ImageIO.read(new java.io.File("assets/backgrounds/MenuBackground.png"));
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
            if (keyH.pausePressed) {
                fightManager.togglePause();
                keyH.pausePressed = false;
            }
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

        if (gameState == GameState.MAP_SELECT) {
            handleMapSelectInput();
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

        if (gameState == GameState.AI_DIFFICULTY){
            handleAiDifficultyInput();
            return;
        }

        if (gameState == GameState.MAP_SELECT) {
            handleMapSelectInput();
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

       if (gameState == GameState.CREDITS) {
            if (!creditsInitialized) initCredits();

            // Stars always keep moving
            for (int i = 0; i < 120; i++) {
                starY[i] += starSpeed[i];
                if (starY[i] > screenHeight) {
                    starY[i] = 0;
                    starX[i] = (float)(Math.random() * screenWidth);
                }
            }

            // If pausing at the end, count down then restart
            if (creditsPauseTimer > 0) {
                creditsPauseTimer--;
                if (creditsPauseTimer == 0) {
                    creditsScrollY = screenHeight; // restart
                }
                return; // stop here, don't scroll while pausing
            }

            // Only scroll if not pausing
            creditsScrollY -= 0.8f;
            return;
        }

    }

    private void updateMenuMusic() {
        if (previousGameState == gameState) {
            return;
        }

        if (gameState == GameState.MENU) {
            audioManager.setMusicVolumePercent(titleMusicVolumePercent);
            audioManager.playMusicLoop(titleMusicPath);
        } else if (gameState == GameState.PLAY) {
            audioManager.setMusicVolumePercent(fightMusicVolumePercent);
            audioManager.playMusicLoop(fightMusicPath);
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
            // Disabled: Esc/Backspace no longer immediately exits during a match.
            // Use P to pause, then choose "Exit Match" from the pause menu.
            return false;
        }

        if (gameState == GameState.ARCADE_GAME_OVER) {
            gameState = GameState.ARCADE;
            keyH.resetKeys();
            return true;
        }

        if (gameState == GameState.MODE_SELECT || gameState == GameState.ARCADE || gameState == GameState.CREDITS || gameState == GameState.PAUSE) {
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

        if (gameState == GameState.AI_DIFFICULTY) {
            gameState = GameState.AI_SELECT;
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

        if (gameState == GameState.AI_DIFFICULTY){
            drawAiDifficulty(g2);
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
        
        if (gameState == GameState.CREDITS) {
            drawCredits(g2);
            g2.dispose();
            return;
        }
        

        if (gameState == GameState.PLAY) {
            fightManager.draw(g2);
            g2.dispose();
            return;
        }

        if (gameState == GameState.MAP_SELECT) {
            drawMapSelect(g2);
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
            creditsInitialized = false;
            gameState = GameState.CREDITS;
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
            openMapSelect(player1Character, player2Character, selectedPlayMode, false, 0, 0, GameState.PVP_P2_SELECT);
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
        gameState = GameState.AI_DIFFICULTY;
        keyH.resetKeys();
    }
    private void handleAiDifficultyInput() {
        if (keyH.menuUpPressed) {
            difficultyIndex = (difficultyIndex - 1 + difficultyItems.length) % difficultyItems.length;
            keyH.menuUpPressed = false;
        }
        if (keyH.menuDownPressed) {
            difficultyIndex = (difficultyIndex + 1) % difficultyItems.length;
            keyH.menuDownPressed = false;
        }

        if (!keyH.menuSelectPressed) return;

        switch (difficultyIndex) {
            case 0 -> selectedDifficulty = FightManager.AIDifficulty.EASY;
            case 1 -> selectedDifficulty = FightManager.AIDifficulty.MEDIUM;
            case 2 -> selectedDifficulty = FightManager.AIDifficulty.HARD;
        }

        fightManager.reset();
        fightManager.setAIDifficulty(selectedDifficulty);
        openMapSelect(player1Character, player2Character, "Player vs Computer", false, 0, 0, GameState.AI_DIFFICULTY);
        keyH.resetKeys();
    }
        

    private void handleCharacterSelectInput() {
        if (keyH.menuUpPressed) {
            selectedCharacterIndex = (selectedCharacterIndex - characterGridCols + roster.length) % roster.length;
            keyH.menuUpPressed = false;
        }
        if (keyH.menuDownPressed) {
            selectedCharacterIndex = (selectedCharacterIndex + characterGridCols) % roster.length;
            keyH.menuDownPressed = false;
        }
        if (keyH.menuLeftPressed) {
            selectedCharacterIndex = (selectedCharacterIndex - 1 + roster.length) % roster.length;
            keyH.menuLeftPressed = false;
        }
        if (keyH.menuRightPressed) {
            selectedCharacterIndex = (selectedCharacterIndex + 1) % roster.length;
            keyH.menuRightPressed = false;
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
    private void drawAiDifficulty(Graphics2D g2) {
        g2.setColor(Color.black);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Title
        g2.setFont(new Font("Impact", Font.BOLD, 42));
        g2.setColor(new Color(180, 20, 0));
        String title = "SELECT DIFFICULTY";
        FontMetrics tm = g2.getFontMetrics();
        int titleX = (screenWidth - tm.stringWidth(title)) / 2;
        g2.drawString(title, titleX + 3, 153);
        g2.drawString(title, titleX - 3, 153);
        g2.setColor(new Color(245, 184, 0));
        g2.drawString(title, titleX, 150);

        int btnWidth = 270;
        int btnHeight = 50;
        int btnX = (screenWidth - btnWidth) / 2;
        int startY = 240;
        int gap = 65;

        g2.setFont(new Font("Impact", Font.BOLD, 28));
        FontMetrics bm = g2.getFontMetrics();


        for (int i = 0; i < difficultyItems.length; i++) {
            int btnY = startY + (i * gap);

            if (i == difficultyIndex) {
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

            String item = difficultyItems[i];
            int textX = btnX + (btnWidth - bm.stringWidth(item)) / 2;
            int textY = btnY + (btnHeight + bm.getAscent() - bm.getDescent()) / 2;
            g2.setColor(i == difficultyIndex ? new Color(255, 215, 0) : new Color(200, 160, 0));
            g2.drawString(item, textX, textY);

          
        }

        g2.setStroke(new BasicStroke(1));
        g2.setColor(new Color(200, 160, 0));
        g2.setFont(new Font("Serif", Font.PLAIN, 18));
        String prompt = "W/S or Arrow Keys to move - Enter/Space to select - Esc/Backspace to go back";
        FontMetrics pm = g2.getFontMetrics();
        g2.drawString(prompt, (screenWidth - pm.stringWidth(prompt)) / 2, screenHeight - 40);
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

    private void drawMapSelect(Graphics2D g2) {
        g2.setColor(Color.black);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        if (battleMapPreviews == null) {
            loadBattleMapPreviews();
        }

        g2.setFont(new Font("Impact", Font.BOLD, 44));
        g2.setColor(Color.yellow);
        String title = "CHOOSE A MAP";
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleX = (screenWidth - titleMetrics.stringWidth(title)) / 2;
        g2.drawString(title, titleX, 56);

        g2.setFont(new Font("Serif", Font.PLAIN, 18));
        g2.setColor(new Color(220, 220, 220));
        String subtitle = "Left / Right to change, Enter to confirm, Esc to go back";
        FontMetrics subtitleMetrics = g2.getFontMetrics();
        int subtitleX = (screenWidth - subtitleMetrics.stringWidth(subtitle)) / 2;
        g2.drawString(subtitle, subtitleX, 84);

        int cardWidth = 220;
        int cardHeight = 150;
        int gap = 36;
        int totalWidth = (battleMapPaths.length * cardWidth) + ((battleMapPaths.length - 1) * gap);
        int startX = (screenWidth - totalWidth) / 2;
        int cardY = 120;

        for (int i = 0; i < battleMapPaths.length; i++) {
            int cardX = startX + i * (cardWidth + gap);

            g2.setColor(new Color(18, 18, 18));
            g2.fillRoundRect(cardX - 6, cardY - 6, cardWidth + 12, cardHeight + 62, 14, 14);

            if (i == battleMapIndex) {
                g2.setColor(new Color(255, 215, 0));
                g2.setStroke(new BasicStroke(4));
            } else {
                g2.setColor(new Color(90, 90, 90));
                g2.setStroke(new BasicStroke(2));
            }
            g2.drawRoundRect(cardX - 6, cardY - 6, cardWidth + 12, cardHeight + 62, 14, 14);
            g2.setStroke(new BasicStroke(1));

            java.awt.image.BufferedImage preview = battleMapPreviews[i];
            if (preview != null) {
                g2.drawImage(preview, cardX, cardY, cardWidth, cardHeight, null);
            } else {
                g2.setColor(new Color(50, 50, 50));
                g2.fillRect(cardX, cardY, cardWidth, cardHeight);
                g2.setColor(Color.white);
                g2.drawString("Preview unavailable", cardX + 38, cardY + 76);
            }

            g2.setFont(new Font("Impact", Font.PLAIN, 24));
            g2.setColor(i == battleMapIndex ? new Color(255, 215, 0) : Color.white);
            String mapName = i < battleMapNames.length ? battleMapNames[i] : battleMapPaths[i];
            FontMetrics mapMetrics = g2.getFontMetrics();
            g2.drawString(mapName, cardX + (cardWidth - mapMetrics.stringWidth(mapName)) / 2, cardY + cardHeight + 34);
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

        private void initCredits() {
            creditsScrollY = screenHeight;
            creditsPauseTimer = 0;
            starX = new float[120];
            starY = new float[120];
            starSpeed = new float[120];
            for (int i = 0; i < 120; i++) {
                starX[i] = (float)(Math.random() * screenWidth);
                starY[i] = (float)(Math.random() * screenHeight);
                starSpeed[i] = (float)(Math.random() * 1.5f + 0.3f);
            }
            creditsInitialized = true;
        }

        private void drawCredits(Graphics2D g2) {

             if (starSpeed == null) {
                initCredits();
             }
            g2.setColor(Color.black);
            g2.fillRect(0, 0, screenWidth, screenHeight);

            for (int i = 0; i < 120; i++) {
                float speed = starSpeed[i];
                int size = speed > 1.2f ? 3 : speed > 0.7f ? 2 : 1;
                int brightness = speed > 1.2f ? 255 : speed > 0.7f ? 180 : 110;
                g2.setColor(new Color(brightness, brightness, brightness));
                g2.fillRect((int)starX[i], (int)starY[i], size, size);
            }

            g2.setClip(0, 60, screenWidth, screenHeight - 100);

            float y = creditsScrollY;

            g2.setFont(new Font("Impact", Font.BOLD, 58));
            g2.setColor(new Color(180, 20, 0));
            String title = "CREDITS";
            FontMetrics tm = g2.getFontMetrics();
            int titleX = (screenWidth - tm.stringWidth(title)) / 2;
            g2.drawString(title, titleX + 3, (int)y + 3);
            g2.setColor(new Color(245, 184, 0));
            g2.drawString(title, titleX, (int)y);
            y += 80;

            g2.setColor(new Color(245, 184, 0));
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(screenWidth / 4, (int)y, screenWidth * 3 / 4, (int)y);
            y += 30;

            y = drawCreditSection(g2, "TEAM", new String[][]{
                {"Lawrance Isaac Lapig",      "Project Manager / Backend Developer"},
                {"Cedric Villareal",      "Backend Developer"},
                {"Xander Maurice Ortiz",   "Graphics Artist"},
                {"Chirsten Raye Rizal",     "Graphics Artist"},
            }, y);

            y += 20;
            g2.setColor(new Color(245, 184, 0));
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(screenWidth / 4, (int)y, screenWidth * 3 / 4, (int)y);
            y += 30;

            y = drawCreditSection(g2, "TOOLS & RESOURCES", new String[][]{
                {"Ezgif",   "Sprite Cutter"},
                {"Picsart", "Background Remover"},
                {"Gemini",  "Sprite Generated Images"},
                {"Chat GPT", "Animated Background Generator"},
                {"VS Code", "IDE"},
                {"Discord", "Communication"},
            }, y);

            y += 20;
            g2.setColor(new Color(245, 184, 0));
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(screenWidth / 4, (int)y, screenWidth * 3 / 4, (int)y);
            y += 30;

            y = drawCreditSection(g2, "SPECIAL THANKS", new String[][]{
                {"To Sir Khai", "Thank you for your guidance and patience throughout this journey."},
                {"To Our Parents & Family",  "Our greatest supporters, in every way that matters."},
                {"To Lawrence & Cedric",  "Thank you for turning countless hours of code into an epic fighting experience."},
                {"To Xander & Chirsten",  "Thank you for crafting the visuals that made every character unforgettable."}
            }, y);


            y += 40;
            g2.setColor(new Color(245, 184, 0));
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(screenWidth / 4, (int)y, screenWidth * 3 / 4, (int)y);
            y += 50;

            g2.setFont(new Font("Impact", Font.BOLD, 32));
            g2.setColor(new Color(245, 184, 0));
            String thanks = "Thank you for playing!";
            FontMetrics thm = g2.getFontMetrics();
            g2.drawString(thanks, (screenWidth - thm.stringWidth(thanks)) / 2, (int)y);
            y += 40;

            g2.setFont(new Font("Serif", Font.ITALIC, 20));
            g2.setColor(Color.white);
            String ph = "Made with teamwork and perseverance";
            FontMetrics phm = g2.getFontMetrics();
            g2.drawString(ph, (screenWidth - phm.stringWidth(ph)) / 2, (int)y);
            y += 30;

            g2.setFont(new Font("Serif", Font.PLAIN, 14));
            g2.setColor(new Color(150, 150, 150));
            String ver = "Sigma Showdown v1.0";
            FontMetrics vm = g2.getFontMetrics();
            g2.drawString(ver, (screenWidth - vm.stringWidth(ver)) / 2, (int)y);

            g2.setClip(null);

            if (creditsScrollY < -(y + 150) && creditsPauseTimer == 0) {
                creditsPauseTimer = 60 * 3; // 3 seconds at 60fps
            }

            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, screenHeight - 50, screenWidth, 50);
            g2.setColor(new Color(200, 160, 0));
            g2.setFont(new Font("Serif", Font.PLAIN, 16));
            String prompt = "Esc/Backspace to go back";
            FontMetrics pm = g2.getFontMetrics();
            g2.drawString(prompt, (screenWidth - pm.stringWidth(prompt)) / 2, screenHeight - 18);
        }

        private float drawCreditSection(Graphics2D g2, String sectionTitle, String[][] entries, float y) {
            g2.setFont(new Font("Impact", Font.BOLD, 24));
            g2.setColor(new Color(255, 140, 0));
            FontMetrics sm = g2.getFontMetrics();
            g2.drawString(sectionTitle, (screenWidth - sm.stringWidth(sectionTitle)) / 2, (int)y);
            y += 35;

            for (String[] entry : entries) {
                g2.setFont(new Font("Impact", Font.PLAIN, 20));
                g2.setColor(new Color(245, 184, 0));
                FontMetrics nm = g2.getFontMetrics();
                g2.drawString(entry[0], (screenWidth - nm.stringWidth(entry[0])) / 2, (int)y);
                y += 22;

                g2.setFont(new Font("Serif", Font.ITALIC, 15));
                g2.setColor(new Color(200, 200, 200));
                FontMetrics rm = g2.getFontMetrics();
                g2.drawString(entry[1], (screenWidth - rm.stringWidth(entry[1])) / 2, (int)y);
                y += 35;
            }
            return y;
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

        // Title
        g2.setColor(Color.yellow);
        g2.setFont(new Font("Impact", Font.BOLD, 44));
        String title = "ARCADE - CHARACTER SELECT";
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleX = (screenWidth - titleMetrics.stringWidth(title)) / 2;
        g2.drawString(title, titleX, 50);

        g2.setColor(new Color(255, 140, 0));
        g2.setFont(new Font("Impact", Font.BOLD, 24));
        String sub = "CHOOSE YOUR FIGHTER";
        FontMetrics subMetrics = g2.getFontMetrics();
        g2.drawString(sub, (screenWidth - subMetrics.stringWidth(sub)) / 2, 85);

        // Character grid
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

                // Highlight selected
                if (index == selectedCharacterIndex) {
                    g2.setColor(new Color(255, 140, 0));
                    g2.setStroke(new BasicStroke(4));
                    g2.drawRect(x - 3, y - 3, boxSize + 6, boxSize + 6);
                    g2.setStroke(new BasicStroke(1));
                }
            }
        }

        // Character info panel
        GameCharacter selected = roster[selectedCharacterIndex];
        int panelX = 80, panelY = screenHeight - 175;
        int panelW = screenWidth - 160, panelH = 130;
        g2.setColor(new Color(20, 20, 20));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 10, 10);
        g2.setColor(new Color(255, 140, 0));
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

        g2.setColor(new Color(255, 140, 0));
        g2.setFont(new Font("Impact", Font.PLAIN, 20));
        g2.drawString(selected.getName(), panelX + 115, panelY + 30);
        g2.setColor(new Color(180, 180, 180));
        g2.setFont(new Font("Serif", Font.PLAIN, 13));
        g2.drawString(selected.getRole() + "  |  HP: " + selected.getMaxHp(), panelX + 115, panelY + 52);
        g2.setColor(new Color(210, 210, 210));
        g2.setFont(new Font("Serif", Font.PLAIN, 13));
        drawWrappedText(g2, selected.getBio(), panelX + 115, panelY + 75, panelW - 200, 18);

        // Prompt
        g2.setColor(Color.white);
        g2.setFont(new Font("Serif", Font.PLAIN, 16));
        String prompt = "Arrow Keys/WASD to move - Enter/Space to start ladder - Esc/Backspace to go back";
        FontMetrics promptMetrics = g2.getFontMetrics();
        g2.drawString(prompt, (screenWidth - promptMetrics.stringWidth(prompt)) / 2, screenHeight - 10);
        }

        private void startArcadeLadder(GameCharacter playerCharacter) {
            arcadeLadderActive = true;
            arcadeLadderPlayerCharacter = playerCharacter;
            arcadeLadderOpponents = buildArcadeLadderOpponents(playerCharacter);
            arcadeLadderOpponentIndex = 0;
            selectedPlayMode = "Player vs Computer";
            // Start first fight directly without map select
            if (arcadeLadderOpponents.length > 0) {
                startNextArcadeLadderFight();
            }
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
            startFightDirectly(
                    arcadeLadderPlayerCharacter,
                    opponent,
                    selectedPlayMode,
                    true,
                    arcadeLadderOpponentIndex + 1,
                    arcadeLadderOpponents.length
            );
        }

        private void openMapSelect(GameCharacter p1, GameCharacter p2, String playMode, boolean isLadder, int ladderIndex, int ladderTotal, GameState returnState) {
            pendingFightP1 = p1;
            pendingFightP2 = p2;
            pendingFightMode = playMode;
            pendingFightIsLadder = isLadder;
            pendingFightLadderIndex = ladderIndex;
            pendingFightLadderTotal = ladderTotal;
            battleMapReturnState = returnState;
            battleMapIndex = 0;
            gameState = GameState.MAP_SELECT;
        }

        private void handleMapSelectInput() {
            if (battleMapPreviews == null) {
                loadBattleMapPreviews();
            }

            if (keyH.menuLeftPressed) {
                battleMapIndex = (battleMapIndex - 1 + battleMapPaths.length) % battleMapPaths.length;
                keyH.menuLeftPressed = false;
            }
            if (keyH.menuRightPressed) {
                battleMapIndex = (battleMapIndex + 1) % battleMapPaths.length;
                keyH.menuRightPressed = false;
            }

            if (keyH.menuBackPressed) {
                cancelMapSelect();
                keyH.resetKeys();
                return;
            }

            if (!keyH.menuSelectPressed) {
                return;
            }

            startPendingFightWithSelectedMap();
            keyH.resetKeys();
        }

        private void cancelMapSelect() {
            pendingFightP1 = null;
            pendingFightP2 = null;
            pendingFightMode = "Player vs Player";
            pendingFightIsLadder = false;
            pendingFightLadderIndex = 0;
            pendingFightLadderTotal = 0;
            gameState = battleMapReturnState;
        }

        private void startPendingFightWithSelectedMap() {
            if (pendingFightP1 == null || pendingFightP2 == null) {
                gameState = GameState.MENU;
                return;
            }

            fightManager.reset();
            fightManager.setBattleBackgroundPath(battleMapPaths[battleMapIndex]);
            fightManager.setAIDifficulty(selectedDifficulty);
            if (pendingFightIsLadder) {
                fightManager.setLadderProgress(pendingFightLadderIndex, pendingFightLadderTotal);
            }
            fightManager.startMatch(pendingFightP1, pendingFightP2, pendingFightMode);

            pendingFightP1 = null;
            pendingFightP2 = null;
            pendingFightMode = "Player vs Player";
            pendingFightIsLadder = false;
            pendingFightLadderIndex = 0;
            pendingFightLadderTotal = 0;
            gameState = GameState.PLAY;
        }

        private void startFightDirectly(GameCharacter p1, GameCharacter p2, String playMode, boolean isLadder, int ladderIndex, int ladderTotal) {
            fightManager.reset();
            if (isLadder) {
                fightManager.setBattleBackgroundPath("assets/backgrounds/arcadebg.png");
                fightManager.setLadderProgress(ladderIndex, ladderTotal);
            } else {
                fightManager.setBattleBackgroundPath(battleMapPaths[0]);
            }
            fightManager.setAIDifficulty(selectedDifficulty);
            fightManager.startMatch(p1, p2, playMode);
            gameState = GameState.PLAY;
        }

        private void loadBattleMapPreviews() {
            battleMapPreviews = new java.awt.image.BufferedImage[battleMapPaths.length];
            for (int i = 0; i < battleMapPaths.length; i++) {
                try {
                    battleMapPreviews[i] = javax.imageio.ImageIO.read(new java.io.File(battleMapPaths[i]));
                } catch (Exception e) {
                    battleMapPreviews[i] = null;
                }
            }
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

    private void loadMapPreviews() {
        mapPreviews = new java.awt.image.BufferedImage[mapPaths.length];
        for (int i = 0; i < mapPaths.length; i++) {
            try {
                mapPreviews[i] = javax.imageio.ImageIO.read(new java.io.File(mapPaths[i]));
            } catch (Exception e) {
                mapPreviews[i] = null;
            }
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

