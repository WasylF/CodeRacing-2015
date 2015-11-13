
import static java.lang.StrictMath.*;

/**
 *
 * @author Wasyl
 */
public class Vector extends Point {
    
    public Vector() {
        x = 0;
        y = 0;
    }
    /**
     * создает вектор с заданным углом с осью абсцисс
     * @param angle 
     */
    public Vector(double angle) {
        x = 0;
        y = 0;
        getVectorByAngle(angle);
    }
    
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public Vector(Point p) {
        this.x = p.x;
        this.y = p.y;
    }
    
    @Override
    public String toString() {
        double v = hypot(x, y);
        return " ( " + x + " , " + y + " ) : |" + v + "|";
    }
    
    public double length() {
        return hypot(x, y);
    }
    
    public boolean equals(Vector v2) {
        if (abs(length()) < EPS && abs(v2.length()) < EPS) {
            return true;
        }
        
        if (abs(length() * v2.length()) < EPS) {
            return false;
        }
        
        return abs(x * v2.y - y * v2.x) < EPS;
    }

    /**
     * вычисление положительного угла между двумя векторами
     *
     * @param v второй вектор
     * @return угол в радианах, если вычислить невозможно, то -1
     */
    public double getPositiveAngle(Vector v) {
        double t = hypot(x, y) * hypot(v.x, v.y);
        if (t == 0) {
            return -1;
        }
        return t / (x * v.x + y * v.y);
    }
    
    public double getAngleToOX() {
        return (Math.atan2(x, -y) - Math.atan2(1, 0));
    }
    
    public void printVector() {
        System.out.println(toString());
    }

    /**
     * векторное произведение
     *
     * @param p2
     * @return
     */
    public double multiplyVectors(Vector v2) {
        return x * v2.y - y * v2.x;
    }

    /**
     * makes length of vector equals to 1
     */
    public void normalize() {
        double length = Math.hypot(x, y);
        if (length > EPS) {
            x /= length;
            y /= length;
        }
    }
    
    public int sgnMultiplyVectors(Vector p2) {
        double t = multiplyVectors(p2);
        if (Math.abs(t) < 1e-6) {
            return 0;
        }
        if (t > 0) {
            return 1;
        }
        return -1;
    }
    
    public void rotateVector(double phi) {
        double xNew = x * cos(phi) - y * sin(phi);
        double yNew = x * sin(phi) + y * cos(phi);
        x = xNew;
        y = yNew;
    }

    /**
     *
     * @param angle angle between vector and Ox
     */
    public void getVectorByAngle(double angle) {
        if (abs(angle - PI / 2) < 1e-3) {
            x = 0;
            y = 1;
            return;
        }
        if (abs(angle + PI / 2) < 1e-3) {
            x = 0;
            y = -1;
            return;
        }
        x = 1;
        y = tan(angle);
    }
    
}
