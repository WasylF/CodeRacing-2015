
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
     *
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return equals((Vector) obj);
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
        return abs(getAngle(v));
    }

    /**
     * ориентированный угол между векторами
     *
     * @param v
     * @return
     */
    public double getAngle(Vector v) {
        return atan2(x * v.y - v.x * y, x * v.x + y * v.y);
    }

    public double getAngleToOX() {
        return getAngle(new Vector(1, 0));
    }

    public void printVector() {
        System.out.println(toString());
    }

    /**
     * векторное произведение
     *
     * @param v2
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

    /**
     * signum of vectors mult
     *
     * @param p2 second vector
     * @return signum
     */
    public int sgnMultiplyVectors(Vector p2) {
        double t = multiplyVectors(p2);
        if (Math.abs(t) < EPS) {
            return 0;
        }
        if (t > 0) {
            return 1;
        }
        return -1;
    }

    /**
     * rotate this vector by clockwise on angle phi
     * @param phi angle
     */
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
        if (abs(angle) < PI / 2) {
            x = 1;
            y = tan(angle);
        } else {
            x = -1;
            y = -tan(angle);
        }
    }

    /**
     * целая часть длинны
     *
     * @return целую часть от длинны вектора
     */
    public int module() {
        return (int) length();
    }

    /**
     * устанавливает вектор эквивалентным другой точке/вектору
     *
     * @param p
     */
    public void SetEqual(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    /**
     * умножает вектор на скаляр
     *
     * @param k скаляр
     */
    public void mult(double k) {
        x *= k;
        y *= k;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

}
