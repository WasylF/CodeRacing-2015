
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

        int[][] curTile = getCurTile();

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
        move.setEnginePower(0.75D);

        if (speedModule * speedModule * abs(angleToWaypoint) > 2.5D * 2.5D * PI) {
            move.setBrake(true);
        }

    }

    int getDistanceToWall(int[][] curTile) {
        Point carSpeed = new Point(self.getSpeedX(), self.getSpeedY());
        carSpeed.normalize();
        //double angle = carSpeed.getAngleToOX();
        {
            int x = selfX;
            int y = selfY;
            double k = 1;
            try {
                if (carSpeed.x > 1e-3) {
                    k = 1 / carSpeed.x;
                } else {
                    k = 1 / carSpeed.y;
                }
            } catch (Exception ex) {
                k = 1;
            }

            int count = 0;
            while (x < tileSize && y < tileSize && x >= 0 && y >= 0
                    && (curTile[x][y] == selfCar
                    || (count < self.getHeight() / 4 && curTile[x][y] == empty))) {
                if (count > 0) {
                    count++;
                }
                x += (int) k * carSpeed.x + 0.1;
                y += (int) k * carSpeed.y + 0.1;
                if (x < tileSize && y < tileSize && x >= 0 && y >= 0
                        && curTile[x][y] == empty) {
                    count = 1;
                }
            }

            if (x == tileSize || y == tileSize || x <= 0 || y <= 0) {
                return tileSize;
            }

            curTile[x][y] = 1;
            Queue<Integer> q = new LinkedBlockingQueue<>();
            q.add(x * tileSize + y);
            while (!q.isEmpty()) {
                x = q.peek() / tileSize;
                y = q.peek() % tileSize;
                q.remove();
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (abs(i + j) != 2) {
                            if (x + i >= 0 && x + i < tileSize && y + j >= 0 && y + i < tileSize) {
                                if (curTile[x + i][y + j] == wall) {
                                    return curTile[x][y];
                                } 
                                if (curTile[x+i][y+j] == empty) {
                                    curTile[x+i][y+j]= curTile[x][y]+1;
                                    q.add((x+i)*tileSize+y+j);
                                }
                              
                            }
                        }
                    }
                }
            }
        }
    }

    int[][] getCurTile() {
        int[][] curTile = tileToMatrix.getMatrix(mapTiles[curTileX][curTileY]);

        Point[] rectangleCar = getCarVertexCoordinates();
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
                    curTile[i][j] = selfCar;
                }
            }
        }
        return curTile;
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

    private Point[] getCarVertexCoordinates() {
        Point carCenter = new Point(self.getX() - curTileX * game.getTrackTileSize(), self.getY() - curTileY * game.getTrackTileSize());
        //int carX = (int) (carCenter.x + 0.1);
        //int carY = (int) (carCenter.y + 0.1);

        double angle = -self.getAngle();
        int carWidth = (int) self.getWidth();
        int carHeight = (int) self.getHeight();

        Point a = new Point(carCenter.x - carHeight * cos(angle) / 2 - carWidth * sin(angle) / 2,
                carCenter.y - carHeight * cos(angle) / 2 + carWidth * cos(angle) / 2);
        Point b = new Point(carCenter.x + carHeight * cos(angle) / 2 - carWidth * sin(angle) / 2,
                carCenter.y + carHeight * sin(angle) / 2 + carWidth * cos(angle) / 2);

        Point c = a.getSymmetric(carCenter);
        Point d = b.getSymmetric(carCenter);

        return new Point[]{a, b, c, d};
    }

}
