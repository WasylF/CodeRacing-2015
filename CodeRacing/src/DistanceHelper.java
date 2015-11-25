
import static java.lang.StrictMath.PI;
import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.max;
import static java.lang.StrictMath.min;
import model.Car;

/**
 *
 * @author Wsl_F
 */
public class DistanceHelper {

    //constants below
    /**
     * константа для отображения на матрице тайла собственной машины
     */
    private static final int selfCar = -1;
    /**
     * константа для отображения на матрице тайла машины сокомандника
     */
    private static final int teammateCar = -2;
    /**
     * константа для отображения на матрице тайла машины соперников
     */
    private static final int opponentCar = -10;
    /**
     * константа для отображения на матрице тайла ключевой точки
     */
    private static final int wayPoint = -50;
    /**
     * константа для отображения на матрице тайла пустой точки
     */
    private static final int empty = 0;
    /**
     * константа для отображения на матрице тайла ограждения/стен
     */
    private static final int wall = -100;
    /**
     * ширина машины (140)
     */
    private static final int carWidth = 140;
    /**
     * высота машины (210)
     */
    private static final int carHeight = 210;
    /**
     * время начала заезда
     */
    private static final int startTick = 180;
    /**
     * размер тайла в общей карте трека
     */
    private static final int worldTileSize = 80;
    /**
     * размер закругления в общей карте трека
     */
    private static final int worldMarginSize = worldTileSize / 10;
//end constants

    /**
     * длинна/ширина тайла
     */
    private final int tileSize;
    /**
     * радиус закругления
     */
    private final int marginSize;

    /**
     * ширина карты в тайлах
     */
    private final int worldWidth;
    /**
     * высота карты в тайлах
     */
    private final int worldHeight;
    /**
     * максимум среди ширины и высоты карты
     */
    private final int worldHW;

    private final StrategyWslF strategy;
    /**
     * объект для операций с матрицей всей карты
     */
    private final WorldMapHelper worldMapHelper;
    /**
     * объект для операций с графом из тайлов.
     */
    private final WorldGraphHelper worldGraphHelper;
    /**
     * объект для работы с текущим тайлом
     */
    private final TileHelper tileHelper;

    public DistanceHelper(int tileSize, int marginSize,
            int worldWidth, int worldHeight, int worldHW,
            StrategyWslF strategyWslF,
            WorldGraphHelper wgh, WorldMapHelper wmh, TileHelper th) {
        this.tileSize = tileSize;
        this.marginSize = marginSize;

        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.worldHW = worldHW;

        this.strategy = strategyWslF;

        this.worldGraphHelper = wgh;
        this.worldMapHelper = wmh;
        this.tileHelper = th;
    }

    /**
     * отодвигает АБСОЛЮТНУЮ координату от стены, если расстояние до стены 2/3
     * ширины корпуса
     *
     * @param t координата
     * @return пересчитаная (если нужно) координата
     */
    private double getNotToCloseToWall(double t) {
        int k = (int) t / tileSize;
        t = (int) t % tileSize;
        if (t < marginSize + 2 * carWidth / 3) {
            t = marginSize + 2 * carWidth / 3;
        }
        if (t > tileSize - marginSize - 2 * carWidth / 3) {
            t = tileSize - (marginSize + 2 * carWidth / 3);
        }
        return k * tileSize + t;
    }

    /**
     * в случае надобности отодвигает координату от стены
     *
     * @param t АБСОЛЮТНАЯ координата для матрици worldMap
     * @return АБСОЛЮТНАЯ координата, отодвинутая от стены, если нужно
     */
    private double getNotToCloseToWorldMapWall(double t) {
        //пересчитать относительную координату
        int k = (int) t / worldTileSize;
        t = (int) t % worldTileSize;

        double delta = carWidth / 2;
        delta = delta * worldTileSize / tileSize;
        if (t < worldMarginSize + delta) {
            t = worldMarginSize + delta;
        }
        if (t > worldTileSize - worldMarginSize - delta) {
            t = worldTileSize - (worldMarginSize + delta);
        }
        return k * worldTileSize + t;
    }

