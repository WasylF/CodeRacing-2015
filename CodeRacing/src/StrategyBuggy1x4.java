
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
    private PairIntInt nextWayPoint;

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

        Vector toNextWayPoint = new Vector(nextWayPoint.first - self.getX(), nextWayPoint.second - self.getY());
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

    private PairIntInt getNextWayPoint() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
