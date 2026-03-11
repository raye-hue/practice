import java.awt.Graphics2D;
import java.awt.Color;

public class TileManager {

    GamePanel gp;
    Tile[] tile;
    int[][] mapTileNum;

    public TileManager(GamePanel gp){
        this.gp = gp;
        tile = new Tile[2];
        mapTileNum = new int[gp.maxScreenCol][gp.maxScreenRow];
        getTileImage();
        loadMap();
    }

    public void getTileImage(){
        tile[0] = new Tile();
        tile[0].collision = false;

        tile[1] = new Tile();
        tile[1].collision = true;
    }

    public void loadMap(){
        for(int row = 0; row < gp.maxScreenRow; row++){
            for(int col = 0; col < gp.maxScreenCol; col++){
                if(row == 0 || row == gp.maxScreenRow-1 || col == 0 || col == gp.maxScreenCol-1){
                    mapTileNum[col][row] = 1;
                } else {
                    mapTileNum[col][row] = 0;
                }
            }
        }

    }

    public void draw(Graphics2D g2){
        for(int row = 0; row < gp.maxScreenRow; row++){
            for(int col = 0; col < gp.maxScreenCol; col++){
                int tileNum = mapTileNum[col][row];
                int x = col * gp.tileSize;
                int y = row * gp.tileSize;

                if(tileNum == 0){
                    g2.setColor(Color.GREEN);
                } else {
                    g2.setColor(Color.GRAY);
                }

                g2.fillRect(x, y, gp.tileSize, gp.tileSize);
            }
        }
    }



}