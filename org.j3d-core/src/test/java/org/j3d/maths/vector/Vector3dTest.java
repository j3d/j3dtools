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

import java.util.Date;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * This class does something
 *
 * @author justin
 */
public class Vector3dTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();

        assertEquals(classUnderTest.x, 0.0, "Non-zero default x coordinate");
        assertEquals(classUnderTest.y, 0.0, "Non-zero default y coordinate");
        assertEquals(classUnderTest.z, 0.0, "Non-zero default z coordinate");
    }

    @Test(groups = "unit")
    public void testSet() throws Exception
    {
        final double TEST_X = 0.4;
        final double TEST_Y = -1.0;
        final double TEST_Z = 4.0;

        Vector3d classUnderTest = new Vector3d();
        classUnderTest.set(TEST_X, TEST_Y, TEST_Z);

        assertEquals(classUnderTest.x, TEST_X, 0.001, "X Coordinate incorrectly set");
        assertEquals(classUnderTest.y, TEST_Y, 0.001, "Y Coordinate incorrectly set");
        assertEquals(classUnderTest.z, TEST_Z, 0.001, "Z Coordinate incorrectly set");
    }

    @Test(groups = "unit")
    public void testNormaliseZeroLength() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.normalise();

        assertEquals(classUnderTest.x, 0.0, "X Coordinate is not zero");
        assertEquals(classUnderTest.y, 0.0, "Y Coordinate is not zero");
        assertEquals(classUnderTest.z, 0.0, "Z Coordinate is not zero");
    }

    @Test(groups = "unit")
    public void testNormalise() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.set(10, -0.5, 0.1);
        classUnderTest.normalise();

        double l2 = Math.sqrt(classUnderTest.x * classUnderTest.x +
                    classUnderTest.y * classUnderTest.y +
                    classUnderTest.z * classUnderTest.z);

        assertEquals(l2, 1.0, "Normalised length is not 1");
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testCrossV1Null() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.cross(null, new Vector3d());
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testCrossV2Null() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.cross(new Vector3d(), null);
    }

    @Test(groups = "unit", dataProvider = "cross product")
    public void testCrossProduct(Double[] v1, Double[] v2, Double[] expectedResult) throws Exception
    {
        Vector3d vx1 = new Vector3d();
        vx1.x = v1[0];
        vx1.y = v1[1];
        vx1.z = v1[2];

        Vector3d vx2 = new Vector3d();
        vx2.x = v2[0];
        vx2.y = v2[1];
        vx2.z = v2[2];

        Vector3d classUnderTest = new Vector3d();
        classUnderTest.cross(vx1, vx2);

        assertEquals(classUnderTest.x, expectedResult[0], "Incorrect X component of cross product");
        assertEquals(classUnderTest.y, expectedResult[1], "Incorrect Y component of cross product");
        assertEquals(classUnderTest.z, expectedResult[2], "Incorrect Z component of cross product");
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testDotNull() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.dot(null);
    }

    @Test(groups = "unit", dataProvider = "dot")
    public void testDot(Double[] v1, Double[] v2, double expectedResult) throws Exception
    {
        Vector3d vx1 = new Vector3d();
        vx1.x = v1[0];
        vx1.y = v1[1];
        vx1.z = v1[2];

        Vector3d classUnderTest = new Vector3d();
        classUnderTest.x = v2[0];
        classUnderTest.y = v2[1];
        classUnderTest.z = v2[2];

        assertEquals(classUnderTest.dot(vx1), expectedResult, "Incorrect dot product");
        assertEquals(vx1.dot(classUnderTest), expectedResult, "Incorrect reverse dot product");
    }

    @Test(groups = "unit")
    public void testNotEqualsToOther() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        assertFalse(classUnderTest.equals(new Date()), "Should not be equal to a date");
    }

    @Test(groups = "unit", dataProvider = "equals")
    public void testEquals(Double[] src, Double[] dest, boolean expectedResult) throws Exception
    {
        Vector3d testClass = new Vector3d();
        testClass.x = src[0];
        testClass.y = src[1];
        testClass.z = src[2];

        Vector3d classUnderTest = new Vector3d();
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

        Vector3d testClass = new Vector3d();
        testClass.x = TEST_X;
        testClass.y = TEST_Y;
        testClass.z = TEST_Z;

        Vector3d classUnderTest = new Vector3d();
        classUnderTest.x = TEST_X;
        classUnderTest.y = TEST_Y;
        classUnderTest.z = TEST_Z;

        assertEquals(classUnderTest.hashCode(), testClass.hashCode(), "Same variables didn't generate same hash");
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testAddV1Null() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.add(null, new Vector3d());
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testAddV2Null() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.add(new Vector3d(), null);
    }

    @Test(groups = "unit", dataProvider = "add")
    public void testAdd(Double[] v1, Double[] v2, Double[] expectedResult) throws Exception
    {
        Vector3d vx1 = new Vector3d();
        vx1.x = v1[0];
        vx1.y = v1[1];
        vx1.z = v1[2];

        Vector3d vx2 = new Vector3d();
        vx2.x = v2[0];
        vx2.y = v2[1];
        vx2.z = v2[2];

        Vector3d classUnderTest = new Vector3d();

        classUnderTest.add(vx1, vx2);

        assertEquals(classUnderTest.x, expectedResult[0], "Incorrect X component");
        assertEquals(classUnderTest.y, expectedResult[1], "Incorrect Y component");
        assertEquals(classUnderTest.z, expectedResult[2], "Incorrect Z component");
    }

    @Test(groups = "unit", dataProvider = "add")
    public void testAddWithSelf(Double[] v1, Double[] v2, Double[] expectedResult) throws Exception
    {
        Vector3d vx1 = new Vector3d();
        vx1.x = v1[0];
        vx1.y = v1[1];
        vx1.z = v1[2];

        Vector3d classUnderTest = new Vector3d();
        classUnderTest.x = v2[0];
        classUnderTest.y = v2[1];
        classUnderTest.z = v2[2];

        classUnderTest.add(vx1, classUnderTest);

        assertEquals(classUnderTest.x, expectedResult[0], "Incorrect X component");
        assertEquals(classUnderTest.y, expectedResult[1], "Incorrect Y component");
        assertEquals(classUnderTest.z, expectedResult[2], "Incorrect Z component");

        // Add in other order, should give same results
        classUnderTest.x = v2[0];
        classUnderTest.y = v2[1];
        classUnderTest.z = v2[2];

        classUnderTest.add(classUnderTest, vx1);

        assertEquals(classUnderTest.x, expectedResult[0], "Incorrect X component");
        assertEquals(classUnderTest.y, expectedResult[1], "Incorrect Y component");
        assertEquals(classUnderTest.z, expectedResult[2], "Incorrect Z component");
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testSubV1Null() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.sub(null, new Vector3d());
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testSubV2Null() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.sub(new Vector3d(), null);
    }

    @Test(groups = "unit", dataProvider = "sub")
    public void testSub(Double[] v1, Double[] v2, Double[] expectedResult) throws Exception
    {
        Vector3d vx1 = new Vector3d();
        vx1.x = v1[0];
        vx1.y = v1[1];
        vx1.z = v1[2];

        Vector3d vx2 = new Vector3d();
        vx2.x = v2[0];
        vx2.y = v2[1];
        vx2.z = v2[2];

        Vector3d classUnderTest = new Vector3d();

        classUnderTest.sub(vx1, vx2);

        assertEquals(classUnderTest.x, expectedResult[0], "Incorrect X component");
        assertEquals(classUnderTest.y, expectedResult[1], "Incorrect Y component");
        assertEquals(classUnderTest.z, expectedResult[2], "Incorrect Z component");
    }

    @DataProvider(name = "cross product")
    public Object[][] generateCrossProductData()
    {
        return new Object[][]
        {
            // X x Y = Z
            { new Double[] { 1.0, 0.0, 0.0}, new Double[] { 0.0, 1.0, 0.0 }, new Double[] { 0.0, 0.0, 1.0 }},
            // X x Z = Y
            { new Double[] { 1.0, 0.0, 0.0}, new Double[] { 0.0, 0.0, 1.0 }, new Double[] { 0.0, 1.0, 0.0 }},
            // Y x Z = X
            { new Double[] { 0.0, 1.0, 0.0}, new Double[] { 0.0, 0.0, 1.0 }, new Double[] { 1.0, 0.0, 0.0 }},

            // Y x X = -Z
            { new Double[] { 0.0, 1.0, 0.0}, new Double[] { 1.0, 0.0, 0.0 }, new Double[] { 0.0, 0.0, -1.0 }},
            // Z x X = -Y
            { new Double[] { 0.0, 0.0, 1.0}, new Double[] { 1.0, 0.0, 0.0 }, new Double[] { 0.0, -1.0, 0.0 }},
            // Z x Y = -X
            { new Double[] { 0.0, 0.0, 1.0}, new Double[] { 0.0, 1.0, 0.0 }, new Double[] { -1.0, 0.0, 0.0 }}
        };
    }

    @DataProvider(name = "dot")
    public Object[][] generateDotData()
    {
        return new Object[][]
        {
            // X.Y = 0
            { new Double[] { 1.0, 0.0, 0.0}, new Double[] { 0.0, 1.0, 0.0 }, 0.0},
            // X.X = 1
            { new Double[] { 1.0, 0.0, 0.0}, new Double[] { 1.0, 0.0, 0.0 }, 1.0},
            // X.Z = 0
            { new Double[] { 1.0, 0.0, 0.0}, new Double[] { 0.0, 0.0, 1.0 }, 0.0},
        };
    }

    @DataProvider(name = "add")
    public Object[][] generateAddData()
    {
        return new Object[][]
            {
                { new Double[] { 1.0, 0.0, 0.0}, new Double[] { 0.0, 1.0, 0.0 }, new Double[] { 1.0, 1.0, 0.0 }},
                { new Double[] { 1.0, 0.0, 0.0}, new Double[] { 0.0, 0.0, 1.0 }, new Double[] { 1.0, 0.0, 1.0 }},
                { new Double[] { 0.0, 1.0, 0.0}, new Double[] { 0.0, 0.0, 1.0 }, new Double[] { 0.0, 1.0, 1.0 }},
                { new Double[] { -1.0, 0.0, 0.0}, new Double[] { 0.0, 1.0, 0.0 }, new Double[] { -1.0, 1.0, 0.0 }},
                { new Double[] { -1.0, 0.0, 0.0}, new Double[] { 0.0, 0.0, 1.0 }, new Double[] { -1.0, 0.0, 1.0 }},
                { new Double[] { 0.0, -1.0, 0.0}, new Double[] { 0.0, 0.0, 1.0 }, new Double[] { 0.0, -1.0, 1.0 }},
                { new Double[] { -1.0, 0.0, 0.0}, new Double[] { 1.0, 0.0, 0.0 }, new Double[] { 0.0, 0.0, 0.0 }},
                { new Double[] { 0.0, -1.0, 0.0}, new Double[] { 0.0, 1.0, 0.0 }, new Double[] { 0.0, 0.0, 0.0 }},
                { new Double[] { 0.0,  0.0, -1.0}, new Double[] { 0.0, 0.0, 1.0 }, new Double[] { 0.0, 0.0, 0.0 }},
            };
    }

    @DataProvider(name = "sub")
    public Object[][] generateSubData()
    {
        return new Object[][]
            {
                { new Double[] { 1.0, 0.0, 0.0}, new Double[] { 0.0, 1.0, 0.0 }, new Double[] { 1.0, -1.0, 0.0 }},
                { new Double[] { 1.0, 0.0, 0.0}, new Double[] { 0.0, 0.0, 1.0 }, new Double[] { 1.0, 0.0, -1.0 }},
                { new Double[] { 0.0, 1.0, 0.0}, new Double[] { 0.0, 0.0, 1.0 }, new Double[] { 0.0, 1.0, -1.0 }},
                { new Double[] { -1.0, 0.0, 0.0}, new Double[] { 0.0, 1.0, 0.0 }, new Double[] { -1.0, -1.0, 0.0 }},
                { new Double[] { -1.0, 0.0, 0.0}, new Double[] { 0.0, 0.0, 1.0 }, new Double[] { -1.0, 0.0, -1.0 }},
                { new Double[] { 0.0, -1.0, 0.0}, new Double[] { 0.0, 0.0, 1.0 }, new Double[] { 0.0, -1.0, -1.0 }},
                { new Double[] { -1.0, 0.0, 0.0}, new Double[] { 1.0, 0.0, 0.0 }, new Double[] { -2.0, 0.0, 0.0 }},
                { new Double[] { 0.0, -1.0, 0.0}, new Double[] { 0.0, 1.0, 0.0 }, new Double[] { 0.0, -2.0, 0.0 }},
                { new Double[] { 0.0,  0.0, -1.0}, new Double[] { 0.0, 0.0, 1.0 }, new Double[] { 0.0, 0.0, -2.0 }},
            };
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
