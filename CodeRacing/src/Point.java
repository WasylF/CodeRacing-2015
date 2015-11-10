
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
    private static final double EPS = 0.5;

    public Point() {
        x = 0;
        y = 0;
    }

    public Point(double x0, double y0) {
        x = x0;
        y = y0;
    }

    @Override
    public String toString() {
        return " (" + x + "," + y + ") ";
    }

    public boolean equals(Point p2) {
        return (Math.abs(p2.x - x) + Math.abs(p2.y - y) < EPS);
    }

    /**
     * line: ax+by+c= 0
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    public boolean isUnderLine(double a, double b, double c) {
        double yLine = (-c - a) / b;
        return y < yLine;
    }

    public void printVector() {
        double v = Math.hypot(x, y);
        if (y != 0) {
            x /= y;
            y /= y;
        }
        System.out.println(" (" + x + "," + y + ")  |xy|: " + v);
    }

    public double multiplyVectors(Point p2) {
        return x * p2.y - y * p2.x;
    }

    public int sgnMultiplyVectors(Point p2) {
        double t = multiplyVectors(p2);
        if (Math.abs(t) < 1e-6) {
            return 0;
        }
        if (t > 0) {
            return 1;
        }
        return -1;
    }

    public boolean checkPointInPolygon(Point[] polygon) {
        int plus;
        plus = 0;
        for (int i = 0; i + 1 < polygon.length; i++) {
            Point v = new Point(polygon[i + 1].x - polygon[i].x, polygon[i + 1].y - polygon[i].y);
            Point vP = new Point(x - polygon[i].x, y - polygon[i].y);
            switch (v.sgnMultiplyVectors(vP)) {
                case 0:
                    return true;
                case 1:
                    plus++;
            }
        }
        
        Point v = new Point(polygon[0].x - polygon[polygon.length - 1].x, polygon[0].y - polygon[polygon.length - 1].y);
        Point vP = new Point(x - polygon[polygon.length - 1].x, y - polygon[polygon.length - 1].y);
        switch (v.sgnMultiplyVectors(vP)) {
            case 0:
                return true;
            case 1:
                plus++;
        }

        return (plus == 0 || plus == polygon.length);
    }

}
