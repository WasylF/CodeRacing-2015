
import java.io.FileWriter;
import java.io.IOException;
import java.lang.*;
import static java.lang.StrictMath.*;
import java.util.Queue;
import model.Car;

/**
 *
 * @author Wsl_F
 */
public class StrategyBuggy1x4 extends StrategyWslF {

    private boolean useBreaks;

    @Override
    public void move() {
        calculateCurTile();

        Vector speed = new Vector(self.getSpeedX(), self.getSpeedY());

        Point nextWayPoint = getNextWayPoint();
        Vector toNextWasyPoint = new Vector(nextWayPoint.x - self.getX(), nextWayPoint.y - self.getY());
        double angleToWaypoint = speed.getAngle(toNextWasyPoint);
        //self.getAngleTo(nextWayPoint.x, nextWayPoint.y);

        double wheelTurn = getWheelTurn(angleToWaypoint);

        calulateEnginePower(angleToWaypoint, wheelTurn, speed);

        activateIfNeedBreaks(angleToWaypoint, speed);

        activateIfNeedAmmo();
        //int[][] wayPoints= world.getWaypoints();
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
        move.setEnginePower(0.8);
        /*        int distanceToWall = getDistanceToWall(PI / 6);
         int dist2 = getDistanceToWall(PI / 60);

         useBreaks = false;
         Vector car = new Vector(self.getAngle());

         if (car.getPositiveAngle(speed) > PI / 3) {
         move.setEnginePower(1.0);
         return;
         }

         if (abs(angleToWaypoint) > PI / 4) {
         move.setEnginePower(0);
         return;
         }

         if (distanceToWall > carHeight * 0.5
         && dist2 > carHeight * 2.5 && abs(angleToWaypoint) < PI / 6) {
         if (wheelTurn < 0.2) {
         move.setEnginePower(1.0);
         } else if (wheelTurn < 0.3) {
         move.setEnginePower(0.8);
         } else if (wheelTurn < 0.4) {
         move.setEnginePower(0.6);
         } else if (wheelTurn < 0.5) {
         move.setEnginePower(0.1);
         } else {
         useBreaks = true;
         move.setEnginePower(-0.1);
         if (wheelTurn > 0.8) {
         move.setEnginePower(-1);
         }
         }

         return;
         }

         if (distanceToWall > carHeight
         && dist2 > carHeight
         * 1.5 && abs(angleToWaypoint)
         < PI / 9) {
         move.setEnginePower(0.85);
         return;
         }

         move.setEnginePower(0.1);*/
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
        if (abs(angleToWaypoint) > PI / 8
                && speed.length() * speed.length() * abs(angleToWaypoint) > 2.5D * 2.5D * PI) {
            move.setBrake(true);
        }

    }

    private void activateIfNeedAmmo() {
        if (world.getTick() != 0 && world.getTick() % 555 == 0) {
            move.setThrowProjectile(true);
            move.setSpillOil(true);
        }
        if (world.getTick() > 180 && abs(move.getEnginePower() - 1) < 0.05) {
            move.setUseNitro(true);
        }
        if (world.getTick() == 182) {
            move.setUseNitro(true);
        }
    }

    /**
     * отодвигает координату от стены, если расстояние до стены 2/3 ширины
     * корпуса
     *
     * @param t координата
     * @return пересчитаная (если нужно) координата
     */
    private double getNotToCloseToWall(double t) {
        if (t < marginSize + 2 * carWidth / 3) {
            t = marginSize + 2 * carWidth / 3;
        }
        if (t > marginSize + tileSize - 2 * carWidth / 3) {
            t = tileSize - (marginSize + 2 * carWidth / 3);
        }
        return t;
    }

    /**
     *
     * @return точка в направлении которой машина будет двигаться
     */
    private Point getNextWayPoint() {
        final double koef = 0.35;
        double nextWaypointX = (self.getNextWaypointX() + 0.5D) * tileSize;
        double nextWaypointY = (self.getNextWaypointY() + 0.5D) * tileSize;

        double curX = getNotToCloseToWall(selfX);
        double curY = getNotToCloseToWall(selfY);

        double cornerTileOffset = (tileSize / 2) - (marginSize + carWidth / 2);
        switch (mapTiles[self.getNextWaypointX()][self.getNextWaypointY()]) {
            case LEFT_TOP_CORNER:
                nextWaypointX += cornerTileOffset;
                nextWaypointY += cornerTileOffset;
                break;
            case RIGHT_TOP_CORNER:
                nextWaypointX -= cornerTileOffset;
                nextWaypointY += cornerTileOffset;
                break;
            case LEFT_BOTTOM_CORNER:
                nextWaypointX += cornerTileOffset;
                nextWaypointY -= cornerTileOffset;
                break;
            case RIGHT_BOTTOM_CORNER:
                nextWaypointX -= cornerTileOffset;
                nextWaypointY -= cornerTileOffset;
                break;
            case VERTICAL:
                nextWaypointX = nextWaypointX - 0.5 * tileSize + curX + koef * (tileSize / 2 - selfX);
                break;
            case HORIZONTAL:
                nextWaypointY = nextWaypointY - 0.5 * tileSize + curY + koef * (tileSize / 2 - selfY);
                break;
            default:
        }

        return new Point(nextWaypointX, nextWaypointY);
    }

