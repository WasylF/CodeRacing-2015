
import model.Car;
import model.Game;
import model.Move;
import model.TileType;
import model.World;

/**
 *
 * @author Wsl_F
 */
public abstract class StrategyWslF {

    Car self;
    World world;
    Game game;
    Move move;

    //constants below
    /**
     * константа для отображения на матрице тайла собственной машины
     */
    public static final int selfCar = -1;
    /**
     * константа для отображения на матрице тайла машины сокомандника
     */
    public static final int teammateCar = -2;
    /**
     * константа для отображения на матрице тайла машины соперников
     */
    public static final int opponentCar = -10;
    /**
     * константа для отображения на матрице тайла пустой точки
     */
    public static final int empty = 0;
    /**
     * константа для отображения на матрице тайла ограждения/стен
     */
    public static final int wall = -100;
    /**
     * ширина машины (140)
     */
    public static final int carWidth = 140;
    /**
     * высота машины (210)
     */
    public static final int carHeight = 210;
    /**
     * время начала заезда
     */
    public static final int startTick = 180;
//end constants

    /**
     * convert tile to matrix 800x800 with margin 80
     */
    protected TileToMatrix tileToMatrix;
    /**
     * convert tile to matrix 80x80 with margin 8
     */
    protected TileToMatrix tileToMatrix80;

    /**
     * карта тайлов
     */
    protected TileType[][] mapTiles;
    /**
     * абсцисса текущего тайла на карте тайлов
     */
    protected int curTileX;
    /**
     * ордината текущего тайла на карте тайлов
     */
    protected int curTileY;
    /**
     * абсцисса центра машины относительно текущего тайла
     */
    protected int selfX;
    /**
     * ордината центра машина относительно текущего тайла
     */
    protected int selfY;
    /**
     * длинна/ширина тайла
     */
    protected int tileSize;
    /**
     * радиус закругления
     */
    protected int marginSize;
    /**
     * матрица 800х800 которая отображает состаяние текущего тайла [0][0] -
     * левый верхний угол [tileSize-1][tileSize-1] - правый нижний угол
     */
    protected int[][] curTile;

    public void move(Car self, World world, Game game, Move move) {
        initAll(self, world, game, move);
        move();
    }

    protected abstract void move();

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
        //carWidth = 140;//(int) (self.getWidth() + 0.1);
        //carHeight = 210;//(int) (self.getHeight() + 0.1);
    }

    protected int getColorOfCar(Car car) {
        if (car.getPlayerId() == world.getMyPlayer().getId()) {
            if (car.equals(self)) {
                return selfCar;
            } else {
                return teammateCar;
            }
        } else {
            return opponentCar;
        }
    }
}
