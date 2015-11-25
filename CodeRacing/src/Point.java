
import static java.lang.StrictMath.*;

/**
 *
 * @author WslF
 */
/**
 * класс точка
 */
public class Point extends Object {

    public double x;
    public double y;
    protected static final double EPS = 1e-3;

    public Point() {
        x = 0;
        y = 0;
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point p) {
        x = p.x;
        y = p.y;
    }

    public Point(PairIntInt p) {
        x = p.first;
        y = p.second;
    }

    @Override
    public String toString() {
        return " (" + x + "," + y + ") ";
    }

    public boolean equals(Point p2) {
        return (Math.abs(p2.x - x) + Math.abs(p2.y - y) < EPS);
    }

    /**
     * check does point belong to polygon
     *
     * @param polygon
     * @return true if belomgs
     */
    public boolean checkPointInPolygon(Point[] polygon) {
        int plus;
        plus = 0;
        for (int i = 0; i + 1 < polygon.length; i++) {
            Vector v = new Vector(polygon[i + 1].x - polygon[i].x, polygon[i + 1].y - polygon[i].y);
            Vector vP = new Vector(x - polygon[i].x, y - polygon[i].y);
            switch (v.sgnMultiplyVectors(vP)) {
                case 0:
                    return true;
                case 1:
                    plus++;
            }
        }

        Vector v = new Vector(polygon[0].x - polygon[polygon.length - 1].x, polygon[0].y - polygon[polygon.length - 1].y);
        Vector vP = new Vector(x - polygon[polygon.length - 1].x, y - polygon[polygon.length - 1].y);
        switch (v.sgnMultiplyVectors(vP)) {
            case 0:
                return true;
            case 1:
                plus++;
        }

        return (plus == 0 || plus == polygon.length);
    }

    /**
     * AX - segment, M belongs AX && AM=MX A - this point, M - middle
     *
     * @param middle
     * @return X
     */
    public Point getSymmetric(Point middle) {
        return new Point(2 * middle.x - x, 2 * middle.y - y);
    }

}
