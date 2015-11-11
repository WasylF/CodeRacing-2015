
import model.Car;
import model.Game;
import model.Move;
import model.TileType;
import model.World;

/**
 *
 * @author Wsl_F
 */
public class StrategyWslF {

    //constants below
    /**
     * константа для отображения на матрице тайла собственной машины
     */
    public static final int selfCar = -1;
    /**
     * константа для отображения на матрице тайла пустой точки
     */
    public static final int empty = 0;
    /**
     * константа для отображения на матрице тайла ограждения/стен
     */
    public static final int wall = -100;
//end constants

    /**
     * convert tile to matrix 800x800 with margin 80
     */
    TileToMatrix tileToMatrix;
    /**
     * convert tile to matrix 80x80 with margin 8
     */
    TileToMatrix tileToMatrix80;

    Car self;
    World world;
    Game game;
    Move move;
    /**
     * карта тайлов
     */
    TileType[][] mapTiles;
    /**
     * абсцисса текущего тайла на карте тайлов
     */
    int curTileX;
    /**
     * ордината текущего тайла на карте тайлов
     */
    int curTileY;
    /**
     * абсцисса центра машины относительно текущего тайла
     */
    int selfX;
    /**
     * ордината центра машина относительно текущего тайла
     */
    int selfY;
    /**
     * длинна/ширина тайла
     */
    int tileSize;
    /**
     * радиус закругления
     */
    int marginSize;
    /**
     * матрица 800х800 которая отображает состаяние текущего тайла [0][0] -
     * левый верхний угол [tileSize-1][tileSize-1] - правый нижний угол
     */
    int[][] curTile;
    /**
     * ширина машины (140)
     */
    int carWidth;
    /**
     * высота машины (210)
     */
    int carHeight;

    public void move(Car self, World world, Game game, Move move) {
    }

    protected void initAll(Car self, World world, Game game, Move move) {
        this.self = self;
        this.world = world;
        this.game = game;
        this.move = move;

        tileToMatrix = new TileToMatrix(world, game, move, self);
        tileToMatrix80 = new TileToMatrix(world, game, move, self, 80, 8);

        /*
         {
         //int[][] curTile = tileToMatrix.getMatrix(TileType.LEFT_TOP_CORNER);
         int[][] curTile = tileToMatrix80.getMatrix(TileType.CROSSROADS);
         for (int i= 0; i<80; i++)
         {
         for (int j= 0; j<80; j++)
         {
         System.out.print(curTile[i][j]);
         }
         System.out.println();
         }
         }*/
        mapTiles = world.getTilesXY();
        tileSize = (int) (game.getTrackTileSize() + 0.1);
        marginSize = (int) (game.getTrackTileMargin() + 0.1);
        curTileX = (int) (self.getX() / game.getTrackTileSize());
        curTileY = (int) (self.getY() / game.getTrackTileSize());
        selfX = (int) (self.getX()) % tileSize;
        selfY = (int) (self.getY()) % tileSize;
        carWidth = 140;//(int) (self.getWidth() + 0.1);
        carHeight = 210;//(int) (self.getHeight() + 0.1);
    }
}
