package world;

import core.GamePanel;
import java.awt.Graphics2D;
import java.awt.Color;


public class TileManager {

    GamePanel gp;
    Tile[][] mapTileNum;

    public TileManager(GamePanel gp){
        this.gp = gp;
        mapTileNum = new Tile[gp.maxScreenCol][gp.maxScreenRow];
    }

    public void loadMap(){
        for(int row = 0; row < gp.maxScreenRow; row++){
            for(int col = 0; col < gp.maxScreenCol; col++){
                Tile tile = new Tile();
                tile.setX(col * gp.tileSize);
                tile.setY(row * gp.tileSize);
                tile.setSize(gp.tileSize);
                if(row == 0 || row == gp.maxScreenRow-1 || col == 0 || col == gp.maxScreenCol-1){
                    tile.setIsWall(true);
                    mapTileNum[col][row] = tile;
                } else {
                    mapTileNum[col][row] = tile;
                }
            }
        }

    }

    public Collision getCollision(int x, int y, int width, int height) {

        for(int row = 0; row < gp.maxScreenRow; row++){
            for(int col = 0; col < gp.maxScreenCol; col++){
                Tile tile = mapTileNum[col][row];
                Collision collision = tile.getCollision(x, y, width, height);
                if(collision.collided) {
                    return collision;
                }
            }
        }

        return new Collision();
    }

    public void draw(Graphics2D g2){
        for(int row = 0; row < gp.maxScreenRow; row++){
            for(int col = 0; col < gp.maxScreenCol; col++){
                Tile tile = mapTileNum[col][row];
                if(tile.getIsWall()){
                    g2.setColor(Color.GREEN);
                } else {
                    g2.setColor(Color.BLUE);
                }

                g2.fillRect(tile.getX(), tile.getY(), gp.tileSize, gp.tileSize);
            }
        }
    }



}
