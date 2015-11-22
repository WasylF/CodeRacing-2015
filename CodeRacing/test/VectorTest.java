
import junit.framework.TestCase;

/**
 *
 * @author Wsl_F
 */
public class VectorTest extends TestCase {
    
    public static final double EPS = 1e-3;

    public VectorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of length method, of class Vector.
     */
    public void testLength() {
        System.out.println("length");
        Vector instance = new Vector(1, 0);
        double expResult = 1;
        double result = instance.length();
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of equals method, of class Vector.
     */
    public void testEquals() {
        System.out.println("equals");
        Vector v2 = new Vector(2, 2);
        Vector instance = new Vector(1, 1);
        boolean expResult = true;
        boolean result = instance.equals(v2);
        assertEquals(expResult, result);
    }

    /**
     * Test of getPositiveAngle method, of class Vector.
     */
    public void testGetPositiveAngle() {
        System.out.println("getPositiveAngle");
        Vector v = new Vector(1, 0);
        Vector instance = new Vector(0, 1);
        double expResult = Math.PI / 2;
        double result = instance.getPositiveAngle(v);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getPositiveAngle method, of class Vector.
     */
    public void testGetPositiveAngle2() {
        System.out.println("getPositiveAngle");
        Vector v = new Vector(0, 1);
        Vector instance = new Vector(1, 0);
        double expResult = Math.PI / 2;
        double result = instance.getPositiveAngle(v);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getPositiveAngle method, of class Vector.
     */
    public void testGetPositiveAngle3() {
        System.out.println("getPositiveAngle");
        Vector instance = new Vector(0, 1);
        Vector v = new Vector(1, 1000);
        double expResult = Math.atan(1 / 1000);
        double result = instance.getPositiveAngle(v);
        assertEquals(expResult, result, EPS);

        result = v.getPositiveAngle(instance);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle0() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 0);
        Vector v = new Vector(0, 1);
        double expResult = Math.PI / 2;
        double result = instance.getAngle(v);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle1() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 0);
        Vector v = new Vector(0, -1);
        double expResult = -Math.PI / 2;
        double result = instance.getAngle(v);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle2() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 0);
        Vector v = new Vector(1, 0);
        double expResult = 0;
        double result = instance.getAngle(v);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle3() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 0);
        Vector v = new Vector(-1, 0.0001);
        double expResult = Math.PI;
        double result = instance.getAngle(v);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle4() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 0);
        Vector v = new Vector(-1, -0.0001);
        double expResult = -Math.PI;
        double result = instance.getAngle(v);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle5() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 1);
        Vector v = new Vector(0, 1);
        double expResult = Math.PI / 4;
        double result = instance.getAngle(v);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle6() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 1);
        Vector v = new Vector(1, 0);
        double expResult = -Math.PI / 4;
        double result = instance.getAngle(v);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle7() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 1);
        Vector v = new Vector(-1, 1);
        double expResult = Math.PI / 2;
        double result = instance.getAngle(v);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle10() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 0);
        Vector v = new Vector(0, 1);
        double expResult = -Math.PI / 2;
        double result = v.getAngle(instance);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle11() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 0);
        Vector v = new Vector(0, -1);
        double expResult = Math.PI / 2;
        double result = v.getAngle(instance);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle12() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 0);
        Vector v = new Vector(1, 0);
        double expResult = 0;
        double result = v.getAngle(instance);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle13() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 0);
        Vector v = new Vector(-1, 0.0001);
        double expResult = -Math.PI;
        double result = v.getAngle(instance);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle14() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 0);
        Vector v = new Vector(-1, -0.0001);
        double expResult = +Math.PI;
        double result = v.getAngle(instance);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle15() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 1);
        Vector v = new Vector(0, 1);
        double expResult = -Math.PI / 4;
        double result = v.getAngle(instance);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle16() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 1);
        Vector v = new Vector(1, 0);
        double expResult = +Math.PI / 4;
        double result = v.getAngle(instance);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle17() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 1);
        Vector v = new Vector(-1, 1);
        double expResult = -Math.PI / 2;
        double result = v.getAngle(instance);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle50() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 0);
        for (double x = 1; x < 30; x++) {
            for (double y = 1; y < 30; y++) {
                Vector v = new Vector(x, y);
                double expResult = Math.atan(y / x);
                double result = instance.getAngle(v);
                assertEquals(expResult, result, EPS);
            }
        }
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle51() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 0);
        for (double x = 1; x < 30; x++) {
            for (double y = 1; y < 30; y++) {
                Vector v = new Vector(x, -y);
                double expResult = -Math.atan(y / x);
                double result = instance.getAngle(v);
                assertEquals(expResult, result, EPS);
            }
        }
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle52() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 0);
        for (double x = 1; x < 30; x++) {
            for (double y = 1; y < 30; y++) {
                Vector v = new Vector(-x, -y);
                double expResult = Math.atan(y / x) - Math.PI;
                double result = instance.getAngle(v);
                assertEquals(expResult, result, EPS);
            }
        }
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle53() {
        System.out.println("getAngle");
        Vector instance = new Vector(1, 0);
        for (double x = 1; x < 30; x++) {
            for (double y = 1; y < 30; y++) {
                Vector v = new Vector(-x, y);
                double expResult = Math.PI - Math.atan(y / x);
                double result = instance.getAngle(v);
                assertEquals(expResult, result, EPS);
            }
        }
    }

    /**
     * Test of getAngle method, of class Vector.
     */
    public void testGetAngle60() {
        System.out.println("getAngle");
        Vector instance = new Vector(0, 1);
        for (double x = 1; x < 30; x++) {
            for (double y = 1; y < 30; y++) {
                Vector v = new Vector(x, y);
                double expResult = -Math.atan(x / y);
                double result = instance.getAngle(v);
                assertEquals(expResult, result, EPS);
            }
        }
    }

    /**
     * Test of getAngleToOX method, of class Vector.
     */
    public void testGetAngleToOX() {
        System.out.println("getAngleToOX");
        Vector instance = new Vector(1, 1);
        double expResult = -Math.PI / 4;
        double result = instance.getAngleToOX();
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of multiplyVectors method, of class Vector.
     */
    public void testMultiplyVectors() {
        System.out.println("multiplyVectors");
        Vector v2 = new Vector();
        Vector instance = new Vector();
        double expResult = 0.0;
        double result = instance.multiplyVectors(v2);
        assertEquals(expResult, result, EPS);
    }

    /**
     * Test of sgnMultiplyVectors method, of class Vector.
     */
    public void testSgnMultiplyVectors() {
        System.out.println("sgnMultiplyVectors");
        Vector p2 = new Vector(0.5, 0.5);
        Vector instance = new Vector(1, 0);
        int expResult = 1;
        int result = instance.sgnMultiplyVectors(p2);
        assertEquals(expResult, result);
    }

    /**
     * Test of rotateVector method, of class Vector.
     */
    public void testRotateVector() {
        System.out.println("rotateVector");
        double phi = 0.0;
        Vector instance = new Vector(1, 1);
        instance.rotateVector(phi);
        assertEquals(new Vector(1, 1), instance);
    }

    /**
     * Test of getVectorByAngle method, of class Vector.
     */
    public void testGetVectorByAngle() {
        System.out.println("getVectorByAngle");
        double angle = Math.PI / 4;
        Vector instance = new Vector();
        instance.getVectorByAngle(angle);
        assertEquals(new Vector(1, 1), instance);
    }

    /**
     * Test of module method, of class Vector.
     */
    public void testModule() {
        System.out.println("module");
        Vector instance = new Vector(3, 4);
        int expResult = 5;
        int result = instance.module();
        assertEquals(expResult, result);
    }

    /**
     * Test of SetEqual method, of class Vector.
     */
    public void testSetEqual() {
        System.out.println("SetEqual");
        Point p = new Point(123, 234);
        Vector instance = new Vector();
        instance.SetEqual(p);
        assertEquals(new Vector(p), instance);
    }

    /**
     * Test of mult method, of class Vector.
     */
    public void testMult() {
        System.out.println("mult");
        double k = 2;
        Vector instance = new Vector(1, 1);
        instance.mult(k);
        assertEquals(instance.x, k);
        assertEquals(instance.x, k);
    }
    
}
