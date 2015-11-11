
import java.io.FileWriter;
import java.io.IOException;
import java.lang.*;
import static java.lang.StrictMath.*;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import model.Car;
import model.Game;
import model.Move;
import model.World;

/**
 *
 * @author Wsl_F
 */
public class StrategyBuggy1x4 extends StrategyWslF {

    public void move(Car self, World world, Game game, Move move) {
        initAll(self, world, game, move);

        calculateCurTile();

   //     printCurTileToFile();

        double nextWaypointX = (self.getNextWaypointX() + 0.5D) * game.getTrackTileSize();
        double nextWaypointY = (self.getNextWaypointY() + 0.5D) * game.getTrackTileSize();

        double cornerTileOffset = 0.25D * game.getTrackTileSize();

        switch (world.getTilesXY()[self.getNextWaypointX()][self.getNextWaypointY()]) {
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

        double angleToWaypoint = self.getAngleTo(nextWaypointX, nextWaypointY);
        double speedModule = hypot(self.getSpeedX(), self.getSpeedY());

        move.setWheelTurn(angleToWaypoint * 32.0D / PI);
        int distanceToWall = getDistanceToWall();
        if (distanceToWall >= self.getHeight() * 1.5) {
            move.setEnginePower(1.0D);
            if (distanceToWall >= tileSize / 2 && speedModule > 0) {
                move.setUseNitro(true);
            }
        } else if (distanceToWall > self.getHeight()) {
            move.setEnginePower(0.85D);
        } else {
            move.setEnginePower(0.5D);
        }

        if (world.getTick() != 0 && world.getTick() % 555 == 0) {
            move.setThrowProjectile(true);
            move.setSpillOil(true);
        }

        if (abs(angleToWaypoint) > PI / 6 && distanceToWall < 2 * self.getHeight()) {
            move.setEnginePower(-1.0D);
        }

        if (abs(angleToWaypoint) > PI / 4
                && speedModule * speedModule * abs(angleToWaypoint) > 2.5D * 2.5D * PI) {
            move.setBrake(true);
        }

    }

    int getDistanceToWall() {
        /*       Point carSpeed = new Point(self.getSpeedX(), self.getSpeedY());
         carSpeed.normalize();
         if (hypot(self.getSpeedX(), self.getSpeedY()) < 1e-1) {
         return tileSize;
         }
         while (max(abs(carSpeed.x), abs(carSpeed.y)) <= 1) {
         carSpeed.x *= 1.5;
         carSpeed.y *= 1.5;
         }
         //double angle = carSpeed.getAngleToOX();
         final int numberOfTurns = 20;
         double deltaAngle = PI / 6;
         double turnAngle = 2 * deltaAngle / numberOfTurns;

         for (int d = 1; d <= tileSize; d++) {
         Point leftVector = new Point(carSpeed);
         leftVector.rotateVector(-deltaAngle);

         turnAngle = 2 * deltaAngle / numberOfTurns;
         //numberOfTurns = (int) (deltaAngle * 2 / turnAngle);
         for (int i = 0; i <= numberOfTurns; i++) {
         int x = (int) (selfX + d * leftVector.x);
         int y = (int) (selfY + d * leftVector.y);
         if (x <= 0 || y <= 0 || x >= tileSize || y >= tileSize) {
         x = getAccurateCoordinate(x);
         y = getAccurateCoordinate(y);
         if (curTile[x][y] == selfCar || curTile[x][y] == empty) {
         return tileSize;
         } else {
         return d;
         }
         }

         if (curTile[x][y] != selfCar && curTile[x][y] != empty) {
         return d;
         }
         leftVector.rotateVector(turnAngle);
         }
         if (deltaAngle > PI / 30) {
         deltaAngle -= turnAngle / 10;
         }
         }*/
        return tileSize;
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
        curTile[y][x]= val;
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
