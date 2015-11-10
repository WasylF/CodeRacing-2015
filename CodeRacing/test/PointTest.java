/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Wasyl
 */
public class PointTest {

    public PointTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of toString method, of class Point.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Point instance = new Point();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class Point.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Point p2 = null;
        Point instance = new Point();
        boolean expResult = false;
        boolean result = instance.equals(p2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isUnderLine method, of class Point.
     */
    @Test
    public void testIsUnderLine() {
        System.out.println("isUnderLine");
        double a = 0.0;
        double b = 0.0;
        double c = 0.0;
        Point instance = new Point();
        boolean expResult = false;
        boolean result = instance.isUnderLine(a, b, c);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAngleToOX method, of class Point.
     */
    @Test
    public void testGetAngleToOX() {
        System.out.println("getAngleToOX");
        Point instance = new Point(1, 0);
        double expResult = 0.0;
        double result = instance.getAngleToOX();
        assertEquals(expResult, result, 1e-3);
    }

    @Test
    public void testGetAngleToOX2() {
        System.out.println("getAngleToOX");
        Point instance = new Point(0, 1);
        double expResult = Math.PI / 2;
        double result = instance.getAngleToOX();
        assertEquals(expResult, result, 1e-3);
    }

    @Test
    public void testGetAngleToOX3() {
        System.out.println("getAngleToOX");
        Point instance = new Point(0, -1);
        double expResult = -Math.PI / 2;
        double result = instance.getAngleToOX();
        assertEquals(expResult, result, 1e-3);
    }

    @Test
    public void testGetAngleToOX4() {
        System.out.println("getAngleToOX");
        Point instance = new Point(-1, 0);
        double expResult = -Math.PI;
        double result = instance.getAngleToOX();
        assertEquals(expResult, result, 1e-3);
    }

    @Test
    public void testGetAngleToOX5() {
        System.out.println("getAngleToOX");
        Point instance = new Point(1, 1);
        double expResult = Math.PI / 4;
        double result = instance.getAngleToOX();
        assertEquals(expResult, result, 1e-3);
    }

    /**
     * Test of printVector method, of class Point.
     */
    @Test
    public void testPrintVector() {
        System.out.println("printVector");
        Point instance = new Point();
        instance.printVector();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of multiplyVectors method, of class Point.
     */
    @Test
    public void testMultiplyVectors() {
        System.out.println("multiplyVectors");
        Point p2 = null;
        Point instance = new Point();
        double expResult = 0.0;
        double result = instance.multiplyVectors(p2);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sgnMultiplyVectors method, of class Point.
     */
    @Test
    public void testSgnMultiplyVectors() {
        System.out.println("sgnMultiplyVectors");
        Point p2 = null;
        Point instance = new Point();
        int expResult = 0;
        int result = instance.sgnMultiplyVectors(p2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of checkPointInPolygon method, of class Point.
     */
    @Test
    public void testCheckPointInPolygon() {
        System.out.println("checkPointInPolygon");
        Point[] polygon = null;
        Point instance = new Point();
        boolean expResult = false;
        boolean result = instance.checkPointInPolygon(polygon);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSymmetric method, of class Point.
     */
    @Test
    public void testGetSymmetric() {
        System.out.println("getSymmetric");
        Point middle = null;
        Point instance = new Point();
        Point expResult = null;
        Point result = instance.getSymmetric(middle);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
