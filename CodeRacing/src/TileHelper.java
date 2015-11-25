
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.StrictMath.PI;
import static java.lang.StrictMath.max;
import static java.lang.StrictMath.min;
import model.Car;
import model.Game;
import model.Move;
import model.World;

/**
 *
 * @author Wsl_F
 */
public class TileHelper {

    /**
     * ширина машины (140)
     */
    private static final int carWidth = 140;
    /**
     * высота машины (210)
     */
    private static final int carHeight = 210;

    private final StrategyWslF strategy;
    private final int tileSize;
    private final int tileMargin;
    private final TileToMatrix tileToMatrix;

    public TileHelper(Car self, World world, Game game, Move move, StrategyWslF strategy) {
        this.strategy = strategy;
        this.tileSize = (int) (game.getTrackTileSize() + 0.1);
        this.tileMargin = tileSize / 10;
        this.tileToMatrix = new TileToMatrix(tileSize, tileMargin);
    }

    /**
     * получает схематическое изображение текущего тайла с учетом собстевенной
     * машины, планируется добавить машины соперников
     */
    public void calculateCurTile() {
        strategy.currentTile = tileToMatrix.getMatrix(strategy.mapTiles[strategy.curTileX][strategy.curTileY]);

        Point[] rectangleCar = getCarVertexCoordinates(strategy.self);
        Point minP = new Point();
        Point maxP = new Point();
        getMinMaxCoordinates(rectangleCar, minP, maxP);
        int xMin, xMax, yMin, yMax;
        xMin = max((int) minP.x - 1, 0);
        yMin = max((int) minP.y - 1, 0);
        xMax = min((int) maxP.x + 1, (int) strategy.game.getTrackTileSize() - 1);
        yMax = min((int) maxP.y + 1, (int) strategy.game.getTrackTileSize() - 1);

        for (int i = xMin; i <= xMax; i++) {
            for (int j = yMin; j <= yMax; j++) {
                Point p = new Point(i, j);
                if (p.checkPointInPolygon(rectangleCar)) {
                    setCurTile(i, j, strategy.selfCar);
                }
            }
        }
    }

    /**
     * возвращеет через minP, maxP минимальные и максимальные значения абсцисс и
     * ординат из мн-ва точек polygon
     *
     * @param polygon множество точек
     * @param minP параметр для возвращения минимальной абсциссы и ординаты
     * @param maxP параметр для возвращения максимальной абсциссы и ординаты
     */
    private void getMinMaxCoordinates(Point[] polygon, Point minP, Point maxP) {
        double xMin, xMax, yMin, yMax;
        xMin = polygon[0].x;
        xMax = polygon[0].x;
        yMin = polygon[0].y;
        yMax = polygon[0].y;
        for (Point p : polygon) {
            if (p.x < xMin) {
                xMin = p.x;
            } else if (p.x > xMax) {
                xMax = p.x;
            }

            if (p.y < yMin) {
                yMin = p.y;
            } else if (p.y > yMax) {
                yMax = p.y;
            }
        }

        minP.x = xMin;
        minP.y = yMin;
        maxP.x = xMax;
        maxP.y = yMax;
    }

    /**
     * вычисляет относительные координаты 4 вершин машини
     *
     * @return массив из 4 точек - вершин машины
     */
    private Point[] getCarVertexCoordinates(Car car) {
        double x = getRelativeCoordinate(car.getX());
        double y = getRelativeCoordinate(car.getY());
        Point carCenter = new Point(x, y);

        Vector carVector = new Vector(car.getAngle());

        Vector v = new Vector(carVector);
        v.rotateVector(-PI / 2);

        double k = carWidth / (2 * v.length());
        Point M = new Point(carCenter.x + k * v.x, carCenter.y + k * v.y);
        k = carHeight / (2 * carVector.length());

        Point a = new Point(M.x + k * carVector.x, M.y + k * carVector.y);
        Point b = a.getSymmetric(M);
        Point c = a.getSymmetric(carCenter);
        Point d = b.getSymmetric(carCenter);

        return new Point[]{a, b, c, d};
    }

    /**
     * возвращает значение в матрице таййла, с относительными координатами (х;у)
     *
     * @param x абсцисса
     * @param y ордината
     * @return значение в текущем тайле (стена/пусто/своя машина/...)
     */
    public int getCurTile(int x, int y) {
        if (x < 0 || y < 0 || x >= tileSize || y >= tileSize) {
            return strategy.wall;
        }
        return strategy.currentTile[x][y];
    }

    /**
     * задает значение val в матрице таййла, с относительными координатами (х;у)
     *
     * @param x абсцисса
     * @param y ордината
     * @param val значение
     */
    public boolean setCurTile(int x, int y, int val) {
        if (x < 0 || y < 0 || x >= tileSize || y >= tileSize) {
            return false;
        }
        strategy.currentTile[x][y] = val;
        return true;
    }

    private double getRelativeCoordinate(double c) {
        return c - ((int) (c / tileSize)) * tileSize;
    }

    /**
     * печатает в файл currentTile.txt схематическое изображение текущего тайла
     */
    public void printCurTileToFile() {
        try (FileWriter writer = new FileWriter("curTile.txt", false)) {
            for (int y = 0; y < tileSize; y++) {
                String s = "";
                for (int x = 0; x < tileSize; x++) {
                    switch (strategy.currentTile[x][y]) {
                        case StrategyWslF.selfCar:
                            s += '.';
                            break;
                        case StrategyWslF.wall:
                            s += '▓';
                            break;
                        case StrategyWslF.empty:
                            s += ' ';
                            break;
                    }
                }

                writer.write(s + "\r\n");
            }
        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }
    }

}