    /**
     * находим расстояние от центра машины до ближайшей стены
     *
     * @param car машина
     * @param deltaAngle угол отклонения от вектора скорости
     * @return расстояние до стены
     */
    int getDistanceToWall(Car car, double deltaAngle) {
        int carColor = getColorOfCar(car);
        int carX = (int) getRelativeCoordinate(car.getX());
        int carY = (int) getRelativeCoordinate(car.getY());
        Vector carSpeed = new Vector(car.getSpeedX(), car.getSpeedY());
        if (carSpeed.length() < 1e-1) {
            return tileSize;
        }
        carSpeed.normalize();
        while (max(abs(carSpeed.x), abs(carSpeed.y)) <= 1) {
            carSpeed.x *= 1.02;
            carSpeed.y *= 1.02;
        }
        final int numberOfTurns = 40;
        double turnAngle;

        for (int d = 1; d <= tileSize; d++) {
            Vector vector = new Vector(carSpeed);
            vector.rotateVector(-deltaAngle);

            turnAngle = 2 * deltaAngle / numberOfTurns;
            for (int i = 0; i <= numberOfTurns; i++) {
                int x = (int) (carX + d * vector.x);
                int y = (int) (carY + d * vector.y);
                if (x <= 0 || y <= 0 || x >= tileSize || y >= tileSize) {
                    x = getAccurateCoordinate(x);
                    y = getAccurateCoordinate(y);
                    if (getCurTile(x, y) == carColor || getCurTile(x, y) == empty) {
                        return tileSize;
                    } else {
                        return d;
                    }
                }

                if (getCurTile(x, y) != wall) {
                    return d;
                }
                vector.rotateVector(turnAngle);
            }
            if (deltaAngle > PI / 30) {
                deltaAngle -= turnAngle / 50;
            }
        }
        return tileSize;
    }

    /**
     * находим расстояние от центра собственной машины до ближайшей стены
     *
     * @param deltaAngle угол отклонения от вектора скорости
     * @return расстояние до стены
     */
    int getDistanceToWall(double deltaAngle) {
        return getDistanceToWall(self, deltaAngle);
    }

    /**
     * возвращает корректную координату, то есть от 0 до tileSize-1
     *
     * @param t входная координата
     * @return подкорректированная
     */
    int getAccurateCoordinate(double t) {
        t = max(t, 0);
        t = min(t, tileSize - 1);
        return (int) t;
    }

    /**
     * получает схематическое изображение текущего тайла с учетом собстевенной
     * машины, планируется добавить машины соперников
     */
    void calculateCurTile() {
        curTile = tileToMatrix.getMatrix(mapTiles[curTileX][curTileY]);

        Point[] rectangleCar = getCarVertexCoordinates(self);
        Point minP = new Point();
        Point maxP = new Point();
        getMinMaxCoordinates(rectangleCar, minP, maxP);
        int xMin, xMax, yMin, yMax;
        xMin = max((int) minP.x - 1, 0);
        yMin = max((int) minP.y - 1, 0);
        xMax = min((int) maxP.x + 1, (int) game.getTrackTileSize() - 1);
        yMax = min((int) maxP.y + 1, (int) game.getTrackTileSize() - 1);

        for (int i = xMin; i <= xMax; i++) {
            for (int j = yMin; j <= yMax; j++) {
                Point p = new Point(i, j);
                if (p.checkPointInPolygon(rectangleCar)) {
                    setCurTile(i, j, selfCar);
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

    private double getRelativeCoordinate(double c) {
        return c - ((int) (c / tileSize)) * tileSize;
    }

    /**
     * возвращает значение в матрице таййла, с относительными координатами (х;у)
     *
     * @param x абсцисса
     * @param y ордината
     * @return значение в текущем тайле (стена/пусто/своя машина/...)
     */
    private int getCurTile(int x, int y) {
        if (x < 0 || y < 0 || x >= tileSize || y >= tileSize) {
            return wall;
        }
        return curTile[y][x];
    }

    /**
     * задает значение val в матрице таййла, с относительными координатами (х;у)
     *
     * @param x абсцисса
     * @param y ордината
     * @param val значение
     */
    private boolean setCurTile(int x, int y, int val) {
        if (x < 0 || y < 0 || x >= tileSize || y >= tileSize) {
            return false;
        }
        curTile[y][x] = val;
        return true;
    }

    /**
     * печатает в файл curTile.txt схематическое изображение текущего тайла
     */
    private void printCurTileToFile() {
        try (FileWriter writer = new FileWriter("curTile.txt", false)) {
            for (int x = 0; x < tileSize; x++) {
                String s = "";
                for (int y = 0; y < tileSize; y++) {
                    switch (curTile[x][y]) {
                        case selfCar:
                            s += '.';
                            break;
                        case wall:
                            s += '▓';
                            break;
                        case empty:
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
