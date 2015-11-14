
import java.io.FileWriter;
import java.io.IOException;
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
     * константа для отображения на матрице тайла ключевой точки
     */
    public static final int wayPoint = -50;
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
    /**
     * размер тайла в общей карте трека
     */
    public static final int worldTileSize = 80;
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
    /**
     * карта всей трассы [0][0] - левый верхний угол
     * [tileSize*n-1][tileSize*m-1] - правый нижний угол
     */
    protected int[][] worldMap;
    /**
     * ширина карты в тайлах
     */
    protected int worldWidth;
    /**
     * высота карты в тайлах
     */
    protected int worldHeight;

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
        worldHeight = mapTiles.length;
        worldWidth = mapTiles[0].length;
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

    /**
     * строит карту всей трассы
     */
    protected void calculateWorldMap() {
        worldMap = new int[worldHeight * worldTileSize][worldWidth * worldTileSize];
        TileToMatrix worldTile = new TileToMatrix(world, game, move, self, worldTileSize, worldTileSize / 10);
        int[][] tileMatrix;
        for (int i = 0; i < worldHeight; i++) {
            for (int j = 0; j < worldWidth; j++) {
                tileMatrix = worldTile.getMatrix(mapTiles[i][j]);
                //worldTile.printCurTileToFile("tile "+i+" "+j+" .txt");
                for (int q = 0; q < worldTileSize; q++) {
                    System.arraycopy(tileMatrix[q], 0, worldMap[j * worldTileSize + q], i * worldTileSize, worldTileSize);
                }
            }
        }

        int[][] wayPoints = world.getWaypoints();
        for (int[] wayPoint1 : wayPoints) {
            int x = wayPoint1[0] * worldTileSize + worldTileSize / 2;
            int y = wayPoint1[1] * worldTileSize + worldTileSize / 2;
            for (int delta1 = -worldTileSize / 7; delta1 < worldTileSize / 7; delta1++) {
                for (int delta2 = -worldTileSize / 7; delta2 < worldTileSize / 7; delta2++) {
                    worldMap[y + delta1][x + delta2] = wayPoint;
                }
            }
        }
    }

    /**
     * печатает в файл worldMap.txt схематическое изображение текущего тайла
     */
    protected void printWorldMapToFile() {
        try (FileWriter writer = new FileWriter("WorldMap.txt", false)) {
            for (int x = 0; x < worldHeight * worldTileSize; x++) {
                String s = "";
                for (int y = 0; y < worldWidth * worldTileSize; y++) {
                    switch (worldMap[x][y]) {
                        case selfCar:
                            s += '.';
                            break;
                        case wall:
                            s += '▓';
                            break;
                        case empty:
                            s += ' ';
                            break;
                        case wayPoint:
                            s += '@';
                            break;
                    }
                }

                writer.write(s + "\r\n");
            }
        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }
    }

    /**
     * задает значение val в матрице карты, с относительными координатами (х;у)
     *
     * @param x абсцисса
     * @param y ордината
     * @param val значение
     */
    public boolean setCurWorldMap(int x, int y, int val) {
        if (x < 0 || y < 0
                || x >= worldTileSize * worldHeight
                || y >= worldTileSize * worldWidth) {
            return false;
        }
        worldMap[y][x] = val;
        return true;
    }

    /**
     * возвращает значение в матрице карты, с относительными координатами (х;у)
     *
     * @param x абсцисса
     * @param y ордината
     * @return значение в текущем тайле (стена/пусто/своя машина/...)
     */
    protected int getCurWorldMap(int x, int y) {
        if (x < 0 || y < 0
                || x >= worldTileSize * worldHeight
                || y >= worldTileSize * worldWidth) {
            return wall;
        }
        return worldMap[y][x];
    }

}
