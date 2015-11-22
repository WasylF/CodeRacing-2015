
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
    private static final int numberOfTickToGoBack = 120;
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
        double angleToWaypoint = carDirection.getAngle(toNextWayPoint);

        double wheelTurn = getWheelTurn(angleToWaypoint);

        calulateEnginePower(angleToWaypoint, wheelTurn, speed);

        activateIfNeedBreaks(angleToWaypoint, speed);

        activateIfNeedAmmo();
    }

    private boolean shouldGetBonus() {
        Bonus[] bonuses = world.getBonuses();
        for (Bonus bonus : bonuses) {
            PairIntInt bonusCoordinate = new PairIntInt(bonus.getX(), bonus.getY());
            PairIntInt bonusTile = getTileOfObject(bonus.getX(), bonus.getY());
            if (getTileDistance(curTile, bonusTile) < 2) {
                Vector toBonus = new Vector(bonus.getX() - self.getX(), bonus.getY() - self.getY());
                Vector toWayPoint = new Vector(new Point(self.getX(), self.getY()), new Point(nextWayPoint.first, nextWayPoint.second));
                if (abs(toBonus.getAngle(toWayPoint)) > PI / 20) {
                    continue;
                }
                double angleToBonus = abs(curSpeed.getAngle(toBonus));
                if ((angleToBonus < PI / 60)
                        || (angleToBonus < PI / 15 && self.getDistanceTo(bonus) > tileSize)
                        || (angleToBonus < PI / 10 && bonus.getType() == BonusType.PURE_SCORE)
                        || (angleToBonus < PI / 10 && self.getDurability() < 0.9
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
        Vector opCarDirect = new Vector(carDirection);
        opCarDirect.rotateVector(PI);
        //если уперлись задом в стенку, то нужно ехать вперед
        if (distanceHelper.getDistanceToWall(self, PI / 30, opCarDirect) < 2 * carHeight / 3) {
            goBack = 0;
            return false;
        }

        if (goBack > 0) {
            return true;
        }

        if (world.getTick() - startTick > 100 && curSpeed.length() < 1e-1 && previousSpeed.length() < 1e-1) {
            goBack = numberOfTickToGoBack;
            return true;
        }
        int distToWall = distanceHelper.getDistanceToWallByCarDirection(PI / 60);

        if ((abs(distToWall - carHeight / 2) < 20)
                || (distToWall <= carHeight && curSpeed.module() >= 10)
                || (distToWall < carHeight && goBack == 0
                && curSpeed.module() == 0
                && world.getTick() > startTick + 10)) {
            goBack = numberOfTickToGoBack;
            System.out.println("goBack starts!");
            return true;
        }
        return false;
    }

    /**
     * совершения хода, при котором машина сдает назад
     */
    private void goingBack() {
        Vector carVector = new Vector(self.getAngle());
        //1 - едем вперед, -1 - назад
        int goingDirection = curSpeed.getPositiveAngle(carVector) <= PI / 2 ? 1 : -1;
        move.setBrake(false);
        move.setSpillOil(false);
        move.setUseNitro(false);
        if (goBack > 0.4 * numberOfTickToGoBack) {
            move.setEnginePower(-1.0);
            //если скорость направленна в сторону капота
            if (goingDirection == 1 && curSpeed.length() > 5e-1) {
                move.setBrake(true);
                move.setEnginePower(0);
                if (goBack % 2 == 0) {
                    goBack += 3;
                }
            }
            /* if (goBack % 3 == 0) {
             goBack++;
             }*/
        } else {
            // если скорость направленна в сторону капота
            if (goingDirection == 1 || curSpeed.module() < 2) {
                goBack *= 1.5;
            } else {
                if (curSpeed.module() > 2) {
                    move.setBrake(true);
                    //goBack++;
                } else {
                    if (goBack > 0.1 * numberOfTickToGoBack) {
                        move.setEnginePower(0.01);
                    } else {
                        move.setEnginePower(1);
                    }
                }
            }
        }
        if (goingDirection == 1 || goBack >= 0.6 * numberOfTickToGoBack) {
            move.setWheelTurn(0);
        } else {
            Vector toNextWasyPoint = new Vector(nextWayPoint.first - self.getX(), nextWayPoint.second - self.getY());
            double angleToWaypoint = curSpeed.getAngle(toNextWasyPoint);

            if (goBack >= 0.4 * numberOfTickToGoBack) {
                move.setWheelTurn(signum(getWheelTurn(angleToWaypoint)));
                if (abs(angleToWaypoint) > PI / 2 && curSpeed.module() == 0) {
                    goBack += 2;
                }
            } else {
                getWheelTurn(angleToWaypoint);
            }
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

        if (abs(angleToWaypoint) > PI / 3
                && abs(angleToWaypoint) * speed.length() * speed.length() > 2.5D * 2.5D * PI) {
            move.setBrake(true);
            return;
        }

        if (speed.length() > 10 && willTurn(nextTile.first, nextTile.second)
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
        if (world.getTick() != 0 && world.getTick() % 555 == 0) {
            move.setThrowProjectile(true);
            move.setSpillOil(true);
        }
        if (world.getTick() > startTick && abs(move.getEnginePower() - 1) < 0.05
                && !move.isBrake()) {
            move.setUseNitro(true);
        }
        if (world.getTick() == startTick) {
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

        // максимально допустимое смещение машинки от центра тайла
        double cornerTileOffset = (tileSize / 2) - (marginSize + carWidth / 2);
        // коэфициент отклонения от текущий координаты в случае движения вертикально или горизонтально
        double koef = 0.2;

        switch (mapTiles[nextTile.first][nextTile.second]) {
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
            case VERTICAL:
                if (mapTiles[curTileX][curTileY] == TileType.VERTICAL) {
                    nextPoint.first = (int) (nextPoint.first - 0.5 * tileSize
                            + relativeX + (koef * (tileSize / 2 - relativeX)));
                }
                break;
            case HORIZONTAL:
                if (mapTiles[curTileX][curTileY] == TileType.HORIZONTAL) {
                    nextPoint.second = (int) (nextPoint.second - 0.5 * tileSize
                            + relativeY + (koef * (tileSize / 2 - relativeY)));
                }
                break;
            default:
        }

        return nextPoint;
    }

    /**
     *
     * @return тайл в который нужно ехать автомобилю
     */
    protected PairIntInt getNextTile() {
        return getNextTileByBFS(self.getNextWaypointX(), self.getNextWaypointY(), curTileX, curTileY);
    }

}
