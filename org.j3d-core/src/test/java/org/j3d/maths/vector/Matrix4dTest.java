/*
 * j4d.org Copyright (c) 2001-2013
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
 * Unit test for the Matrix4d class
 *
 * @author justin
 */
public class Matrix4dTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        Matrix4d classUnderTest = new Matrix4d();

        assertEquals(classUnderTest.m00, 0.0, "Non-zero default [0][0] coordinate");
        assertEquals(classUnderTest.m01, 0.0, "Non-zero default [0][1] coordinate");
        assertEquals(classUnderTest.m02, 0.0, "Non-zero default [0][2] coordinate");
        assertEquals(classUnderTest.m03, 0.0, "Non-zero default [0][3] coordinate");

        assertEquals(classUnderTest.m10, 0.0, "Non-zero default [1][0] coordinate");
        assertEquals(classUnderTest.m11, 0.0, "Non-zero default [1][1] coordinate");
        assertEquals(classUnderTest.m12, 0.0, "Non-zero default [1][2] coordinate");
        assertEquals(classUnderTest.m13, 0.0, "Non-zero default [1][3] coordinate");

        assertEquals(classUnderTest.m20, 0.0, "Non-zero default [2][0] coordinate");
        assertEquals(classUnderTest.m21, 0.0, "Non-zero default [2][1] coordinate");
        assertEquals(classUnderTest.m22, 0.0, "Non-zero default [2][2] coordinate");
        assertEquals(classUnderTest.m23, 0.0, "Non-zero default [2][3] coordinate");

        assertEquals(classUnderTest.m30, 0.0, "Non-zero default [3][0] coordinate");
        assertEquals(classUnderTest.m31, 0.0, "Non-zero default [3][1] coordinate");
        assertEquals(classUnderTest.m32, 0.0, "Non-zero default [3][2] coordinate");
        assertEquals(classUnderTest.m33, 0.0, "Non-zero default [3][3] coordinate");
    }

    @Test(groups = "unit", dataProvider = "equals")
    public void testEquals(Double[] src, Double[] dest, boolean expectedResult) throws Exception
    {
        Matrix4d testClass = new Matrix4d();
        testClass.m00 = src[0];
        testClass.m11 = src[1];
        testClass.m22 = src[2];
        testClass.m33 = src[3];

        Matrix4d classUnderTest = new Matrix4d();
        classUnderTest.m00 = dest[0];
        classUnderTest.m11 = dest[1];
        classUnderTest.m22 = dest[2];
        classUnderTest.m33 = dest[3];

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

        Matrix4d testClass = new Matrix4d();
        testClass.m00 = TEST_X;
        testClass.m11 = TEST_Y;
        testClass.m22 = TEST_Z;
        testClass.m33 = TEST_W;

        Matrix4d classUnderTest = new Matrix4d();
        classUnderTest.m00 = TEST_X;
        classUnderTest.m11 = TEST_Y;
        classUnderTest.m22 = TEST_Z;
        classUnderTest.m33 = TEST_W;

        assertEquals(classUnderTest.hashCode(), testClass.hashCode(), "Same variables didn't generate same hash");
    }

    @Test(groups = "unit")
    public void testSetIdentity() throws Exception
    {
        // Set a collection of random values to check that it is reset to identity
        Matrix4d classUnderTest = new Matrix4d();
        classUnderTest.m00 = Math.random();
        classUnderTest.m01 = Math.random();
        classUnderTest.m02 = Math.random();
        classUnderTest.m03 = Math.random();

        classUnderTest.m10 = Math.random();
        classUnderTest.m11 = Math.random();
        classUnderTest.m12 = Math.random();
        classUnderTest.m13 = Math.random();

        classUnderTest.m20 = Math.random();
        classUnderTest.m21 = Math.random();
        classUnderTest.m22 = Math.random();
        classUnderTest.m23 = Math.random();

        classUnderTest.m30 = Math.random();
        classUnderTest.m31 = Math.random();
        classUnderTest.m32 = Math.random();
        classUnderTest.m33 = Math.random();

        classUnderTest.setIdentity();

        assertEquals(classUnderTest.m00, 1.0, "[0][0] not set to 1");
        assertEquals(classUnderTest.m01, 0.0, "[0][1] not reset to 0");
        assertEquals(classUnderTest.m02, 0.0, "[0][2] not reset to 0");
        assertEquals(classUnderTest.m03, 0.0, "[0][3] not reset to 0");

        assertEquals(classUnderTest.m10, 0.0, "[1][0] not reset to 0");
        assertEquals(classUnderTest.m11, 1.0, "[1][1] not set to 1");
        assertEquals(classUnderTest.m12, 0.0, "[1][2] not reset to 0");
        assertEquals(classUnderTest.m13, 0.0, "[1][3] not reset to 0");

        assertEquals(classUnderTest.m20, 0.0, "[2][0] not reset to 0");
        assertEquals(classUnderTest.m21, 0.0, "[2][1] not reset to 0");
        assertEquals(classUnderTest.m22, 1.0, "[2][2] not set to 1");
        assertEquals(classUnderTest.m23, 0.0, "[2][3] not reset to 0");

        assertEquals(classUnderTest.m30, 0.0, "[3][0] not reset to 0");
        assertEquals(classUnderTest.m31, 0.0, "[3][1] not reset to 0");
        assertEquals(classUnderTest.m32, 0.0, "[3][2] not reset to 0");
        assertEquals(classUnderTest.m33, 1.0, "[3][3] not set to 1");
    }

    @Test(groups = "unit", dependsOnMethods = "testSetIdentity")
    public void testTransformVectorIdentityMatrix() throws Exception
    {
        final double TEST_X = 0.4;
        final double TEST_Y = -0.4;
        final double TEST_Z = 13.4;
        final double TEST_W = 0.25;

        Vector4d testVector = new Vector4d();
        testVector.x = TEST_X;
        testVector.y = TEST_Y;
        testVector.z = TEST_Z;
        testVector.w = TEST_W;

        Matrix4d classUnderTest = new Matrix4d();
        classUnderTest.setIdentity();

        classUnderTest.transform(testVector, testVector);

        assertEquals(testVector.x, TEST_X, "Same vector X component was changed unexpectedly");
        assertEquals(testVector.y, TEST_Y, "Same vector Y component was changed unexpectedly");
        assertEquals(testVector.z, TEST_Z, "Same vector Z component was changed unexpectedly");
        assertEquals(testVector.w, TEST_W, "Same vector W component was changed unexpectedly");

        Vector4d resultVector = new Vector4d();

        classUnderTest.transform(testVector, resultVector);

        assertEquals(resultVector.x, TEST_X, "Different vector X component was changed unexpectedly");
        assertEquals(resultVector.y, TEST_Y, "Different vector Y component was changed unexpectedly");
        assertEquals(resultVector.z, TEST_Z, "Different vector Z component was changed unexpectedly");
        assertEquals(resultVector.w, TEST_W, "Different vector W component was changed unexpectedly");
    }

    @Test(groups = "unit", dependsOnMethods = "testSetIdentity")
    public void testTransformPointIdentityMatrix() throws Exception
    {
        final double TEST_X = 0.4;
        final double TEST_Y = -0.4;
        final double TEST_Z = 13.4;

        Point3d testPoint = new Point3d();
        testPoint.x = TEST_X;
        testPoint.y = TEST_Y;
        testPoint.z = TEST_Z;

        Matrix4d classUnderTest = new Matrix4d();
        classUnderTest.setIdentity();

        classUnderTest.transform(testPoint, testPoint);

        assertEquals(testPoint.x, TEST_X, "Same vector X component was changed unexpectedly");
        assertEquals(testPoint.y, TEST_Y, "Same vector Y component was changed unexpectedly");
        assertEquals(testPoint.z, TEST_Z, "Same vector Z component was changed unexpectedly");

        Point3d resultPoint = new Point3d();

        classUnderTest.transform(testPoint, resultPoint);

        assertEquals(resultPoint.x, TEST_X, "Different vector X component was changed unexpectedly");
        assertEquals(resultPoint.y, TEST_Y, "Different vector Y component was changed unexpectedly");
        assertEquals(resultPoint.z, TEST_Z, "Different vector Z component was changed unexpectedly");
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
