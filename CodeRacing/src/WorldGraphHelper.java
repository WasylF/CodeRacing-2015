
import java.util.LinkedList;
import java.util.List;
import model.TileType;

/**
 *
 * @author Wsl_F
 */
public class WorldGraphHelper {

    private final StrategyWslF strategy;
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

    public WorldGraphHelper(StrategyWslF strategy, int worldWidth, int worldHeight, int worldHW) {
        this.strategy = strategy;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.worldHW = worldHW;
    }

    /**
     * построение графа карты номер вершини : v= x*worldHW+y
     *
     * @return граф
     */
    protected int[][] buildWorldGraph() {
        int[][] g = new int[worldHeight * worldWidth][];
        for (int x = 0; x < worldWidth; x++) {
            for (int y = 0; y < worldHeight; y++) {
                int cur = x * worldHW + y;
                LinkedList<Integer> list = new LinkedList<>();
                switch (strategy.mapTiles[x][y]) {
                    case VERTICAL:
                        addUp(x, y, list);
                        addDown(x, y, list);
                        break;
                    case HORIZONTAL:
                        addLeft(x, y, list);
                        addRight(x, y, list);
                        break;
                    case LEFT_TOP_CORNER:
                        addRight(x, y, list);
                        addDown(x, y, list);
                        break;
                    case LEFT_BOTTOM_CORNER:
                        addRight(x, y, list);
                        addUp(x, y, list);
                        break;
                    case RIGHT_TOP_CORNER:
                        addLeft(x, y, list);
                        addDown(x, y, list);
                        break;
                    case RIGHT_BOTTOM_CORNER:
                        addLeft(x, y, list);
                        addUp(x, y, list);
                        break;
                    case LEFT_HEADED_T:
                        addUp(x, y, list);
                        addLeft(x, y, list);
                        addDown(x, y, list);
                        break;
                    case RIGHT_HEADED_T:
                        addDown(x, y, list);
                        addUp(x, y, list);
                        addRight(x, y, list);
                        break;
                    case TOP_HEADED_T:
                        addLeft(x, y, list);
                        addUp(x, y, list);
                        addRight(x, y, list);
                        break;
                    case BOTTOM_HEADED_T:
                        addDown(x, y, list);
                        addLeft(x, y, list);
                        addRight(x, y, list);
                        break;
                    case CROSSROADS:
                        addDown(x, y, list);
                        addLeft(x, y, list);
                        addUp(x, y, list);
                        addRight(x, y, list);
                        break;
                    default:
                        continue;
                }
                g[cur] = new int[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    g[cur][i] = list.poll();
                }
            }
        }

        return g;
    }

    protected void addLeft(int x, int y, List<Integer> list) {
        if (x - 1 >= 0) {
            list.add((x - 1) * worldHW + y);
        }
    }

    protected void addRight(int x, int y, List<Integer> list) {
        if (x + 1 < worldWidth) {
            list.add((x + 1) * worldHW + y);
        }
    }

    protected void addUp(int x, int y, List<Integer> list) {
        if (y >= 1) {
            list.add(x * worldHW + y - 1);
        }
    }

    protected void addDown(int x, int y, List<Integer> list) {
        if (y + 1 < worldHeight) {
            list.add(x * worldHW + y + 1);
        }
    }

}
