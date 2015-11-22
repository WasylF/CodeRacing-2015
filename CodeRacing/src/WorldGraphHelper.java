
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
    /**
     * граф карты трасы
     */
    private final int[][] worldGraph;
    /**
     * количество вершин в графе
     */
    private final int graphSize;

    public WorldGraphHelper(StrategyWslF strategy, int worldWidth, int worldHeight, int worldHW) {
        this.strategy = strategy;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.worldHW = worldHW;
        this.graphSize = worldHW * worldHW + 10;

        this.worldGraph = buildWorldGraph();
    }

    /**
     *
     * @return копию графа карты трасы
     */
    public int[][] getCopyWorldGraph() {
        int[][] g = new int[graphSize][];

        for (int i = 0; i < graphSize; i++) {
            g[i] = new int[worldGraph[i].length];
            System.arraycopy(worldGraph[i], 0, g[i], 0, worldGraph[i].length);
        }

        return g;
    }

    /**
     * построение графа карты номер вершини : v= x*worldHW+y
     *
     * @return граф
     */
    private int[][] buildWorldGraph() {
        int[][] g = new int[graphSize][];
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
                for (int i = list.size() - 1; i >= 0; i--) {
                    g[cur][i] = list.poll();
                }
            }
        }

        for (int i = 0; i < graphSize; i++) {
            if (g[i] == null) {
                g[i] = new int[0];
            }
        }
        return g;
    }

    private void addLeft(int x, int y, List<Integer> list) {
        if (x - 1 >= 0) {
            list.add((x - 1) * worldHW + y);
        }
    }

    private void addRight(int x, int y, List<Integer> list) {
        if (x + 1 < worldWidth) {
            list.add((x + 1) * worldHW + y);
        }
    }

    private void addUp(int x, int y, List<Integer> list) {
        if (y >= 1) {
            list.add(x * worldHW + y - 1);
        }
    }

    private void addDown(int x, int y, List<Integer> list) {
        if (y + 1 < worldHeight) {
            list.add(x * worldHW + y + 1);
        }
    }

}
