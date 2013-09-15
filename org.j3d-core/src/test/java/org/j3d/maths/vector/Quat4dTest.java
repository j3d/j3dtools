/*
 * j3d.org Copyright (c) 2001-2013
 *                                 Java Source
 *
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read docs/LGPL.txt for more information
 *
 *  This software comes with the standard NO WARRANTY disclaimer for any
 *  purpose. Use it at your own risk. If there's a problem you get to fix it.
 */

package org.j3d.maths.vector;

import java.util.Date;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * Unit tests for the Quat4d class
 *
 * @author justin
 */
public class Quat4dTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        Quat4d classUnderTest = new Quat4d();

        assertEquals(classUnderTest.x, 0.0, "Non-zero default x coordinate");
        assertEquals(classUnderTest.y, 0.0, "Non-zero default y coordinate");
        assertEquals(classUnderTest.z, 0.0, "Non-zero default z coordinate");
        assertEquals(classUnderTest.angle, 0.0, "Non-zero default angle coordinate");
    }

    @Test(groups = "unit")
    public void testSet() throws Exception
    {
        final double TEST_X = 0.4;
        final double TEST_Y = -1.0;
        final double TEST_Z = 4.0;
        final double TEST_ANGLE = -0.6;

        Quat4d classUnderTest = new Quat4d();
        classUnderTest.set(TEST_X, TEST_Y, TEST_Z, TEST_ANGLE);

        assertEquals(classUnderTest.x, TEST_X, 0.001, "X Coordinate incorrectly set");
        assertEquals(classUnderTest.y, TEST_Y, 0.001, "Y Coordinate incorrectly set");
        assertEquals(classUnderTest.z, TEST_Z, 0.001, "Z Coordinate incorrectly set");
        assertEquals(classUnderTest.angle, TEST_ANGLE, 0.001, "Angle amount incorrectly set");
    }

    @Test(groups = "unit")
    public void testNormaliseZeroLength() throws Exception
    {
        Quat4d classUnderTest = new Quat4d();
        classUnderTest.normalise();

        assertEquals(classUnderTest.x, 0.0, "X Coordinate is not zero");
        assertEquals(classUnderTest.y, 0.0, "Y Coordinate is not zero");
        assertEquals(classUnderTest.z, 0.0, "Z Coordinate is not zero");
    }

    @Test(groups = "unit")
    public void testNormalise() throws Exception
    {
        Quat4d classUnderTest = new Quat4d();
        classUnderTest.set(10, -0.5, 0.1, 0.0);
        classUnderTest.normalise();

        double l2 = classUnderTest.x * classUnderTest.x +
            classUnderTest.y * classUnderTest.y +
            classUnderTest.z * classUnderTest.z;

        assertEquals(l2, 1.0, "Normalised length is not 1");
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testInterpolateQ1Null() throws Exception
    {
        Quat4d classUnderTest = new Quat4d();
        classUnderTest.interpolate(null, new Quat4d(), 0);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testInterpolateQ2Null() throws Exception
    {
        Quat4d classUnderTest = new Quat4d();
        classUnderTest.interpolate(new Quat4d(), null, 0);
    }

    @Test(groups = "unit")
    public void testInterpolate() throws Exception
    {
        Quat4d testQuat1 = new Quat4d();
        testQuat1.x = 1;

        Quat4d testQuat2 = new Quat4d();
        testQuat2.y = 1;

        Quat4d classUnderTest = new Quat4d();

        classUnderTest.interpolate(testQuat1, testQuat2, 0);

        assertEquals(classUnderTest, testQuat1, "Zero alpha angle didn't give initial position");

        classUnderTest.interpolate(testQuat1, testQuat2, 1);

        assertEquals(classUnderTest, testQuat2, "1.0 alpha angle didn't give initial position");

        Quat4d expectedResult = new Quat4d();
        expectedResult.x = 1 / Math.sqrt(2);
        expectedResult.y = 1 / Math.sqrt(2);

        classUnderTest.interpolate(testQuat1, testQuat2, 0.5);

        assertEquals(classUnderTest, expectedResult, "Incorrect halfway interpolation");
    }

    @Test(groups = "unit")
    public void testNegate() throws Exception
    {
        final double TEST_X = 0.4;
        final double TEST_Y = -0.4;
        final double TEST_Z = 13.4;
        final double TEST_ANGLE = 0.25;

        Quat4d classUnderTest = new Quat4d();
        classUnderTest.x = TEST_X;
        classUnderTest.y = TEST_Y;
        classUnderTest.z = TEST_Z;
        classUnderTest.angle = TEST_ANGLE;

        classUnderTest.negate();

        assertEquals(classUnderTest.x, -TEST_X, "X component wasn't negated");
        assertEquals(classUnderTest.y, -TEST_Y, "Y component wasn't negated");
        assertEquals(classUnderTest.z, -TEST_Z, "Z component wasn't negated");
        assertEquals(classUnderTest.angle, -TEST_ANGLE, "Angle component wasn't negated");
    }

    @Test(groups = "unit")
    public void testNotEqualsToOther() throws Exception
    {
        Quat4d classUnderTest = new Quat4d();
        assertFalse(classUnderTest.equals(new Date()), "Should not be equal to a date");
    }

    @Test(groups = "unit", dataProvider = "equals")
    public void testEquals(Double[] src, Double[] dest, boolean expectedResult) throws Exception
    {
        Quat4d testClass = new Quat4d();
        testClass.x = src[0];
        testClass.y = src[1];
        testClass.z = src[2];
        testClass.angle = src[3];

        Quat4d classUnderTest = new Quat4d();
        classUnderTest.x = dest[0];
        classUnderTest.y = dest[1];
        classUnderTest.z = dest[2];
        classUnderTest.angle = dest[3];

        assertEquals(classUnderTest.equals(testClass), expectedResult, "Incorrect equals comparison");
        assertEquals(testClass.equals(classUnderTest), expectedResult, "Incorrect reverse equals comparison");
    }

    @Test(groups = "unit")
    public void testHashCode() throws Exception
    {
        final double TEST_X = 0.4;
        final double TEST_Y = -0.4;
        final double TEST_Z = 13.4;
        final double TEST_W = 0.25;

        Quat4d testClass = new Quat4d();
        testClass.x = TEST_X;
        testClass.y = TEST_Y;
        testClass.z = TEST_Z;
        testClass.angle = TEST_W;

        Quat4d classUnderTest = new Quat4d();
        classUnderTest.x = TEST_X;
        classUnderTest.y = TEST_Y;
        classUnderTest.z = TEST_Z;
        classUnderTest.angle = TEST_W;

        assertEquals(classUnderTest.hashCode(), testClass.hashCode(), "Same variables didn't generate same hash");
    }

    @DataProvider(name = "equals")
    public Object[][] generateEqualsData()
    {
        Object[][] retval = new Object[5][3];

        retval[0][0] = new Double[] {0.0, 0.0, 0.0, 1.0};
        retval[0][1] = new Double[] {0.0, 0.0, 0.0, 1.0};
        retval[0][2] = true;

        retval[1][0] = new Double[] {0.0, 0.0, 0.0, 1.0};
        retval[1][1] = new Double[] {0.1, 0.0, 0.0, 1.0};
        retval[1][2] = false;

        retval[2][0] = new Double[] {0.3, 0.0, 0.0, 1.0};
        retval[2][1] = new Double[] {0.3, 0.0, 0.0, 1.0};
        retval[2][2] = true;

        retval[3][0] = new Double[] {0.0, 0.5, 0.0, 1.0};
        retval[3][1] = new Double[] {0.0, 0.5, 0.0, 1.0};
        retval[3][2] = true;

        retval[4][0] = new Double[] {0.0, 0.0, 1.0, 1.0};
        retval[4][1] = new Double[] {0.0, 0.0, 1.0, 1.0};
        retval[4][2] = true;

        // NaN need to be equal?

        return retval;
    }
}
