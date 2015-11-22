
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.StrictMath.abs;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
    /**
     * размер закругления в общей карте трека
     */
    public static final int worldMarginSize = worldTileSize / 10;
//end constants

    /**
     * длинна/ширина тайла
     */
    protected int tileSize;
    /**
     * радиус закругления
     */
    protected int marginSize;

    /**
     * ширина карты в тайлах
     */
    protected int worldWidth;
    /**
     * высота карты в тайлах
     */
    protected int worldHeight;
    /**
     * максимум среди ширины и высоты карты
     */
    protected int worldHW;

    /**
     * карта тайлов
     */
    protected TileType[][] mapTiles;
    /**
     * карта всей трассы [0][0] - левый верхний угол
     * [tileSize*n-1][tileSize*m-1] - правый нижний угол
     */
    protected int[][] worldMap;

    /**
     * массив ключевых тайлов.
     */
    protected PairIntInt[] systemWayPoints;
    /*
    
     * nextTile[x][y] - список следующих тайлов, если сейчас нахожишься в тайле
     * [x][y]. nextTile[x][y][i] - следующий тайл, который нужно посетить если
     * индекс ключевого тайла посещенного последним - i
     
     protected PairIntInt nextTile[][][];
     */

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
    protected int relativeX;
    /**
     * ордината центра машина относительно текущего тайла
     */
    protected int relativeY;

    /**
     * матрица 800х800 которая отображает состаяние текущего тайла [0][0] -
     * левый верхний угол [tileSize-1][tileSize-1] - правый нижний угол
     */
    protected int[][] curTile;

    /**
     * скорость на предыдущем ходу
     */
    protected Vector previousSpeed;
    /**
     * скорость на текущем ходу
     */
    protected Vector curSpeed;
    /**
     * копия предыдущего хода
     */
    /**
     * вектор направления корпуса машины
     */
    protected Vector carDirection;
    protected Move previousMove;

    /**
     * объект для операций с матрицей всей карты
     */
    protected WorldMapHelper worldMapHelper;
    /**
     * объект для операций с графом из тайлов.
     */
    protected WorldGraphHelper worldGraphHelper;
    /**
     * объект для работы с текущим тайлом
     */
    protected TileHelper tileHelper;
    /**
     * объект для нахождений расстояний от собственной машины до стен. +++
     * планируется добавить расстояние до машин опонентов
     */
    protected DistanceHelper distanceHelper;

    public void move(Car self, World world, Game game, Move move) {
        initAll(self, world, game, move);
        move();
    }

    protected abstract void move();

    protected void initFirst(Car self, World world, Game game, Move move) {
        int[][] tWayPoints = world.getWaypoints();
        systemWayPoints = new PairIntInt[tWayPoints.length * 3];
        for (int i = 0; i < tWayPoints.length; i++) {
            systemWayPoints[i] = new PairIntInt(tWayPoints[i][0], tWayPoints[i][1]);
            systemWayPoints[i + tWayPoints.length] = new PairIntInt(tWayPoints[i][0], tWayPoints[i][1]);
            systemWayPoints[i + 2 * tWayPoints.length] = new PairIntInt(tWayPoints[i][0], tWayPoints[i][1]);
        }

        mapTiles = world.getTilesXY();
        worldHeight = mapTiles.length;
        worldWidth = mapTiles[0].length;
        worldHW = Math.max(worldHeight, worldWidth) + 1;

        tileSize = (int) (game.getTrackTileSize() + 0.1);
        marginSize = (int) (game.getTrackTileMargin() + 0.1);

        tileHelper = new TileHelper(self, world, game, move, this);
        worldMapHelper = new WorldMapHelper(worldHeight, worldWidth, self, world, game, move, this);
        worldGraphHelper = new WorldGraphHelper(this, worldWidth, worldHeight, worldHW);
        distanceHelper = new DistanceHelper(tileSize, marginSize, worldWidth, worldHeight, worldHW, this, worldGraphHelper, worldMapHelper, tileHelper);

        worldMap = worldMapHelper.calculateWorldMap(worldTileSize);
    }

    protected void initAll(Car self, World world, Game game, Move move) {
        this.self = self;
        this.world = world;
        this.game = game;
        this.move = move;

        if (world.getTick() < 2) {
            initFirst(self, world, game, move);
        }

        curTileX = (int) (self.getX() / game.getTrackTileSize());
        curTileY = (int) (self.getY() / game.getTrackTileSize());
        relativeX = (int) (self.getX()) % tileSize;
        relativeY = (int) (self.getY()) % tileSize;
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
     *
     * @param ar входной двумерный массив
     * @return копия двумерного массива
     */
    public int[][] get2DArrayCopy(int[][] ar) {
        int[][] copy = new int[ar.length][ar[0].length];

        for (int i = 0; i < ar.length; i++) {
            System.arraycopy(ar[i], 0, copy[i], 0, ar[0].length);
        }

        return copy;
    }

    /**
     * вычисление тайла, в который будем направляться. Метод работает обратным
     * ходом, то есть мы движемся от следующего ключевого до стартового
     * (текущего)
     *
     * @param sTileX абсцисса следующего ключевого тайла
     * @param sTileY ордината следующего ключевого тайла
     * @param fTileX абсцисса текущего тайла
     * @param fTileY ордината текущего тайла
     * @param g граф карты трасы
     * @return следующий тайл для посещения (он соседний с текущим)
     */
    protected PairIntInt getNextTileByBFS(int sTileX, int sTileY, int fTileX, int fTileY, int[][] g) {
        int start = sTileX * worldHW + sTileY;
        int finish = fTileX * worldHW + fTileY;
        double[] dist = new double[g.length];
        int[] comeFrom = new int[g.length];
        for (int i = g.length - 1; i >= 0; i--) {
            dist[i] = worldHW * worldHW * 10;
            comeFrom[i] = -1;
        }
        Queue<Integer> q = new LinkedList<>();
        q.add(start);
        dist[start] = 0;
        while (!q.isEmpty()) {
            int current = q.poll();
            int cX = current / worldHW;
            int cY = current % worldHW;
            double add = 1;
            if (mapTiles[cX][cY] != TileType.VERTICAL && mapTiles[cX][cY] != TileType.HORIZONTAL) {
                add += 0.3;
            }

            for (int v : g[current]) {
                if ((dist[current] + add) < dist[v] && Math.abs(dist[v] - (dist[current] + add)) > 1e-1) {
                    dist[v] = dist[current] + add;
                    comeFrom[v] = current;
                    q.add(v);
                }
            }
        }

        PairIntInt pii = new PairIntInt(sTileX, sTileY);
        if (comeFrom[finish] != -1) {
            pii = new PairIntInt(comeFrom[finish] / worldHW, comeFrom[finish] % worldHW);
        }
        return pii;
    }
}
