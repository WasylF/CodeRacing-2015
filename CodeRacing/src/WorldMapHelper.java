
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
    public final int worldTileSize;
    /**
     * размер закругления в общей карте трека
     */
    public final int worldMarginSize;
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
    /**
     * матрица трасы содержащяя только стены и совободные клетки
     */
    private final int[][] ClearWorldMap;
    /**
     * матрица трассы содержащяя не только стены, а и машины
     */
    private final int[][] WorldMap;

    public WorldMapHelper(int tileSize, int worldTileSize, World world, StrategyWslF strategy) {
        this.worldWidth = world.getWidth();
        this.worldHeight = world.getHeight();
        this.strategy = strategy;
        this.tileSize = tileSize;
        this.worldTileSize = worldTileSize;
        this.worldMarginSize = (worldTileSize + 9) / 10;
        ClearWorldMap = calculateClearWorldMap();
        WorldMap = calculateWorldMap();
    }

    /**
     * строит карту всей трассы world[x][y] - содержимое точки с карты с
     * координатами (x,y)
     *
     * @return карту трассы
     */
    private int[][] calculateClearWorldMap() {
        int[][] wMap = new int[worldWidth * worldTileSize][worldHeight * worldTileSize];
        TileToMatrix worldTile = new TileToMatrix(worldTileSize, worldMarginSize);
        int[][] tileMatrix;

        for (int tileX = 0; tileX < worldWidth; tileX++) {
            for (int tileY = 0; tileY < worldHeight; tileY++) {
                tileMatrix = worldTile.getMatrix(strategy.mapTiles[tileX][tileY]);
                for (int x = 0; x < worldTileSize; x++) {
                    for (int y = 0; y < worldTileSize; y++) {
                        wMap[tileX * worldTileSize + x][tileY * worldTileSize + y] = tileMatrix[x][y];
                    }
                }
            }
        }
        return wMap;
    }

    /**
     * печатает в файл изображение карты
     *
     * @param fileName имя файла
     */
    public void printClearWorldMapToFile(String fileName) {
        try (FileWriter writer = new FileWriter(fileName, false)) {
            for (int y = 0; y < ClearWorldMap[0].length; y++) {
                String s = "";
                for (int x = 0; x < ClearWorldMap.length; x++) {
                    switch (ClearWorldMap[x][y]) {
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
                            s += (ClearWorldMap[x][y]) % 10;
                    }
                }

                writer.write(s + "\r\n");
            }
        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }
    }

    /**
     *
     * @return копию матрици
     */
    private int[][] getCopy2D(int[][] ar) {
        int[][] copy = new int[ar.length][ar[0].length];

        for (int i = 0; i < ar.length; i++) {
            for (int j = 0; j < ar[i].length; i++) {
                copy[i][j] = ar[i][j];
            }
        }
        return copy;
    }

    /**
     * возвращает копию матрицы трасы с нанесенными стенами и ничего больше
     *
     * @return
     */
    public int[][] getClearWorld() {
        return getCopy2D(ClearWorldMap);
    }

    /**
     * копия матрицы трасы со стенами и авто
     *
     * @return
     */
    public int[][] getWorldMap() {
        return getCopy2D(WorldMap);
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

    /**
     * вычисление абсолютных координат по координатам "в мире"
     *
     * @param worldCordinate
     * @return
     */
    public int convertToAbsoluteCordinate(double worldCordinate) {
        return (int) (worldCordinate * tileSize) / worldTileSize;
    }

    private int[][] calculateWorldMap() {
        int[][] wMap = calculateClearWorldMap();
        return wMap;
    }

    public int getClear(int x, int y) {
        return ClearWorldMap[x][y];
    }
}