    /**
     * возвращает относительную карту (для текущего тайла) координату
     *
     * @param c абсолютная координата
     * @return
     */
    private double getRelativeCoordinate(double c) {
        return c - ((int) (c / tileSize)) * tileSize;
    }

    /**
     * возвращает корректную координату, то есть от 0 до tileSize-1
     *
     * @param t входная координата
     * @return подкорректированная
     */
    private int getAccurateCoordinate(double t) {
        t = max(t, 0);
        t = min(t, tileSize - 1);
        return (int) t;
    }

    /**
     * возвращает корректную координату, то есть от 0 до tileSize-1 !!!
     * изменяются значения внутри параметра @code(p)
     *
     * @param p входная координата
     * @return подкорректированная
     */
    private PairIntInt getAccurateWorldCoordinate(PairIntInt p) {
        p.first = max(0, p.first);
        p.first = min(p.first, worldWidth * worldTileSize);
        p.second = max(0, p.second);
        p.second = min(p.second, worldWidth * worldTileSize);
        return p;
    }

    /**
     * находим расстояние от центра машины до ближайшей стены
     *
     * @param car машина
     * @param deltaAngle угол отклонения от вектора скорости
     * @param directionVector направление в котором ищем стену (равняется
     * вектору скорости или вектору направления авто)
     * @return расстояние до стены
     */
    public int getDistanceToWall(Car car, double deltaAngle, Vector directionVector) {
        directionVector = new Vector(directionVector);
        int carX = (int) worldMapHelper.convertToWorldCordinate(car.getX());
        int carY = (int) worldMapHelper.convertToWorldCordinate(car.getY());
        if (directionVector.length() < 1e-3) {
            return tileSize;
        }
        directionVector.normalize();
        directionVector.mult(1.001);
        final int numberOfTurns = 40;
        double turnAngle;

        for (int d = 1; d > 0; d++) {
            Vector vector = new Vector(directionVector);
            vector.rotateVector(-deltaAngle);

            turnAngle = 2 * deltaAngle / numberOfTurns;
            for (int i = 0; i <= numberOfTurns; i++) {
                int x = (int) (carX + d * vector.x);
                int y = (int) (carY + d * vector.y);
                PairIntInt p = new PairIntInt(x, y);
                getAccurateWorldCoordinate(p);
                if (worldMapHelper.getClear(x, y) == wall) {
                    return worldMapHelper.convertToAbsoluteCordinate(d);
                }
                vector.rotateVector(turnAngle);
            }
            if (deltaAngle > PI / 180) {
                deltaAngle *= 0.95;
            }
        }
        //should never happens
        return tileSize;
    }

    /**
     * находим расстояние от центра собственной машины до ближайшей стены за
     * направление поиска берем вектор СКОРОСТИ авто
     *
     * @param deltaAngle угол отклонения от вектора скорости
     * @return расстояние до стены
     */
    public int getDistanceToWallBySpeed(double deltaAngle) {
        Vector directionVector = new Vector(strategy.curSpeed);
        if (directionVector.module() == 0) {
            directionVector.getVectorByAngle(strategy.self.getAngle());
        }
        return getDistanceToWall(strategy.self, deltaAngle, directionVector);
    }

    /**
     * находим расстояние от центра собственной машины до ближайшей стены за
     * направление поиска берем вектор НАПРАВЛЕНИЯ авто
     *
     * @param deltaAngle угол отклонения от вектора скорости
     * @return расстояние до стены
     */
    public int getDistanceToWallByCarDirection(double deltaAngle) {
        Vector directionVector = new Vector(strategy.self.getAngle());
        return getDistanceToWall(strategy.self, deltaAngle, directionVector);
    }

    /**
     * находим расстояние от центра собственной машины до ближайшей стены за
     * направление поиска берем вектор НАПРАВЛЕНИЯ авто
     *
     * @param deltaAngle угол отклонения от вектора скорости
     * @return расстояние до стены
     */
    public int getDistanceToWallByOpCarDirection(double deltaAngle) {
        Vector directionVector = new Vector(strategy.self.getAngle());
        directionVector.rotateVector(PI);
        return getDistanceToWall(strategy.self, deltaAngle, directionVector);
    }

}
