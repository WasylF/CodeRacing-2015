
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.StrictMath.abs;
import java.util.LinkedList;
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
    /**
     * список номеров тайлов в порядке обхода, повторенные трижды. Каждый тайл
     * задаётся массивом длины 2, где элемент с индексом {@code 0} содержит
     * позицию X, а элемент с индексом {@code 1} --- позицию Y. Конвертировать
     * позицию в точные координаты можно, используя значение
     * {@code tileSize/worldTileSize}. Для прохождения круга кодемобилю
     * необходимо посещать тайлы в указанном порядке.
     */
    protected int[][] allWayPoints;
    /**
     * массив ключевых тайлов. Каждый тайл задаётся массивом длины 2, где
     * элемент с индексом {@code 0} содержит позицию X, а элемент с индексом
     * {@code 1} --- позицию Y. Конвертировать позицию в точные координаты
     * можно, используя значение {@code game.trackTileSize}. Для прохождения
     * круга кодемобилю необходимо посещать тайлы в указанном порядке. Ключевой
     * тайл с индексом {@code 0} является одновременно начальным тайлом трассы и
     * конечным тайлом каждого круга. Считается, что кодемобиль посетил ключевой
     * тайл, если центр кодемобиля пересёк границу этого тайла.
     */
    protected int[][] systemWayPoints;
    /**
     * текущий размер массива allWayPoints
     */
    protected int curAllWayPointsSize;
    /**
     * тру, если уже развернули
     */
    protected boolean isAllWayPointReversed;
    /**
     * массив, содержащий для каждого тайла из массива allWayPoints координаты
     * ключевой точки(в которую мы будем пытаться заехать) в нем
     */
    protected Point[] allWayKeyPoints;
    /**
     * индекс текущего тайла в массиве allWayPoints
     */
    protected int curPositionInAllPoints;
    /**
     * nextTile[x][y] - список следующих тайлов, если сейчас нахожишься в тайле
     * [x][y]. nextTile[x][y][i] - следующий тайл, который нужно посетить если
     * индекс ключевого тайла посещенного последним - i
     */
    protected PairIntInt nextTile[][][];

    public void move(Car self, World world, Game game, Move move) {
        initAll(self, world, game, move);
        move();
    }

    protected abstract void move();

    protected void initFirst(Car self, World world, Game game, Move move) {
        int[][] tWayPoints = world.getWaypoints();
        systemWayPoints = new int[tWayPoints.length * 3][2];
        System.arraycopy(tWayPoints, 0, systemWayPoints, 0, tWayPoints.length);
        System.arraycopy(tWayPoints, 0, systemWayPoints, tWayPoints.length, tWayPoints.length);
        System.arraycopy(tWayPoints, 0, systemWayPoints, 2 * tWayPoints.length, tWayPoints.length);

        mapTiles = world.getTilesXY();
        worldHeight = mapTiles.length;
        worldWidth = mapTiles[0].length;
        //carWidth = 140;//(int) (self.getWidth() + 0.1);
        //carHeight = 210;//(int) (self.getHeight() + 0.1);

        tileSize = (int) (game.getTrackTileSize() + 0.1);
        marginSize = (int) (game.getTrackTileMargin() + 0.1);

        int n = world.getWaypoints().length;
        nextTile = new PairIntInt[worldWidth + 5][worldHeight + 5][n + 1];
    }

    protected void initAll(Car self, World world, Game game, Move move) {
        this.self = self;
        this.world = world;
        this.game = game;
        this.move = move;

        tileToMatrix = new TileToMatrix(world, game, move, self);
        tileToMatrix80 = new TileToMatrix(world, game, move, self, 80, 8);

        curTileX = (int) (self.getX() / game.getTrackTileSize());
        curTileY = (int) (self.getY() / game.getTrackTileSize());
        selfX = (int) (self.getX()) % tileSize;
        selfY = (int) (self.getY()) % tileSize;
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
    protected int[][] calculateWorldMap(int wTileSize) {
        int[][] wMap = new int[worldWidth * wTileSize][worldHeight * wTileSize];
        TileToMatrix worldTile = new TileToMatrix(world, game, move, self, wTileSize, wTileSize / 10);
        int[][] tileMatrix;
        for (int i = 0; i < worldHeight; i++) {
            for (int j = 0; j < worldWidth; j++) {
                tileMatrix = worldTile.getMatrix(mapTiles[i][j]);
                //worldTile.printCurTileToFile("tile "+i+" "+j+" .txt");
                for (int q = 0; q < wTileSize; q++) {
                    System.out.println("q: " + q);
                    System.arraycopy(tileMatrix[q], 0, wMap[j * wTileSize + q], i * wTileSize, wTileSize);
                }
            }
        }
        /*
         //нанесение системных ключевых точек
         int[][] wayPoints = world.getWaypoints();
         for (int[] wayPoint1 : wayPoints) {
         int x = wayPoint1[0] * wTileSize + wTileSize / 2;
         int y = wayPoint1[1] * wTileSize + wTileSize / 2;
         for (int delta1 = -wTileSize / 7; delta1 < wTileSize / 7; delta1++) {
         for (int delta2 = -wTileSize / 7; delta2 < wTileSize / 7; delta2++) {
         wMap[y + delta1][x + delta2] = wayPoint;
         }
         }
         }
         */
        return wMap;
    }

    /**
     * печатает в файл worldMap.txt схематическое изображение текущего тайла
     */
    protected void printWorldMapToFile(String fileName) {
        try (FileWriter writer = new FileWriter(fileName, false)) {
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
                        default:
                            s += (worldMap[x][y]) % 10;
                    }
                }

                writer.write(s + "\r\n");
            }
        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }
    }

    /**
     * печатает в файл worldMap.txt схематическое изображение текущего тайла
     */
    protected void printWorldMapToFile(int[][] wMap, int wTileSize, String fileName) {
        try (FileWriter writer = new FileWriter(fileName, false)) {
            for (int x = 0; x < worldHeight * wTileSize; x++) {
                String s = "";
                for (int y = 0; y < worldWidth * wTileSize; y++) {
                    switch (wMap[x][y]) {
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
                        default:
                            s += (wMap[x][y]) % 10;
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
    public boolean setWorldMap(int x, int y, int val) {
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
    protected int getWorldMap(int x, int y) {
        if (x < 0 || y < 0
                || x >= worldTileSize * worldHeight
                || y >= worldTileSize * worldWidth) {
            return wall;
        }
        return worldMap[y][x];
    }

    /**
     *
     * @return копию матрици карты
     */
    public int[][] getCopyWorldMap() {
        int[][] map = new int[worldTileSize * worldHeight][worldTileSize * worldWidth];

        for (int i = 0; i < worldTileSize * worldHeight; i++) {
            System.arraycopy(worldMap[i], 0, map[i], 0, worldTileSize * worldWidth);
        }
        return map;
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
     * вычисление координаты для доступа к матрице карты
     *
     * @param absolute абсолютная координата
     * @return относительную координату
     */
    public int convertToWorldCordinate(double absolute) {
        return (int) (absolute * worldTileSize) / tileSize;
    }

    public int convertToAbsoluteCordinate(double worldCordinate) {
        return (int) (worldCordinate * tileSize) / worldTileSize;
    }

    protected void calculateAllWayPoints() {
        int startTileX = systemWayPoints[0][0];
        int startTileY = systemWayPoints[0][1];

        int numberOfSystemWayPoints = world.getWaypoints().length;
        allWayPoints[0] = new int[]{startTileX, startTileY};
        curAllWayPointsSize = 1;
        int smallWorldTile = 10;
        int[][] smallWorld = calculateWorldMap(smallWorldTile);
        //printWorldMapToFile(smallWorld, smallWorldTile, "smallWorld.txt");

        for (int i = 1; i <= numberOfSystemWayPoints; i++) {
            int[][] sWorld = get2DArrayCopy(smallWorld);
            int cTileX = systemWayPoints[i - 1][0];
            int cTileY = systemWayPoints[i - 1][1];
            int nTileX = systemWayPoints[i][0];
            int nTileY = systemWayPoints[i][1];
            int destination = 2_000_000_000;
            int x = (int) ((cTileX + 0.5) * smallWorldTile);
            int y = (int) ((cTileY + 0.5) * smallWorldTile);
            sWorld[y][x] = 1;
            sWorld[(int) ((nTileY + 0.5) * smallWorldTile)][(int) ((nTileX + 0.5) * smallWorldTile)] = destination;

            int n = 10000;
            Queue<Integer> queue = new LinkedList<>();
            Queue<Integer> qBack = new LinkedList<>();
            queue.add(x * n + y);
            ForwardBFS_ForAllWayPoints(queue, qBack, n, destination, sWorld);
            while (qBack.size() > 1) {
                qBack.poll();
            }
            LinkedList<Integer> list = BackwordBFS_ForAllWayPoints(qBack, n, sWorld, smallWorldTile);
            while (!list.isEmpty()) {
                int cur = list.pollLast();
                x = cur / n;
                y = cur % n;
                if (allWayPoints[curAllWayPointsSize - 1][0] != x
                        || allWayPoints[curAllWayPointsSize - 1][1] != y) {
                    allWayPoints[curAllWayPointsSize++] = new int[]{x, y};
                }
            }
        }

        for (int i = 0; i < curAllWayPointsSize; i++) {
            System.out.println("tile: " + allWayPoints[i][0] + " " + allWayPoints[i][1]);
        }

        int awpSize = curAllWayPointsSize - 1;
        for (int i = 1; i < curAllWayPointsSize; i++) {
            allWayPoints[awpSize + i] = allWayPoints[i];
            allWayPoints[awpSize * 2 + i] = allWayPoints[i];
        }

        curAllWayPointsSize *= 3;

        System.out.println();
        System.out.println();
        for (int i = 0; i < curAllWayPointsSize; i++) {
            System.out.println("tile: " + allWayPoints[i][0] + " " + allWayPoints[i][1]);
        }
    }

    /**
     * поиска в ширину с целью нахождения следующей ключевой точки пути и
     * расстояния до следующей системной целевой точки
     *
     * @param queue очередь, содержащая стартовые точки
     * @param qBack очередь точек соседних с финальными
     * @param n множитель для обработки пары чисел в формате first*n+second
     * @param destination метка финальной точки
     * @return расстояния до следующей системной целевой точки
     */
    private int ForwardBFS_ForAllWayPoints(Queue<Integer> queue, Queue<Integer> qBack,
            int n, final int destination, int[][] sWorld) {
        int x, y, cur;
        int distance = 2_000_000_000;

        while (!queue.isEmpty()) {
            cur = queue.poll();
            x = cur / n;
            y = cur % n;
            cur = sWorld[y][x];
            if (cur > distance) {
                continue;
            }
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (abs(i + j) == 1) {
                        System.out.println((y + j) + "    -     " + (x + i));
                        try {
                            int tmp = sWorld[y + j][x + i];
                            if (tmp == empty) {
                                sWorld[y + j][x + i] = cur + 1;
                                queue.add((x + i) * n + (y + j));
                            }
                            if (tmp == destination) {
                                sWorld[y][x] = 2 * cur;
                                qBack.add(x * n + y);
                                //qBack.add(new PointAnglePrev(x, y, 0, new Point()));
                                if (cur < distance) {
                                    distance = cur;
                                }
                            }

                        } catch (Exception e) {
                            System.out.println("Организаторы раньше времени и без предупреждения ввели невидимые тайлы!!!!");
                        }
                    }
                }
            }
        }

        return distance;
    }

    /**
     * обратный поиск в ширину для нахождения следующей ключевой точки пути
     *
     * @param qBack очередь достигнутых точек соседних с финальными
     * @param n множитель для обработки пары чисел в формате first*n+second
     * @param distance расстояние на котором ищем следующую ключевую точку
     * @return множество точек на расстоянии distance
     */
    private LinkedList<Integer> BackwordBFS_ForAllWayPoints(Queue<Integer> qBack,
            int n, int[][] sWorld, int sWorldTile) {
        boolean[][] used = new boolean[20][20];
        LinkedList<Integer> ans = new LinkedList<>();

        int x, y, cur;
        while (!qBack.isEmpty()) {
            cur = qBack.poll();
            x = cur / n;
            y = cur % n;
            int tX = x / sWorldTile;
            int tY = y / sWorldTile;
            if (!used[tX][tY]) {
                used[tX][tY] = true;
                ans.add(n * tX + tY);
            }
            cur = sWorld[y][x] / 2;
            sWorld[y][x] = empty;
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (sWorld[y + j][x + i] == cur - 1) {
                        qBack.add((x + i) * n + (y + j));
                        sWorld[y + j][x + i] = (cur - 1) * 2;
                    }
                }
            }
        }
        return ans;
    }

    protected PairIntInt getNextTile(int curTileX, int curTileY) {
        for (int i = curPositionInAllPoints; i < curAllWayPointsSize; i++) {
            if (allWayPoints[i][0] == curTileX && allWayPoints[i][1] == curTileY) {
                curPositionInAllPoints = i;
                return new PairIntInt(allWayPoints[i + 1][0], allWayPoints[i + 1][1]);
            }
        }
        return new PairIntInt(0, 0); // null
    }

    /**
     * вычисление следующих тайлов (массив nextTile[cTileX][cTileY])
     *
     * @param cTileX абсцисса текущего тайла
     * @param cTileY ордината текущего тайла
     */
    protected void getNextTilesFor(int cTileX, int cTileY) {
        int numberOfWayPoints = world.getWaypoints().length;
        int smallWorldTile = 10;
        int[][] smallWorld = calculateWorldMap(smallWorldTile);

        for (int i = 0; i < numberOfWayPoints; i++) {
            int[][] sWorld = get2DArrayCopy(smallWorld);

            int xT = systemWayPoints[i][0];
            int yT = systemWayPoints[i][1];
            int xTN = systemWayPoints[i + 1][0];
            int yTN = systemWayPoints[i + 1][1];

            int destination = 2_000_000_000;
            int x = (int) ((xT + 0.5) * smallWorldTile);
            int y = (int) ((yT + 0.5) * smallWorldTile);
            sWorld[y][x] = 1;
            sWorld[(int) ((yTN + 0.5) * smallWorldTile)][(int) ((xTN + 0.5) * smallWorldTile)] = destination;

            int n = 10000;
            Queue<Integer> queue = new LinkedList<>();
            Queue<Integer> qBack = new LinkedList<>();
            queue.add(x * n + y);
            ForwardBFS_ForAllWayPoints(queue, qBack, n, destination, sWorld);
            while (qBack.size() > 1) {
                qBack.poll();
            }
            LinkedList<Integer> list = BackwordBFS_ForAllWayPoints(qBack, n, sWorld, smallWorldTile);
            if (list.getLast() == xT * n + yT) {
                list.pollLast();
            }
            {
                int cur = list.getLast();
                x = cur / n;
                y = cur % n;
                nextTile[cTileX][cTileY][i] = new PairIntInt(x, y);
            }
        }
    }

}
