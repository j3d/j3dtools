/*
 * j3d.org Copyright (c) 2001-2013
 *
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read docs/LGPL.txt for more information
 *
 *  This software comes with the standard NO WARRANTY disclaimer for any
 *  purpose. Use it at your own risk. If there's a problem you get to fix it.
 */

package org.j3d.maths.vector;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * This class does something
 *
 * @author justin
 */
public class Point3dTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        Point3d classUnderTest = new Point3d();

        assertEquals(classUnderTest.x, 0.0, "Non-zero default x coordinate");
        assertEquals(classUnderTest.y, 0.0, "Non-zero default y coordinate");
        assertEquals(classUnderTest.z, 0.0, "Non-zero default z coordinate");
    }

    @Test(groups = "unit", dataProvider = "equals")
    public void testEquals(Double[] src, Double[] dest, boolean expectedResult) throws Exception
    {
        Point3d testClass = new Point3d();
        testClass.x = src[0];
        testClass.y = src[1];
        testClass.z = src[2];

        Point3d classUnderTest = new Point3d();
        classUnderTest.x = dest[0];
        classUnderTest.y = dest[1];
        classUnderTest.z = dest[2];

        assertEquals(classUnderTest.equals(testClass), expectedResult, "Incorrect equals comparison");
        assertEquals(testClass.equals(classUnderTest), expectedResult, "Incorrect reverse equals comparison");
    }

    @Test(groups = "unit")
    public void testHashCode() throws Exception
    {
        final double TEST_X = 0.4;
        final double TEST_Y = -0.4;
        final double TEST_Z = 13.4;

        Point3d testClass = new Point3d();
        testClass.x = TEST_X;
        testClass.y = TEST_Y;
        testClass.z = TEST_Z;

        Point3d classUnderTest = new Point3d();
        classUnderTest.x = TEST_X;
        classUnderTest.y = TEST_Y;
        classUnderTest.z = TEST_Z;

        assertEquals(classUnderTest.hashCode(), testClass.hashCode(), "Same variables didn't generate same hash");
    }

    @DataProvider(name = "equals")
    public Object[][] generateEqualsData()
    {
        Object[][] retval = new Object[5][3];

        retval[0][0] = new Double[] {0.0, 0.0, 0.0};
        retval[0][1] = new Double[] {0.0, 0.0, 0.0};
        retval[0][2] = true;

        retval[1][0] = new Double[] {0.0, 0.0, 0.0};
        retval[1][1] = new Double[] {0.1, 0.0, 0.0};
        retval[1][2] = false;

        retval[2][0] = new Double[] {0.3, 0.0, 0.0 };
        retval[2][1] = new Double[] {0.3, 0.0, 0.0 };
        retval[2][2] = true;

        retval[3][0] = new Double[] {0.0, 0.5, 0.0 };
        retval[3][1] = new Double[] {0.0, 0.5, 0.0 };
        retval[3][2] = true;

        retval[4][0] = new Double[] {0.0, 0.0, 1.0 };
        retval[4][1] = new Double[] {0.0, 0.0, 1.0 };
        retval[4][2] = true;

        // NaN need to be equal?

        return retval;
    }
}
