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

    @Test(groups = "unit", dataProvider = "interpolate")
    public void testInterpolate(Double[] t1, Double[] t2, double interpolateAmount, Double[] expectedPoint) throws Exception
    {
        Vector3d vector1 = new Vector3d();
        vector1.x = t1[0];
        vector1.y = t1[1];
        vector1.z = t1[2];

        Vector3d vector2 = new Vector3d();
        vector2.x = t2[0];
        vector2.y = t2[1];
        vector2.z = t2[2];

        Vector3d classUnderTest = new Vector3d();
        classUnderTest.interpolate(vector1, vector2, interpolateAmount);

        assertEquals(classUnderTest.x, expectedPoint[0], 0.0001, "X coordinate interpolated incorrectly");
        assertEquals(classUnderTest.y, expectedPoint[1], 0.0001, "Y coordinate interpolated incorrectly");
        assertEquals(classUnderTest.z, expectedPoint[2], 0.0001, "Z coordinate interpolated incorrectly");
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
    public void testAddVectorV1Null() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.add(null, new Vector3d());
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testAddVectorV2Null() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.add(new Vector3d(), null);
    }

    @Test(groups = "unit", dataProvider = "add")
    public void testAddVector(Double[] v1, Double[] v2, Double[] expectedResult) throws Exception
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
    public void testAddVectorWithSelf(Double[] v1, Double[] v2, Double[] expectedResult) throws Exception
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
    public void testSubVectorV1Null() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.sub(null, new Vector3d());
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testSubVectorV2Null() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.sub(new Vector3d(), null);
    }

    @Test(groups = "unit", dataProvider = "sub")
    public void testSubVector(Double[] v1, Double[] v2, Double[] expectedResult) throws Exception
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

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testAddPointV1Null() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.add(null, new Point3d());
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testAddPointV2Null() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.add(new Point3d(), null);
    }

    @Test(groups = "unit", dataProvider = "add")
    public void testAddPoint(Double[] v1, Double[] v2, Double[] expectedResult) throws Exception
    {
        Point3d vx1 = new Point3d();
        vx1.x = v1[0];
        vx1.y = v1[1];
        vx1.z = v1[2];

        Point3d vx2 = new Point3d();
        vx2.x = v2[0];
        vx2.y = v2[1];
        vx2.z = v2[2];

        Vector3d classUnderTest = new Vector3d();

        classUnderTest.add(vx1, vx2);

        assertEquals(classUnderTest.x, expectedResult[0], "Incorrect X component");
        assertEquals(classUnderTest.y, expectedResult[1], "Incorrect Y component");
        assertEquals(classUnderTest.z, expectedResult[2], "Incorrect Z component");
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testSubPointV1Null() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.sub(null, new Point3d());
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testSubPointV2Null() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.sub(new Point3d(), null);
    }

    @Test(groups = "unit", dataProvider = "sub")
    public void testSubPoint(Double[] v1, Double[] v2, Double[] expectedResult) throws Exception
    {
        Point3d vx1 = new Point3d();
        vx1.x = v1[0];
        vx1.y = v1[1];
        vx1.z = v1[2];

        Point3d vx2 = new Point3d();
        vx2.x = v2[0];
        vx2.y = v2[1];
        vx2.z = v2[2];

        Vector3d classUnderTest = new Vector3d();

        classUnderTest.sub(vx1, vx2);

        assertEquals(classUnderTest.x, expectedResult[0], "Incorrect X component");
        assertEquals(classUnderTest.y, expectedResult[1], "Incorrect Y component");
        assertEquals(classUnderTest.z, expectedResult[2], "Incorrect Z component");
    }

    @Test(groups = "unit")
    public void testLength() throws Exception
    {
        Vector3d classUnderTest = new Vector3d();
        classUnderTest.x = 1.0;
        classUnderTest.y = 1.0;
        classUnderTest.z = 1.0;

        assertEquals(classUnderTest.length(), Math.sqrt(3), "Incorrect length");
        assertEquals(classUnderTest.lengthSquared(), 3.0, "Incorrect squared length");
    }

    @Test(groups = "unit")
    public void testScale() throws Exception
    {
        final double TEST_X = 0.4;
        final double TEST_Y = -1.0;
        final double TEST_Z = 4.0;
        final double TEST_SCALE = 0.5;

        Vector3d classUnderTest = new Vector3d();
        classUnderTest.x = TEST_X;
        classUnderTest.y = TEST_Y;
        classUnderTest.z = TEST_Z;

        classUnderTest.scale(TEST_SCALE);

        assertEquals(classUnderTest.x, TEST_X * TEST_SCALE, "Incorrect X scaled value");
        assertEquals(classUnderTest.y, TEST_Y * TEST_SCALE, "Incorrect Y scaled value");
        assertEquals(classUnderTest.z, TEST_Z * TEST_SCALE, "Incorrect Z scaled value");
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

    @DataProvider(name = "interpolate")
    public Object[][] generateInterpolateData()
    {
        Object[][] retval = new Object[10][4];

        retval[0][0] = new Double[] { 0.0, 0.0, 0.0 };
        retval[0][1] = new Double[] { 1.0, 1.0, 1.0 };
        retval[0][2] = 0;
        retval[0][3] = new Double[] { 0.0, 0.0, 0.0 };

        retval[1][0] = new Double[] { 0.0, 0.0, 0.0 };
        retval[1][1] = new Double[] { 1.0, 1.0, 1.0 };
        retval[1][2] = 0.5;
        retval[1][3] = new Double[] { 0.5, 0.5, 0.5 };

        retval[2][0] = new Double[] { 0.0, 0.0, 0.0 };
        retval[2][1] = new Double[] { 1.0, 1.0, 1.0 };
        retval[2][2] = 1;
        retval[2][3] = new Double[] { 1.0, 1.0, 1.0 };

        retval[3][0] = new Double[] { 0.0, 1.0, 0.0 };
        retval[3][1] = new Double[] { 1.0, 1.0, 1.0 };
        retval[3][2] = 0;
        retval[3][3] = new Double[] { 0.0, 1.0, 0.0 };

        retval[4][0] = new Double[] { 0.0, 1.0, 0.0 };
        retval[4][1] = new Double[] { 1.0, 1.0, 1.0 };
        retval[4][2] = 0.5;
        retval[4][3] = new Double[] { 0.5, 1.0, 0.5 };

        retval[5][0] = new Double[] { 0.0, 1.0, 0.0 };
        retval[5][1] = new Double[] { 1.0, 1.0, 1.0 };
        retval[5][2] = 1;
        retval[5][3] = new Double[] { 1.0, 1.0, 1.0 };

        // Now some outside the range of the points

        retval[6][0] = new Double[] { 0.0, 0.0, 0.0 };
        retval[6][1] = new Double[] { 1.0, 1.0, 1.0 };
        retval[6][2] = 1.5;
        retval[6][3] = new Double[] { 1.5, 1.5, 1.5 };

        retval[7][0] = new Double[] { 0.0, 1.0, 0.0 };
        retval[7][1] = new Double[] { 1.0, 1.0, 1.0 };
        retval[7][2] = 1.5;
        retval[7][3] = new Double[] { 1.5, 1.0, 1.5 };

        retval[8][0] = new Double[] { 0.0, 0.0, 0.0 };
        retval[8][1] = new Double[] { 1.0, 1.0, 1.0 };
        retval[8][2] = -1.3;
        retval[8][3] = new Double[] { -1.3, -1.3, -1.3 };

        retval[9][0] = new Double[] { 0.0, 1.0, 0.0 };
        retval[9][1] = new Double[] { 1.0, 1.0, 1.0 };
        retval[9][2] = -1.3;
        retval[9][3] = new Double[] { -1.3, 1.0, -1.3 };

        return retval;
    }
}
