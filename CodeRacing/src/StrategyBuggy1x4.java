
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

    @Override
    public void move() {
        calculateCurTile();

        Vector speed = new Vector(self.getSpeedX(), self.getSpeedY());

        Point nextWayPoint = getNextWayPoint();
        double angleToWaypoint = self.getAngleTo(nextWayPoint.x, nextWayPoint.y);
        double wheelTurn = angleToWaypoint;// * 32.0D / PI;
        move.setWheelTurn(wheelTurn);

        calulateEnginePower(angleToWaypoint, wheelTurn, speed);
        
        activateIfNeedBreaks(angleToWaypoint, speed);
        
        activateIfNeedAmmo();
        //int[][] wayPoints= world.getWaypoints();
    }

    /**
     * вычисляет значение мощности двигателя и устанавливает в move
     */
    private void calulateEnginePower(double angleToWaypoint, double wheelTurn, Vector speed) {
        int distanceToWall = getDistanceToWall(PI/6);
        if (abs(wheelTurn) < 0.7) {
            if (distanceToWall >= carHeight * 2.5) {
                move.setEnginePower(1.0D);
                if (distanceToWall >= carHeight * 3 && speed.length() > 0 && abs(wheelTurn)<0.3) {
                    move.setUseNitro(true);
                }
            } else if (distanceToWall > 2 * carHeight) {
                move.setEnginePower(0.75D);
            } else {
                move.setEnginePower(0.1D);
            }
        } else {
            if (abs(wheelTurn) < 0.8) {
                move.setEnginePower(0.1);
            } else {
                move.setEnginePower(-1);
            }
        }

        if (abs(angleToWaypoint) > PI / 10 && distanceToWall < 2 * carHeight) {
            move.setEnginePower(-1.0D);
        }

        int dist2= getDistanceToWall(PI/60);
        if (dist2> carHeight*3 && wheelTurn<1) {
            move.setEnginePower(0.9);
        }
    }

    /**
     * включает, если нужно тормоза
     * @param angleToWaypoint
     * @param speed 
     */
    private void activateIfNeedBreaks(double angleToWaypoint, Vector speed) {
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
    }
    
    /**
     *
     * @return точка в направлении которой машина будет двигаться
     */
    private Point getNextWayPoint() {
        double nextWaypointX = (self.getNextWaypointX() + 0.5D) * game.getTrackTileSize();
        double nextWaypointY = (self.getNextWaypointY() + 0.5D) * game.getTrackTileSize();

        double cornerTileOffset = 0.25D * tileSize;
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
            default:
        }

        return new Point(nextWaypointX, nextWaypointY);
    }
    
    int getDistanceToWall(Car car, double deltaAngle) {
        int carColor= getColorOfCar(car);
        int carX= (int) getRelativeCoordinate(car.getX());
        int carY= (int) getRelativeCoordinate(car.getY());
        Vector carSpeed = new Vector(car.getSpeedX(), car.getSpeedY());
        if (carSpeed.length() < 1e-1) {
            return tileSize;
        }
        carSpeed.normalize();
        while (max(abs(carSpeed.x), abs(carSpeed.y)) <= 1) {
            carSpeed.x *= 1.02;
            carSpeed.y *= 1.02;
        }
        //double angle = carSpeed.getAngleToOX();
        final int numberOfTurns = 40;
       // double deltaAngle = PI / 6;
        double turnAngle;// = 2 * deltaAngle / numberOfTurns;

        for (int d = 1; d <= tileSize; d++) {
            Vector vector = new Vector(carSpeed);
            vector.rotateVector(-deltaAngle);

            turnAngle = 2 * deltaAngle / numberOfTurns;
            //numberOfTurns = (int) (deltaAngle * 2 / turnAngle);
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

                if (getCurTile(x, y) != carColor && getCurTile(x, y) != empty) {
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

    void calculateCurTile() {
        curTile = tileToMatrix.getMatrix(mapTiles[curTileX][curTileY]);

//        printCurTileToFile(curTile);
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
        int carWidth = 140;//(int) car.getWidth();
        int carHeight = 210;//(int) car.getHeight();

        double x = getRelativeCoordinate(car.getX());
        double y = getRelativeCoordinate(car.getY());
        Point carCenter = new Point(x, y);

        double angle = car.getAngle();
        Vector carVector = new Vector();
        carVector.getVectorByAngle(angle);

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

    private int getCurTile(int x, int y) {
        if (x < 0 || y < 0 || x >= tileSize || y >= tileSize) {
            return wall;
        }
        //return curTile[tileSize - y - 1][x];
        return curTile[y][x];
    }

    private boolean setCurTile(int x, int y, int val) {
        if (x < 0 || y < 0 || x >= tileSize || y >= tileSize) {
            return false;
        }
        //curTile[tileSize - y - 1][x] = val;
        curTile[y][x] = val;
        return true;
    }

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
