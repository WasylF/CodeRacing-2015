
import java.io.FileWriter;
import java.io.IOException;
import model.Car;
import model.Game;
import model.Move;
import model.World;

/**
 *
 * @author Wsl_F
 */
public class WorldMapHelper {

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
     * размер тайла в общей карте трека
     */
    public static final int worldTileSize = 80;
    /**
     * размер закругления в общей карте трека
     */
    public static final int worldMarginSize = worldTileSize / 10;
    /**
     * ширина карты в тайлах
     */
    public final int worldWidth;
    /**
     * высота карты в тайлах
     */
    public final int worldHeight;
    /**
     * длинна/ширина тайла
     */
    public final int tileSize;

//end constants
    StrategyWslF strategy;

    public WorldMapHelper(int worldHight, int worldWidth, Car self, World world, Game game, Move move, StrategyWslF strategy) {
        this.worldWidth = worldHight;
        this.worldHeight = worldWidth;
        this.strategy = strategy;
        this.tileSize = (int) (game.getTrackTileSize() + 0.1);
    }

    /**
     * строит карту всей трассы
     *
     * @param wTileSize размер стороны тайла на матрице
     * @return карту трассы
     */
    public int[][] calculateWorldMap(int wTileSize) {
        int[][] wMap = new int[worldWidth * wTileSize][worldHeight * wTileSize];
        TileToMatrix worldTile = new TileToMatrix(wTileSize, wTileSize / 10);
        int[][] tileMatrix;
        for (int i = 0; i < worldHeight; i++) {
            for (int j = 0; j < worldWidth; j++) {
                tileMatrix = worldTile.getMatrix(strategy.mapTiles[i][j]);
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
    public void printWorldMapToFile(String fileName) {
        try (FileWriter writer = new FileWriter(fileName, false)) {
            for (int x = 0; x < worldHeight * worldTileSize; x++) {
                String s = "";
                for (int y = 0; y < worldWidth * worldTileSize; y++) {
                    switch (strategy.worldMap[x][y]) {
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
                            s += (strategy.worldMap[x][y]) % 10;
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
    public void printWorldMapToFile(int[][] wMap, int wTileSize, String fileName) {
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
     * @return true если установли значение
     */
    public boolean setWorldMap(int x, int y, int val) {
        if (x < 0 || y < 0
                || x >= worldTileSize * worldHeight
                || y >= worldTileSize * worldWidth) {
            return false;
        }
        strategy.worldMap[y][x] = val;
        return true;
    }

    /**
     * возвращает значение в матрице карты, с относительными координатами (х;у)
     *
     * @param x абсцисса
     * @param y ордината
     * @return значение в текущем тайле (стена/пусто/своя машина/...)
     */
    public int getWorldMap(int x, int y) {
        if (x < 0 || y < 0
                || x >= worldTileSize * worldHeight
                || y >= worldTileSize * worldWidth) {
            return wall;
        }
        return strategy.worldMap[y][x];
    }

    /**
     *
     * @return копию матрици карты
     */
    public int[][] getCopyWorldMap() {
        int[][] map = new int[worldTileSize * worldHeight][worldTileSize * worldWidth];

        for (int i = 0; i < worldTileSize * worldHeight; i++) {
            System.arraycopy(strategy.worldMap[i], 0, map[i], 0, worldTileSize * worldWidth);
        }
        return map;
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

}
