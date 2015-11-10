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
    private static final double EPS = 0.8;

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
        double v= Math.hypot(x, y);
        if (y!=0) {
            x/= y; y/= y;
        }
        System.out.println(" ("+x+","+y+")  |xy|: "+v);
    }
}
