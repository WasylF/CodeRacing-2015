
import java.io.FileWriter;
import java.io.IOException;
import java.lang.*;
import static java.lang.StrictMath.*;
import java.util.LinkedList;
import java.util.Queue;
import model.Bonus;
import model.BonusType;
import model.Car;
import model.Move;
import model.TileType;

/**
 *
 * @author Wsl_F
 */
public class StrategyBuggy1x4 extends StrategyWslF {

    /**
     * показывает нужно ли использовать тормоза на текущем ходу. инициализурется
     * 0, в течении хода можем установить 1
     */
    private boolean useBreaks;
    /**
     * оставшееся количество тиков, в котороых машина будет ехать задним ходом.
     */
    private int goBack;

    /**
     * количество тиков в течении которых автомобиль должен сдавать задним ходом
     */
    private static final int numberOfTickToGoBack = 80;
    /**
     * точка, в направлении которой должен двигаться автомобиль координаты
     * АБСОЛЮТНЫЕ
     */
    private PairIntInt nextWayPoint;
    /**
     * координаты тайла, в который сейчас едет автомобиль
     */
    private PairIntInt nextTile;

    /**
     * метод для инициализациии, вызываемый до начала хода
     */
    private void initialization() {
        useBreaks = false;
        if (previousSpeed == null) {
            previousSpeed = new Vector();
        }
        if (goBack < 0) {
            goBack = 0;
        }
    }

    /**
     * вычислении некторых значений необходимых для совершения хода например,
     * матрица текущего тайла, текущая скорость, следующая точка..
     */
    private void prepearMove() {
        curSpeed = new Vector(self.getSpeedX(), self.getSpeedY());
        carDirection = new Vector(self.getAngle());
        tileHelper.calculateCurTile();
        //tileHelper.printCurTileToFile();
        nextWayPoint = getNextWayPoint();
        System.out.println("NextWayPoint: " + nextWayPoint);
    }

    /**
     * метод вызываемый после совершения хода
     */
    private void finalizeMove() {
        previousSpeed = curSpeed;
        if (goBack > 0) {
            goBack--;
        }
        previousMove = move;
    }

    /**
     * метод совершения хода
     */
    private void makeMove() {
        if (shouldGoBack()) {
            goingBack();
            return;
        }

        Vector speed = new Vector(self.getSpeedX(), self.getSpeedY());

        shouldGetBonus();

        Vector toNextWayPoint = new Vector(nextWayPoint.first - self.getX(), nextWayPoint.second - self.getY());
        double angleToWaypoint = (carDirection.getAngle(toNextWayPoint) + 2 * curSpeed.getAngle(toNextWayPoint)) / 3;

        double wheelTurn = getWheelTurn(angleToWaypoint);

        calulateEnginePower(angleToWaypoint, wheelTurn, speed);

        activateIfNeedBreaks(angleToWaypoint, speed);

        activateIfNeedAmmo();
    }

    private boolean shouldGetBonus() {
        Bonus[] bonuses = world.getBonuses();
        for (Bonus bonus : bonuses) {
            int tilesBeforeTurn = getTilesBeforeTurn();
            if (tilesBeforeTurn == 0) {
                return false;
            }
            PairIntInt bonusCoordinate = new PairIntInt(bonus.getX(), bonus.getY());
            PairIntInt bonusTile = getTileOfObject(bonus.getX(), bonus.getY());
            if (getTileDistance(curTile, bonusTile) < 2) {
                Vector toBonus = new Vector(bonus.getX() - self.getX(), bonus.getY() - self.getY());
                Vector toWayPoint = new Vector(new Point(self.getX(), self.getY()), new Point(nextWayPoint.first, nextWayPoint.second));
                double angleToBonus = abs(toBonus.getAngle(toWayPoint));
                if (angleToBonus > PI / 10) {
                    continue;
                }
                if (tilesBeforeTurn < 2 && angleToBonus > PI / 30) {
                    continue;
                }
                angleToBonus = abs(curSpeed.getAngle(toBonus));
                if ((angleToBonus < PI / 60)
                        || (angleToBonus < PI / 15 && self.getDistanceTo(bonus) > tileSize)
                        || (angleToBonus < PI / 10 && bonus.getType() == BonusType.PURE_SCORE)
                        || (angleToBonus < PI / 10 && self.getDurability() < 0.9
                        && bonus.getType() == BonusType.REPAIR_KIT)
                        || (angleToBonus < PI / 20 && self.getDurability() < 0.5
                        && bonus.getType() == BonusType.REPAIR_KIT)) {
                    nextWayPoint = bonusCoordinate;
                    return true;
                }

            }
        }
        return false;
    }

