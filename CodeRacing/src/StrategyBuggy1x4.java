
import java.io.FileWriter;
import java.io.IOException;
import java.lang.*;
import static java.lang.StrictMath.*;
import java.util.LinkedList;
import java.util.Queue;
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
     * скорость на предыдущем ходу
     */
    private Vector previousSpeed;
    /**
     * скорость на текущем ходу
     */
    private Vector curSpeed;
    /**
     * оставшееся количество тиков, в котороых машина будет ехать задним ходом.
     */
    private int goBack;
    /**
     * относительный угол поворота при заднем ходе
     */
    private double goBackWheelTurn;
    /**
     * копия предыдущего хода
     */
    private Move previousMove;

    /**
     * количество тиков в течении которых автомобиль должен сдавать задним ходом
     */
    private static final int numberOfTickToGoBack = 120;
    /**
     * точка, в направлении которой должен двигаться автомобиль координаты
     * АБСОЛЮТНЫЕ
     */
    private Point nextWayPoint;

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
        calculateCurTile();
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

        Vector toNextWayPoint = new Vector(nextWayPoint.x - self.getX(), nextWayPoint.y - self.getY());
        double angleToWaypoint = speed.getAngle(toNextWayPoint);
        //self.getAngleTo(nextWayPoint.x, nextWayPoint.y);

        double wheelTurn = getWheelTurn(angleToWaypoint);

        calulateEnginePower(angleToWaypoint, wheelTurn, speed);

        activateIfNeedBreaks(angleToWaypoint, speed);

        activateIfNeedAmmo();
        //int[][] wayPoints= world.getWaypoints();        
    }

    /**
     * проверяем, нужно ли сдавать задний ход
     *
     * @return тру, если нужно
     */
    private boolean shouldGoBack() {
        Vector opCarDirect = new Vector(self.getAngle() + PI);
        //если уперлись задом в стенку, то нужно ехать вперед
        if (getDistanceToWall(self, PI / 30, opCarDirect) < 2 * carHeight / 3) {
            goBack = 0;
            return false;
        }

        if (goBack > 0) {
            return true;
        }

        if (world.getTick() - startTick > 100 && curSpeed.length() < 1e-3 && previousSpeed.length() < 1e-3) {
            goBack = numberOfTickToGoBack;
            return true;
        }
        int distToWall = getDistanceToWallByCarDirection(PI / 60);

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
        // если на предыдущем ходу тоже сдавали задний ход
       /* if (abs(previousMove.getEnginePower() + 1) < 1e-2) {
         move.setWheelTurn(previousMove.getWheelTurn());
         } else {
         move.setWheelTurn(signum(previousMove.getWheelTurn()) * 1);
         }*/
        if (goingDirection == 1 || goBack >= 0.6 * numberOfTickToGoBack) {
            move.setWheelTurn(0);
        } else {
            Vector toNextWasyPoint = new Vector(nextWayPoint.x - self.getX(), nextWayPoint.y - self.getY());
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
            //printWorldMapToFile("WorldMap" + world.getTick() + ".txt");
        } else {
            if (world.getTick() == 10) {
                worldMap = calculateWorldMap(worldTileSize);
                //printWorldMapToFile("WorldMapNew" + world.getTick() + ".txt");
            }
            /*     if (world.getTick() == 100) {
             allWayPoints = new int[1000][2];
             calculateAllWayPoints();
             curPositionInAllPoints = 0;
             }
             */
            if (world.getTick() == 100) {
                for (int x = worldWidth-1; x >= 0; x--) {
                    for (int y = worldHeight-1; y >= 0; y--) {
                        if (mapTiles[x][y] != TileType.EMPTY) {
                            getNextTilesFor(x, y);
                        }
                    }
                }
            }
        }

        finalizeMove();
        System.out.println("Tick №" + world.getTick() + " ends");
    }

    /**
     * отладочный вывод в консоль
     */
    private void printDebug() {
        System.out.println("car center: (" + selfX + " , " + selfY + ")   speed: " + curSpeed + "   curEnginePower: " + move.getEnginePower());
        if (curSpeed.y > 0 && goBack > 0) {
            for (int i = 0; i < 10; i++) {
                System.out.println("Speed>0!!!!!!!!!!!      timeDiff: " + (numberOfTickToGoBack - goBack));
            }
        }
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

    /**
     * активирует расходники, если нужно
     */
    private void activateIfNeedAmmo() {
        if (world.getTick() != 0 && world.getTick() % 555 == 0) {
            move.setThrowProjectile(true);
            move.setSpillOil(true);
        }
        if (world.getTick() > startTick && abs(move.getEnginePower() - 1) < 0.05) {
            move.setUseNitro(true);
        }
        if (world.getTick() == 182) {
            move.setUseNitro(true);
        }
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
     *
     * @return точка в направлении которой машина будет двигаться
     */
    private Point getNextWayPoint() {
        /*        int nextTileX = self.getNextWaypointX();
         int nextTileY = self.getNextWaypointY();
         Point nextPoint = getNextWayPoint(nextTileX, nextTileY);
         while (nextPoint.equals(new Point(-1, -1))) {
         int[][] tWayPoints = world.getWaypoints();
         int[][] systemWayPoints = new int[tWayPoints.length + 1][2];
         System.arraycopy(tWayPoints, 0, systemWayPoints, 0, tWayPoints.length);
         systemWayPoints[tWayPoints.length] = systemWayPoints[0];
         int k = 0;
         while (systemWayPoints[k][0] != nextTileX || systemWayPoints[k][1] != nextTileY) {
         k++;
         }
         nextTileX = systemWayPoints[k + 1][0];
         nextTileY = systemWayPoints[k + 1][1];
         nextPoint = getNextWayPoint(nextTileX, nextTileY);
         }
         return nextPoint;*/
        int curTileX = (int) (self.getX() / game.getTrackTileSize());
        int curTileY = (int) (self.getY() / game.getTrackTileSize());
        PairIntInt nextTile = getNextTile(curTileX, curTileY);

        final double koef = 0.35;
        double nextWaypointX = (nextTile.first + 0.5D) * tileSize;
        double nextWaypointY = (nextTile.second + 0.5D) * tileSize;

        double curX = getNotToCloseToWall(selfX);
        double curY = getNotToCloseToWall(selfY);

        double cornerTileOffset = (tileSize / 2) - (marginSize + carWidth / 2);
        switch (mapTiles[nextTile.first][nextTile.second]) {
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

    private Point getNextWayPoint(int nextTileX, int nextTileY) {
        int nextWaypointX = nextTileX * worldTileSize;
        int nextWaypointY = nextTileY * worldTileSize;
        Point nextWaypoint = new Point(nextWaypointX, nextWaypointY);

        int[][] map = getCopyWorldMap();
        int carWorldX = convertToWorldCordinate(self.getX());
        int carWorldY = convertToWorldCordinate(self.getY());
//        setWorldMap(carWorldX, carWorldY, 1);

        {
            final int n = 10000;
            final int destination = -1000;

            Queue<Integer> queue = new LinkedList<>();
            Queue<Integer> qBack = new LinkedList<>();

            calculateStartPointsForBFS(queue, carWorldX, carWorldY, n);
            setFinalPointForBFS(nextTileX, nextTileY, destination);

            int distance;
            distance = ForwardBFS_ForNextWayPoint(queue, qBack, n, destination);

            LinkedList<Integer> possibleNext;
            if (distance < carHeight * 2 * worldTileSize / tileSize) {
                worldMap = map;
                return new Point(-1, -1);
            }
            possibleNext = BackwordBFS_ForNextWayPoint(qBack, n, distance);

            if (!possibleNext.isEmpty()) {
                nextWaypoint = getBestNextWaypoint(possibleNext, carWorldX, carWorldY, n);
            }
        }

        worldMap = map;

        nextWaypointX = (int) convertToAbsoluteCordinate(nextWaypoint.x);
        nextWaypointY = (int) convertToAbsoluteCordinate(nextWaypoint.y);

        nextWaypointX = (int) getNotToCloseToWall(nextWaypointX);
        nextWaypointY = (int) getNotToCloseToWall(nextWaypointY);

        return new Point(nextWaypointX, nextWaypointY);

    }

    /**
     * выбирает лучшею следующую ключевую точку по признаку минимального угла
     * между скоростю и направление до точки
     *
     * @param possibleNext множество точек среди которых ищем оптимальную
     * @param carWorldX абсцисса центра авто
     * @param carWorldY ордината центра авто
     * @param n множитель для обработки пары чисел в формате first*n+second
     * @return
     */
    private Point getBestNextWaypoint(LinkedList<Integer> possibleNext, int carWorldX, int carWorldY, int n) {
        Point nextWPoint = new Point();
        Vector direction = new Vector(curSpeed);
        if (direction.module() == 0) {
            direction.getVectorByAngle(self.getAngle());
        }

        double minAngle = PI;
        for (int cur : possibleNext) {
            int x = (int) getNotToCloseToWorldMapWall(cur / n);
            int y = (int) getNotToCloseToWorldMapWall(cur % n);
            double angle = direction.getPositiveAngle(new Vector(x - carWorldX, y - carWorldY));
            if (angle < minAngle) {
                minAngle = angle;
                nextWPoint.x = x;
                nextWPoint.y = y;
            }
        }

        return nextWPoint;
    }

    /**
     * поиска в ширину с целью нахождения следующей ключевой точки пути и
     * расстояния до следующей системной целевой точки
     *
     * @param queue очередь, содержащая стартовые точки
     * @param qBack очередь точек соседних с финальными
     * @param n множитель для обработки пары чисел в формате first*n+second
     * @param destination метка финальной точки
     * @return расстояния до следующей системной целевой точки
     */
    private int ForwardBFS_ForNextWayPoint(Queue<Integer> queue, Queue<Integer> qBack,
            int n, final int destination) {
        int x, y, cur;
        int distance = 2_000_000_000;

        while (!queue.isEmpty()) {
            cur = queue.poll();
            x = cur / n;
            y = cur % n;
            cur = getWorldMap(x, y);
            if (cur > distance) {
                continue;
            }
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (abs(i + j) == 1) {
                        int tmp = getWorldMap(x + i, y + j);
                        if (tmp == empty) {
                            setWorldMap(x + i, y + j, cur + 1);
                            queue.add((x + i) * n + (y + j));
                        }
                        if (tmp == destination) {
                            setWorldMap(x, y, 2 * cur);
                            qBack.add(x * n + y);
                            //qBack.add(new PointAnglePrev(x, y, 0, new Point()));
                            if (cur < distance) {
                                distance = cur;
                            }
                        }
                    }
                }
            }
        }

        return distance;
    }

    /**
     * обратный поиск в ширину для нахождения следующей ключевой точки пути
     *
     * @param qBack очередь достигнутых точек соседних с финальными
     * @param n множитель для обработки пары чисел в формате first*n+second
     * @param distance расстояние на котором ищем следующую ключевую точку
     * @return множество точек на расстоянии distance
     */
    private LinkedList<Integer> BackwordBFS_ForNextWayPoint(Queue<Integer> qBack,
            int n, int distance) {
        LinkedList<Integer> ans = new LinkedList<>();

        int x, y, cur;
        while (!qBack.isEmpty()) {
            cur = qBack.poll();
            x = cur / n;
            y = cur % n;
            cur = getWorldMap(x, y) / 2;
            setWorldMap(x, y, empty);
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (getWorldMap(x + i, y + j) == cur - 1) {
                        qBack.add((x + i) * n + (y + j));
                        setWorldMap(x + i, y + j, (cur - 1) * 2);
                        if (cur - 1 == distance) {
                            ans.add((x + i) * n + (y + j));
                        }
                        //qBack.add(new PointAnglePrev(x + i, y + j,));
                    }
                }
            }
        }
        return ans;
    }

    /**
     * назначаем крайние точки следующего ключевого тайла финальными точками для
     * поиска в ширину с целью найти следующую ключевую точку пути
     *
     * @param nextWaypointX текущая абсцисса ключевой точки
     * @param nextWaypointY текущая ордината ключевой точки
     * @param destination метка для обозначения финальной точки
     */
    private void setFinalPointForBFS(int nextTileX, int nextTileY, int destination) {
        int nextWaypointX = nextTileX * worldTileSize;
        int nextWaypointY = nextTileY * worldTileSize;

        int delta = worldMarginSize + (carWidth * worldTileSize / tileSize) / 2;
        switch (mapTiles[nextTileX][nextTileY]) {
            /*case HORIZONTAL:
             case VERTICAL:
             for (int i = 0; i < worldTileSize; i++) {
             if (getWorldMap(nextWaypointX, nextWaypointY + i) != wall) {
             setWorldMap(nextWaypointX, nextWaypointY + i, destination);
             }
             if (getWorldMap(nextWaypointX + worldTileSize - 1, nextWaypointY + i) != wall) {
             setWorldMap(nextWaypointX + worldTileSize - 1, nextWaypointY + i, destination);
             }
             if (getWorldMap(nextWaypointX + i, nextWaypointY) != wall) {
             setWorldMap(nextWaypointX + i, nextWaypointY, destination);
             }
             if (getWorldMap(nextWaypointX + i, nextWaypointY + worldTileSize - 1) != wall) {
             setWorldMap(nextWaypointX + i, nextWaypointY + worldTileSize - 1, destination);
             }
             }
             break;*/
            case LEFT_TOP_CORNER:
                setWorldMap(nextWaypointX + worldTileSize - delta,
                        nextWaypointY + worldTileSize - delta, destination);
                break;
            case RIGHT_TOP_CORNER:
                setWorldMap(nextWaypointX + delta,
                        nextWaypointY + worldTileSize - delta, destination);
                break;
            default:
                setWorldMap(nextWaypointX + worldTileSize / 2, nextWaypointY + worldTileSize / 2, destination);
        }

    }

    /**
     * вычисления начальных точек для поиска в ширину с целью нахождения
     * следующей ключевой точки пути
     *
     * @param queue очередь, в которую данные точки будут добавленны
     * @param carWorldX относительная абсцисса центра машины
     * @param carWorldY относительная ордината центра машины
     * @param n множитель для заненсения пары чисел в очередь в формате
     * first*n+second
     */
    void calculateStartPointsForBFS(Queue<Integer> queue, int carWorldX, int carWorldY, int n) {
        Vector speed = new Vector(curSpeed);
        if (speed.module() == 0) {
            speed.getVectorByAngle(self.getAngle());
        }
        speed.normalize();
        speed.mult(carHeight * worldTileSize / tileSize);
        int x, y, cur;

        x = carWorldX + (int) (speed.x);
        y = carWorldY + (int) (speed.y);
        while (getWorldMap(x, y) != empty && getWorldMap(x, y) != selfCar) {
            speed.mult(0.9);
            x = carWorldX + (int) (speed.x);
            y = carWorldY + (int) (speed.y);
        }
        setWorldMap(x, y, 1);
        cur = x * n + y;
        queue.add(cur);
        /*Vector v = new Vector(speed);
         double window = PI / 6;
         v.rotateVector(-window);
         final double delta = PI / 180;
         for (int i = (int) (2 * window / delta); i >= 0; i--) {
         x = carWorldX + (int) (v.x);
         y = carWorldY + (int) (v.y);
         if (getWorldMap(x, y) == empty) {
         setWorldMap(x, y, 1);
         cur = x * n + y;
         queue.add(cur);
         }
         v.rotateVector(delta);
         }

         v.SetEqual(speed);
         window = PI / 90;
         for (int i = (int) (2 * window / delta); i >= 0; i--) {
         x = carWorldX + (int) (v.x);
         y = carWorldY + (int) (v.y);
         if (getWorldMap(x, y) == empty) {
         setWorldMap(x, y, 1);
         cur = x * n + y;
         queue.add(cur);
         }
         v.rotateVector(delta);
         }*/
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
    private int getDistanceToWall(Car car, double deltaAngle, Vector directionVector) {
        int carColor = getColorOfCar(car);
        int carX = (int) getRelativeCoordinate(car.getX());
        int carY = (int) getRelativeCoordinate(car.getY());
        if (directionVector.length() < 1e-1) {
            return tileSize;
        }
        directionVector.normalize();
        while (max(abs(directionVector.x), abs(directionVector.y)) <= 1) {
            directionVector.x *= 1.001;
            directionVector.y *= 1.001;
        }
        final int numberOfTurns = 40;
        double turnAngle;

        for (int d = 1; d <= tileSize; d++) {
            Vector vector = new Vector(directionVector);
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

                if (getCurTile(x, y) == wall) {
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
     * находим расстояние от центра собственной машины до ближайшей стены за
     * направление поиска берем вектор СКОРОСТИ авто
     *
     * @param deltaAngle угол отклонения от вектора скорости
     * @return расстояние до стены
     */
    private int getDistanceToWallBySpeed(double deltaAngle) {
        Vector direvtionVector = new Vector(curSpeed);
        if (direvtionVector.module() == 0) {
            direvtionVector.getVectorByAngle(self.getAngle());
        }
        return getDistanceToWall(self, deltaAngle, direvtionVector);
    }

    /**
     * находим расстояние от центра собственной машины до ближайшей стены за
     * направление поиска берем вектор НАПРАВЛЕНИЯ авто
     *
     * @param deltaAngle угол отклонения от вектора скорости
     * @return расстояние до стены
     */
    private int getDistanceToWallByCarDirection(double deltaAngle) {
        Vector direvtionVector = new Vector(self.getAngle());
        return getDistanceToWall(self, deltaAngle, direvtionVector);
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
     * получает схематическое изображение текущего тайла с учетом собстевенной
     * машины, планируется добавить машины соперников
     */
    private void calculateCurTile() {
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
