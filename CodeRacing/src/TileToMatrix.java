
import model.*;

/**
 *
 * @author Wsl_F
 */
public class TileToMatrix {

    private World world;
    private Game game;
    private Move move;
    private Car car;

    /**
     * размер тайла конвертированный в инт
     */
    private int tileSize;

    /**
     * размер закругления/отступа конвертированный в инт
     */
    private int tileMargin;

    private int[][] ans;
    private final int empty = StrategyWslF.empty;
    private final int wall = StrategyWslF.wall;

    public TileToMatrix(World world, Game game, Move move, Car car) {
        init(world, game, move, car, (int) (game.getTrackTileSize() + 0.1), (int) (game.getTrackTileMargin() + 0.1));
    }

    public TileToMatrix(World world, Game game, Move move, Car car, int tileSize, int tileMargin) {
        init(world, game, move, car, tileSize, tileMargin);
    }

    private void init(World world, Game game, Move move, Car car, int tileSize, int tileMargin) {
        this.world = world;
        this.game = game;
        this.move = move;
        this.car = car;
        this.tileSize = tileSize;
        this.tileMargin = tileMargin;;
        ans = new int[tileSize][tileSize];
    }

    /**
     * возвращает квадратную матрицу 800х800, где 1 значит, что в клетку нельзя
     * заехать (стена или за стеной) 0 - можно (свободная)
     *
     * @param tileName имя тайла
     * @return матрица 800х800
     */
    public int[][] getMatrix(TileType tileName) {

        switch (tileName) {
            case EMPTY:
                getEmpty();
                break;
            case VERTICAL:
                getVertical();
                break;
            case HORIZONTAL:
                getVertical();
                rotate90();
                break;
            case LEFT_TOP_CORNER:
                getLeftTopCorner();
                break;
            case LEFT_BOTTOM_CORNER:
                getLeftBottomCorner();
                break;
            case RIGHT_TOP_CORNER:
                getRightTopCorner();
                break;
            case RIGHT_BOTTOM_CORNER:
                getRightBottomCorner();
                break;
            case LEFT_HEADED_T:
                getLeftHeadedT();
                break;
            case RIGHT_HEADED_T:
                getRightHeadedT();
                break;
            case TOP_HEADED_T:
                getTopHeadedT();
                break;
            case BOTTOM_HEADED_T:
                getBottomHeadedT();
                break;
            case CROSSROADS:
                getCrossRoads();
                break;
        }
        return ans;
    }

    private void getEmpty() {
        for (int i = 0; i < tileSize; i++) {
            for (int j = 0; j < tileSize; j++) {
                ans[i][j] = wall;
            }
        }
    }

    private void getVertical() {
        for (int i = 0; i < tileSize; i++) {
            for (int j = 0; j < tileSize; j++) {
                if (j <= tileMargin || tileSize - j <= tileMargin) {
                    ans[i][j] = wall;
                } else {
                    ans[i][j] = empty;
                }
            }
        }
    }

    private void rotate90() {
        /*
         1234
         5678
         9012
         3456
         */
        for (int i = 0; i < tileSize; i++) {
            for (int j = i; j + i < tileSize; j++) {
                int t1 = ans[i][j];
                int t2 = ans[i][tileSize - 1 - j];
                int t3 = ans[tileSize - 1 - i][tileSize - 1 - j];
                int t4 = ans[tileSize - 1 - i][j];

                ans[i][j] = t4;
                ans[i][tileSize - 1 - j] = t1;
                ans[tileSize - 1 - i][tileSize - 1 - j] = t2;
                ans[tileSize - 1 - i][j] = t3;
            }
        }
    }

    private void rotate180() {
        rotate90();
        rotate90();
    }

    private void rotate270() {
        rotate90();
        rotate90();
        rotate90();
    }

    private void getLeftTopCorner() {
        // отрезаем верхние строчки
        for (int i = 0; i < tileSize; i++) {
            for (int j = 0; j <= tileMargin; j++) {
                ans[i][j] = wall;
            }
        }
        // отрезаем левые столбцы
        for (int i = 0; i <= tileMargin; i++) {
            for (int j = 0; j < tileSize; j++) {
                ans[i][j] = wall;
            }
        }

        // остальное заполняем нулями
        for (int i = tileMargin + 1; i < tileSize; i++) {
            for (int j = tileMargin + 1; j < tileSize; j++) {
                ans[i][j] = empty;
            }
        }

        // отрезаем левый верхний угол
        cutCorner(tileMargin, tileMargin, false);

        // отрезаем правый нижний угол
        cutCorner(tileSize - 1, tileSize - 1, true);
    }

    /**
     * заполняем уголок (треугольный) стенами, радиус равен tileMargin правей и
     * ниже || левей и выше
     *
     * @param x абсцисса центра угла
     * @param y ордината центра угла
     * @param invert 1 - заполняем сверху от центра, 0 - снизу
     */
    private void cutCorner(int x, int y, boolean invert) {
        int t = invert ? -1 : 1;
        for (int i = 0; i <= tileMargin; i++) {
            for (int j = 0; i + j <= tileMargin; j++) {
                ans[x + t * i][y + t * j] = wall;
            }
        }
    }

    /**
     * заполняем уголок (треугольный) стенами, радиус равен tileMargin правей и
     * выше || левей и ниже
     *
     * @param x абсцисса центра угла
     * @param y ордината центра угла
     * @param invert 1 - заполняем сверху от центра, 0 - снизу
     */
    private void cutCorner2(int x, int y, boolean invert) {
        int t = invert ? -1 : 1;
        for (int i = 0; i <= tileMargin; i++) {
            for (int j = 0; i + j <= tileMargin; j++) {
                ans[x - t * i][y + t * j] = 1;
            }
        }
    }

    private void getLeftBottomCorner() {
        getLeftTopCorner();
        rotate270();
    }

    private void getRightTopCorner() {
        getLeftBottomCorner();
        rotate90();
    }

    private void getRightBottomCorner() {
        getLeftTopCorner();
        rotate180();
    }

    private void getLeftHeadedT() {
        for (int i = 0; i < tileSize; i++) {

            for (int j = tileSize - tileMargin - 1; j >= 0; j--) {
                ans[i][j] = empty;
            }

            // отрезаем правые столбцы
            for (int j = tileSize - tileMargin; j < tileSize; j++) {
                ans[i][j] = wall;
            }
        }

        //отрезаем левый верхний угол
        cutCorner(0, 0, false);

        //отрезаем левый нижний угол
        cutCorner2(tileSize - 1, 0, false);
    }

    private void getRightHeadedT() {
        getLeftHeadedT();
        rotate180();
    }

    private void getTopHeadedT() {
        getLeftHeadedT();
        rotate90();
    }

    private void getBottomHeadedT() {
        getLeftHeadedT();
        rotate270();
    }

    private void getCrossRoads() {
        for (int i = 0; i < tileSize; i++) {
            for (int j = 0; j < tileSize; j++) {
                ans[i][j] = empty;
            }
        }

        //отрезаем левый верхний угол
        cutCorner(0, 0, false);

        //отрезаем левый нижний угол
        cutCorner2(tileSize - 1, 0, false);

        //отрезаем правый верхний угол
        cutCorner2(0, tileSize - 1, true);

        //отрезаем правый нижний угол
        cutCorner(tileSize - 1, tileSize - 1, true);
    }

}