    /**
     * проверяем, нужно ли сдавать задний ход
     *
     * @return тру, если нужно
     */
    private boolean shouldGoBack() {
        if (world.getTick() < startTick * 1.5) {
            return false;
        }
        double deltaAngle = PI / 30;
        int backDistance = distanceHelper.getDistanceToWallByOpCarDirection(deltaAngle);
        int forwardDistance = distanceHelper.getDistanceToWallByCarDirection(deltaAngle);
        int speedDistance = distanceHelper.getDistanceToWallBySpeed(deltaAngle);

        //если уперлись задом в стенку, то нужно ехать вперед
        if (backDistance < carHeight) {
            goBack = 0;
            return false;
        }

        if (goBack > 0) {
            return true;
        }

        // если лобовое расстояние до стены очень маленькое, то нужно сдавать задний ход
        if (forwardDistance < carHeight && abs(forwardDistance - speedDistance) < tileSize / 10) {
            goBack = numberOfTickToGoBack;
            return true;
        }

        // если мы уперлись в другую машину
        if (curSpeed.module() == 0) {
            Car[] cars = getOpCars();
            for (Car car : cars) {
                Vector carSpeed = new Vector(car.getSpeedX(), car.getSpeedY());
                if (carSpeed.module() == 0 && self.getDistanceTo(car) < 1.2 * carHeight) {
                    goBack = numberOfTickToGoBack;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * совершения хода, при котором машина сдает назад
     */
    private void goingBack() {
        /*Vector opDirect = new Vector(carDirection);
         opDirect.rotateVector(PI);
         Vector toPoint = new Vector(new Point(self.getX(), self.getY()), new Point(nextWayPoint));
         double angleToWayPoint = carDirection.getAngle(toPoint);
         double angle = 2 * toPoint.getAngle(opDirect);
         // находим точку симметричную следующей, для движения задним ходом в ее направлении
         toPoint.rotateVector(angle);
         nextWayPoint = new PairIntInt(self.getX() + toPoint.x, self.getY() + toPoint.y);
         */
        double prevWheelTurn = signum(previousMove.getWheelTurn());
        // если мы начали движение назад на текущем тике выворачиваем руль в противоположную сторону
        //иначе оставляем как на предыдущем ходе
        move.setWheelTurn(goBack == numberOfTickToGoBack ? -prevWheelTurn : prevWheelTurn);

        int minDist = min(distanceHelper.getDistanceToWallByCarDirection(PI / 90),
                distanceHelper.getDistanceToWallBySpeed(PI / 90));
        minDist = min(minDist, distanceHelper.getDistanceToNearesOpCar(self));
        move.setUseNitro(false);
        if (minDist > 3 * carHeight) {//если пора заканчивать движение назад
            move.setEnginePower(1.0);
            move.setBrake(true);
            move.setWheelTurn(0);
        } else {
            move.setEnginePower(-1.0);
            // тормозим только в случае если еще движемся вперед
            move.setBrake(self.getEnginePower() > 0);
        }
    }

    /**
     * метод вызываемый из вне, для совершения хода
     */
    @Override
    public void move() {
        System.out.println("Tick №" + world.getTick() + " starts");

        initialization();
        if (world.getTick() >= startTick) {
            prepearMove();
            makeMove();
            printDebug();
        }
        finalizeMove();

        System.out.println("Tick №" + world.getTick() + " ends");
    }

    /**
     * отладочный вывод в консоль
     */
    private void printDebug() {
        System.out.println("car center: (" + relativeX + " , " + relativeY + ")   speed: " + curSpeed + "   curEnginePower: " + move.getEnginePower());
    }

    /**
     * вычисляет и устанавливает относительтное значение поворота колес
     *
     * @param angleToWaypoint
     * @return относительный поворот колес
     */
    private double getWheelTurn(double angleToWaypoint) {
        double wheelTurn = angleToWaypoint * 12 / PI;
        move.setWheelTurn(wheelTurn);
        return wheelTurn;
    }

    /**
     * вычисляет значение мощности двигателя и устанавливает в move
     */
    private void calulateEnginePower(double angleToWaypoint, double wheelTurn, Vector speed) {
        move.setEnginePower(0.9);
    }

    /**
     * включает, если нужно тормоза
     *
     * @param angleToWaypoint
     * @param speed
     */
    private void activateIfNeedBreaks(double angleToWaypoint, Vector speed) {
        if (useBreaks) {
            move.setBrake(true);
            return;
        }
        Vector toNextWayPoint = new Vector(nextWayPoint.first - self.getX(), nextWayPoint.second - self.getY());
        angleToWaypoint = carDirection.getAngle(toNextWayPoint);
        /*
         if (abs(angleToWaypoint) > PI / 3
         && abs(angleToWaypoint) * speed.length() * speed.length() > 2.5D * 2.5D * PI) {
         move.setBrake(true);
         return;
         }*/
        int tilesBeforeTurn = getTilesBeforeTurn();
        int distanceBeforeTurn = getDistanceBeforeTurn();

        if ((distanceBeforeTurn <= tileSize * 2 && speed.length() > 20)
                || (tilesBeforeTurn <= 2 && speed.length() > 40)
                || (tilesBeforeTurn <= 3 && speed.length() > 45)) {
            move.setUseNitro(false);
            move.setBrake(true);
            return;
        }
        if (speed.length() > 15 && tilesBeforeTurn <= 1
                && abs(angleToWaypoint) > PI / 180) {
            // если проехали больше половины тайла
            if (mapTiles[curTileX][curTileY] == TileType.VERTICAL
                    && signum(self.getSpeedY()) * (relativeY - tileSize / 2) > 0) {
                move.setBrake(true);
                move.setEnginePower(1.0);
                return;
            }

            if (mapTiles[curTileX][curTileY] == TileType.HORIZONTAL
                    && signum(self.getSpeedX()) * (relativeX - tileSize / 2) > 0) {
                move.setBrake(true);
                move.setEnginePower(1.0);
                return;
            }
        }
    }

    /**
     * будем ли поварачивать на тайле, при условии, что с нашего текущего
     * положения можно добраться до (tileX,tileY) без поворотов
     *
     * @param tileX
     * @param tileY
     * @return тру, если будем
     */
    private boolean willTurn(int tileX, int tileY) {
        if (mapTiles[tileX][tileY] == TileType.HORIZONTAL
                || mapTiles[tileX][tileY] == TileType.VERTICAL) {
            return false;
        }

        if (wayToNextKeyPoint.size() < 2) {
            return true;
        }
        PairIntInt nextNextTile = wayToNextKeyPoint.get(1);
        return !(nextNextTile.first == curTileX || nextNextTile.second == curTileY);
    }

    /**
     * активирует расходники, если нужно
     */
    private void activateIfNeedAmmo() {
        Car[] cars = getOpCars();
        if (world.getTick() > 1.5 * startTick) {
            for (Car car : cars) {
                double angle = abs(self.getAngleTo(car));
                int distToCar = (int) self.getDistanceTo(car);
                if (angle < PI / 100 && distToCar < 2.5 * tileSize) {
                    move.setThrowProjectile(true);
                }
                if (angle > 3 * PI / 4 && distToCar < tileSize) {
                    move.setSpillOil(true);
                }
            }
        }
        activateNitroIfNeed();
    }

    /**
     * включаем нитро, если нужно
     */
    private void activateNitroIfNeed() {
        if (world.getTick() < startTick) {
            move.setUseNitro(false);
            return;
        }
        if (move.isBrake()) {
            move.setUseNitro(false);
            return;
        }

        int distToWall = distanceHelper.getDistanceToWallByCarDirection(PI / 360);
        if (world.getTick() == startTick && distToWall > 2 * tileSize) {
            move.setUseNitro(true);
            return;
        }

        if (distToWall < 3 * tileSize) {
            move.setUseNitro(false);
            return;
        }

        if (abs(self.getEnginePower() - 1) < 0.01) {
            move.setUseNitro(true);
            return;
        }

        int beforeTurn = getTilesBeforeTurn();
        Vector direct = new Vector(carDirection);
        direct.normalize();
        if (beforeTurn > 4 && (abs(direct.x) < 0.1 || abs(direct.y) < 0.1)) {
            move.setUseNitro(true);
        }
    }

    /**
     * вычесление следующией точки в направлении которой будем двигаться
     *
     * @return АБСОЛЮТНЫЕ координаты точки
     */
    private PairIntInt getNextWayPoint() {
        nextTile = getNextTile();
        //устанавливаем точку - середину следующего тайла
        PairIntInt nextPoint = new PairIntInt((int) ((nextTile.first + 0.5) * tileSize),
                (int) ((nextTile.second + 0.5) * tileSize));

        TileType nextTileType = clasifyNextTile();
        correctPointByTileType(nextPoint, nextTileType);

        if (directToNextKeyPoint.size() > 2) {
            int curDirect = getCurDirection();
            int dist = -(abs(curDirect) == 1 ? curDirect * relativeX : (curDirect / 2) * relativeY);
            if (dist < 0) {
                dist += tileSize;
            }
            if (dist > tileSize) {
                dist -= tileSize;
            }
            if (getTilesBeforeTurn() == 2 && dist < tileSize / 10) {
                int nnDirect = directToNextKeyPoint.get(2);
                if (abs(nnDirect) == 1) {
                    if (abs(self.getX() - nextPoint.first) < (tileSize / 3)) {
                        nextPoint.first -= nnDirect * (tileSize / 3);
                    }
                } else {
                    if (abs(self.getY() - nextPoint.second) < (tileSize / 3)) {
                        nextPoint.second -= (nnDirect / 2) * (tileSize / 3);
                    }
                }
            } else {
                // если выполняем двойной поворот
                if (curDirect != directToNextKeyPoint.get(0)
                        && directToNextKeyPoint.get(0) != directToNextKeyPoint.get(1)) {
                    correctPointDoubleTurn(nextPoint);
                }
            }
        }

        return nextPoint;
    }

    private void correctPointDoubleTurn(PairIntInt nextPoint) {
        /* if (directToNextKeyPoint == null
         || directToNextKeyPoint.size() < 3
         || self.getDistanceTo(nextPoint.first, nextPoint.second) > 9 * tileSize / 10) {
         return;
         }

         int curDirect = getCurDirection();
         //int curDirect = directToNextKeyPoint.get(0);
         // едем почти прямо
         if (curDirect == directToNextKeyPoint.get(1)) {
         int nextDirect = directToNextKeyPoint.get(0);
         // едем по х
         if (abs(nextDirect) == 1) {
         nextPoint.first += nextDirect * tileSize / 3;
         } else {
         nextPoint.second += (nextDirect / 2) * tileSize / 3;
         // едем по у 
         }
         } else*/ {
            /*         // поворот на 180
             PairIntInt nnTile = wayToNextKeyPoint.get(1);
             // едем по х
             if (abs(curDirect) == 1) {
             nextPoint.second += (curTileY - nnTile.second) * (tileSize / 2);
             } else {
             nextPoint.first += (curTileX - nnTile.first) * (tileSize / 2);
             }
             */
        }
    }

    /**
     * корректируем точку в зависимости от типа тайла
     *
     */
    private void correctPointByTileType(PairIntInt nextPoint, TileType nextTileType) {
        // максимально допустимое смещение машинки от центра тайла
        double cornerTileOffset = (tileSize / 2) - (marginSize + carWidth / 2);
        // коэфициент отклонения от текущий координаты в случае движения вертикально или горизонтально
        double koef = 0.2;

        switch (nextTileType) {
            case LEFT_TOP_CORNER:
                nextPoint.first += cornerTileOffset;
                nextPoint.second += cornerTileOffset;
                break;
            case RIGHT_TOP_CORNER:
                nextPoint.first -= cornerTileOffset;
                nextPoint.second += cornerTileOffset;
                break;
            case LEFT_BOTTOM_CORNER:
                nextPoint.first += cornerTileOffset;
                nextPoint.second -= cornerTileOffset;
                break;
            case RIGHT_BOTTOM_CORNER:
                nextPoint.first -= cornerTileOffset;
                nextPoint.second -= cornerTileOffset;
                break;
            case VERTICAL: /*if (mapTiles[curTileX][curTileY] == TileType.VERTICAL) */ {
                nextPoint.first = (int) (nextPoint.first - 0.5 * tileSize
                        + relativeX + (koef * (tileSize / 2 - relativeX)));
            }
            break;
            case HORIZONTAL: /*if (mapTiles[curTileX][curTileY] == TileType.HORIZONTAL)*/ {
                nextPoint.second = (int) (nextPoint.second - 0.5 * tileSize
                        + relativeY + (koef * (tileSize / 2 - relativeY)));
            }
            break;
            default:
        }
    }

    /**
     * вычисление условного типа следующего тайла например, если мы подъезжаем к
     * Т-угольному перекрестку, а на нем поворачиваем влево то получим поворот
     * ввлево
     *
     * @return LEFT_TOP_CORNER || RIGHT_TOP_CORNER || LEFT_BOTTOM_CORNER ||
     * RIGHT_BOTTOM_CORNER || VERTICAL || HORIZONTAL
     */
    private TileType clasifyNextTile() {
        TileType nextTileType = TileType.UNKNOWN;
        switch (mapTiles[nextTile.first][nextTile.second]) {
            case LEFT_TOP_CORNER:
            case RIGHT_TOP_CORNER:
            case LEFT_BOTTOM_CORNER:
            case RIGHT_BOTTOM_CORNER:
            case VERTICAL:
            case HORIZONTAL:
                nextTileType = mapTiles[nextTile.first][nextTile.second];
                break;
            default:
                if (wayToNextKeyPoint.size() > 1) {
                    PairIntInt nnTile = wayToNextKeyPoint.get(1);
                    if (nnTile.first == curTile.first) {
                        nextTileType = TileType.VERTICAL;
                        break;
                    }
                    if (nnTile.second == curTile.second) {
                        nextTileType = TileType.HORIZONTAL;
                        break;
                    }
                    if (nextTile.first == curTile.first + 1) {
                        nextTileType = nnTile.second > curTile.second ? TileType.RIGHT_TOP_CORNER : TileType.RIGHT_BOTTOM_CORNER;
                        break;
                    }
                    if (nextTile.first == curTile.first - 1) {
                        nextTileType = nnTile.second > curTile.second ? TileType.LEFT_TOP_CORNER : TileType.LEFT_BOTTOM_CORNER;
                        break;
                    }
                    if (nextTile.second == curTile.second + 1) {
                        nextTileType = nnTile.first > curTile.first ? TileType.LEFT_BOTTOM_CORNER : TileType.RIGHT_BOTTOM_CORNER;
                        break;
                    }
                    if (nextTile.second == curTile.second - 1) {
                        nextTileType = nnTile.first > curTile.first ? TileType.LEFT_TOP_CORNER : TileType.RIGHT_TOP_CORNER;
                        break;
                    }
                }
        }
        return nextTileType;
    }

    /**
     *
     * @return тайл в который нужно ехать автомобилю
     */
    protected PairIntInt getNextTile() {
        return getNextTileByBFS(self.getNextWaypointX(), self.getNextWaypointY(), curTileX, curTileY);
    }

}
