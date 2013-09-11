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
import static org.testng.Assert.assertNotEquals;

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

        testAllZeroMatrix(classUnderTest);
    }

    @Test(groups = "unit")
    public void testNotEqualsToOther() throws Exception
    {
        Matrix4d classUnderTest = new Matrix4d();
        assertFalse(classUnderTest.equals(new Date()), "Should not be equal to a date");
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
        generateRandomMatrix(classUnderTest);

        classUnderTest.setIdentity();

        checkIsIdentityMatrix(classUnderTest);
    }

    @Test(groups = "unit")
    public void testSetMatrixWithNull() throws Exception
    {
        // Set a collection of random values to check that it is reset to identity
        Matrix4d classUnderTest = new Matrix4d();
        generateRandomMatrix(classUnderTest);

        classUnderTest.set((Matrix4d) null);

        assertNotEquals(classUnderTest.m00, 0.0, "[0][0] reset to 0");
        assertNotEquals(classUnderTest.m01, 0.0, "[0][1] reset to 0");
        assertNotEquals(classUnderTest.m02, 0.0, "[0][2] reset to 0");
        assertNotEquals(classUnderTest.m03, 0.0, "[0][3] reset to 0");

        assertNotEquals(classUnderTest.m10, 0.0, "[1][0] reset to 0");
        assertNotEquals(classUnderTest.m11, 0.0, "[1][1] reset to 0");
        assertNotEquals(classUnderTest.m12, 0.0, "[1][2] reset to 0");
        assertNotEquals(classUnderTest.m13, 0.0, "[1][3] reset to 0");

        assertNotEquals(classUnderTest.m20, 0.0, "[2][0] reset to 0");
        assertNotEquals(classUnderTest.m21, 0.0, "[2][1] reset to 0");
        assertNotEquals(classUnderTest.m22, 0.0, "[2][2] reset to 0");
        assertNotEquals(classUnderTest.m23, 0.0, "[2][3] reset to 0");

        assertNotEquals(classUnderTest.m30, 0.0, "[3][0] reset to 0");
        assertNotEquals(classUnderTest.m31, 0.0, "[3][1] reset to 0");
        assertNotEquals(classUnderTest.m32, 0.0, "[3][2] reset to 0");
        assertNotEquals(classUnderTest.m33, 0.0, "[3][3] reset to 0");
    }

    @Test(groups = "unit")
    public void testSetAxisAngleWithNull() throws Exception
    {
        // Set a collection of random values to check that it is reset to identity
        Matrix4d classUnderTest = new Matrix4d();
        generateRandomMatrix(classUnderTest);

        classUnderTest.set((AxisAngle4d) null);

        assertNotEquals(classUnderTest.m00, 0.0, "[0][0] reset to 0");
        assertNotEquals(classUnderTest.m01, 0.0, "[0][1] reset to 0");
        assertNotEquals(classUnderTest.m02, 0.0, "[0][2] reset to 0");
        assertNotEquals(classUnderTest.m03, 0.0, "[0][3] reset to 0");

        assertNotEquals(classUnderTest.m10, 0.0, "[1][0] reset to 0");
        assertNotEquals(classUnderTest.m11, 0.0, "[1][1] reset to 0");
        assertNotEquals(classUnderTest.m12, 0.0, "[1][2] reset to 0");
        assertNotEquals(classUnderTest.m13, 0.0, "[1][3] reset to 0");

        assertNotEquals(classUnderTest.m20, 0.0, "[2][0] reset to 0");
        assertNotEquals(classUnderTest.m21, 0.0, "[2][1] reset to 0");
        assertNotEquals(classUnderTest.m22, 0.0, "[2][2] reset to 0");
        assertNotEquals(classUnderTest.m23, 0.0, "[2][3] reset to 0");

        assertNotEquals(classUnderTest.m30, 0.0, "[3][0] reset to 0");
        assertNotEquals(classUnderTest.m31, 0.0, "[3][1] reset to 0");
        assertNotEquals(classUnderTest.m32, 0.0, "[3][2] reset to 0");
        assertNotEquals(classUnderTest.m33, 0.0, "[3][3] reset to 0");
    }

    @Test(groups = "unit")
    public void testSetVectorWithNull() throws Exception
    {
        // Set a collection of random values to check that it is reset to identity
        Matrix4d classUnderTest = new Matrix4d();
        generateRandomMatrix(classUnderTest);

        classUnderTest.set((Vector4d) null);

        assertNotEquals(classUnderTest.m00, 0.0, "[0][0] reset to 0");
        assertNotEquals(classUnderTest.m01, 0.0, "[0][1] reset to 0");
        assertNotEquals(classUnderTest.m02, 0.0, "[0][2] reset to 0");
        assertNotEquals(classUnderTest.m03, 0.0, "[0][3] reset to 0");

        assertNotEquals(classUnderTest.m10, 0.0, "[1][0] reset to 0");
        assertNotEquals(classUnderTest.m11, 0.0, "[1][1] reset to 0");
        assertNotEquals(classUnderTest.m12, 0.0, "[1][2] reset to 0");
        assertNotEquals(classUnderTest.m13, 0.0, "[1][3] reset to 0");

        assertNotEquals(classUnderTest.m20, 0.0, "[2][0] reset to 0");
        assertNotEquals(classUnderTest.m21, 0.0, "[2][1] reset to 0");
        assertNotEquals(classUnderTest.m22, 0.0, "[2][2] reset to 0");
        assertNotEquals(classUnderTest.m23, 0.0, "[2][3] reset to 0");

        assertNotEquals(classUnderTest.m30, 0.0, "[3][0] reset to 0");
        assertNotEquals(classUnderTest.m31, 0.0, "[3][1] reset to 0");
        assertNotEquals(classUnderTest.m32, 0.0, "[3][2] reset to 0");
        assertNotEquals(classUnderTest.m33, 0.0, "[3][3] reset to 0");
    }

    @Test(groups = "unit")
    public void testSetVector() throws Exception
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
        generateRandomMatrix(classUnderTest);

        classUnderTest.set(testVector);

        assertEquals(classUnderTest.m00, 0.0, "Non-zero value [0][0] coordinate");
        assertEquals(classUnderTest.m01, 0.0, "Non-zero value [0][1] coordinate");
        assertEquals(classUnderTest.m02, 0.0, "Non-zero value [0][2] coordinate");
        assertEquals(classUnderTest.m03, TEST_X, "Incorrect translation for [0][3]");

        assertEquals(classUnderTest.m10, 0.0, "Non-zero value [1][0] coordinate");
        assertEquals(classUnderTest.m11, 0.0, "Non-zero value [1][1] coordinate");
        assertEquals(classUnderTest.m12, 0.0, "Non-zero value [1][2] coordinate");
        assertEquals(classUnderTest.m13, TEST_Y, "Incorrect translation for [1][3]");

        assertEquals(classUnderTest.m20, 0.0, "Non-zero value [2][0] coordinate");
        assertEquals(classUnderTest.m21, 0.0, "Non-zero value [2][1] coordinate");
        assertEquals(classUnderTest.m22, 0.0, "Non-zero value [2][2] coordinate");
        assertEquals(classUnderTest.m23, TEST_Z, "Incorrect translation for [2][3]");

        assertEquals(classUnderTest.m30, 0.0, "Non-zero value [3][0] coordinate");
        assertEquals(classUnderTest.m31, 0.0, "Non-zero value [3][1] coordinate");
        assertEquals(classUnderTest.m32, 0.0, "Non-zero value [3][2] coordinate");
        assertEquals(classUnderTest.m33, TEST_W, "Incorrect translation for [3][3]");
    }

    @Test(groups = "unit")
    public void testSetAxisAngleZeroLength() throws Exception
    {
        Matrix4d classUnderTest = new Matrix4d();
        generateRandomMatrix(classUnderTest);

        classUnderTest.set(new AxisAngle4d());

        checkIsIdentityMatrix(classUnderTest);
    }

    @Test(groups = "unit")
    public void testClear() throws Exception
    {
        // Set a collection of random values to check that it is reset to identity
        Matrix4d classUnderTest = new Matrix4d();
        generateRandomMatrix(classUnderTest);

        classUnderTest.clear();

        testAllZeroMatrix(classUnderTest);
    }

    @Test(groups = "unit")
    public void testSet() throws Exception
    {
        // Set a collection of random values to check that it is reset to identity
        Matrix4d classUnderTest = new Matrix4d();
        generateRandomMatrix(classUnderTest);

        classUnderTest.set(new Matrix4d());

        testAllZeroMatrix(classUnderTest);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testTransformVectorV1Null() throws Exception
    {
        Matrix4d classUnderTest = new Matrix4d();

        classUnderTest.transform(null, new Vector4d());
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testTransformVectorV2Null() throws Exception
    {
        Matrix4d classUnderTest = new Matrix4d();

        classUnderTest.transform(new Vector4d(), null);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testTransformPoint3V1Null() throws Exception
    {
        Matrix4d classUnderTest = new Matrix4d();

        classUnderTest.transform(null, new Point3d());
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testTransformPoint3V2Null() throws Exception
    {
        Matrix4d classUnderTest = new Matrix4d();

        classUnderTest.transform(new Point3d(), null);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testTransformPoint4V1Null() throws Exception
    {
        Matrix4d classUnderTest = new Matrix4d();

        classUnderTest.transform(null, new Point4d());
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testTransformPoint4V2Null() throws Exception
    {
        Matrix4d classUnderTest = new Matrix4d();

        classUnderTest.transform(new Point4d(), null);
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
    public void testTransformPoint3IdentityMatrix() throws Exception
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

    @Test(groups = "unit", dependsOnMethods = "testSetIdentity")
    public void testTransformPoint4IdentityMatrix() throws Exception
    {
        final double TEST_X = 0.4;
        final double TEST_Y = -0.4;
        final double TEST_Z = 13.4;
        final double TEST_W = 1.5;

        Point4d testPoint = new Point4d();
        testPoint.x = TEST_X;
        testPoint.y = TEST_Y;
        testPoint.z = TEST_Z;
        testPoint.w = TEST_W;

        Matrix4d classUnderTest = new Matrix4d();
        classUnderTest.setIdentity();

        classUnderTest.transform(testPoint, testPoint);

        assertEquals(testPoint.x, TEST_X, "Same vector X component was changed unexpectedly");
        assertEquals(testPoint.y, TEST_Y, "Same vector Y component was changed unexpectedly");
        assertEquals(testPoint.z, TEST_Z, "Same vector Z component was changed unexpectedly");
        assertEquals(testPoint.w, TEST_W, "Same vector W component was changed unexpectedly");

        Point4d resultPoint = new Point4d();

        classUnderTest.transform(testPoint, resultPoint);

        assertEquals(resultPoint.x, TEST_X, "Different vector X component was changed unexpectedly");
        assertEquals(resultPoint.y, TEST_Y, "Different vector Y component was changed unexpectedly");
        assertEquals(resultPoint.z, TEST_Z, "Different vector Z component was changed unexpectedly");
        assertEquals(resultPoint.w, TEST_W, "Different vector W component was changed unexpectedly");
    }

    @Test(groups = "unit")
    public void testEmptyMatrixDeterminant() throws Exception
    {
        Matrix4d classUnderTest = new Matrix4d();

        assertEquals(classUnderTest.determinant(), 0.0, "Empty matrix should have determinant of zero");
    }

    @Test(groups = "unit")
    public void testIdentityMatrixDeterminant() throws Exception
    {
        Matrix4d classUnderTest = new Matrix4d();
        classUnderTest.setIdentity();

        assertEquals(classUnderTest.determinant(), 1.0, "Identity matrix should have determinant of one");
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

    /**
     * Take the input matrix and generate a set of random values for
     * every position in the matrix. Assumes we have a valid input class.
     *
     * @param src The matrix to randomise
     */
    private void generateRandomMatrix(Matrix4d src)
    {
        src.m00 = Math.random();
        src.m01 = Math.random();
        src.m02 = Math.random();
        src.m03 = Math.random();

        src.m10 = Math.random();
        src.m11 = Math.random();
        src.m12 = Math.random();
        src.m13 = Math.random();

        src.m20 = Math.random();
        src.m21 = Math.random();
        src.m22 = Math.random();
        src.m23 = Math.random();

        src.m30 = Math.random();
        src.m31 = Math.random();
        src.m32 = Math.random();
        src.m33 = Math.random();
    }

    private void testAllZeroMatrix(Matrix4d src)
    {
        assertEquals(src.m00, 0.0, "Non-zero default [0][0] coordinate");
        assertEquals(src.m01, 0.0, "Non-zero default [0][1] coordinate");
        assertEquals(src.m02, 0.0, "Non-zero default [0][2] coordinate");
        assertEquals(src.m03, 0.0, "Non-zero default [0][3] coordinate");

        assertEquals(src.m10, 0.0, "Non-zero default [1][0] coordinate");
        assertEquals(src.m11, 0.0, "Non-zero default [1][1] coordinate");
        assertEquals(src.m12, 0.0, "Non-zero default [1][2] coordinate");
        assertEquals(src.m13, 0.0, "Non-zero default [1][3] coordinate");

        assertEquals(src.m20, 0.0, "Non-zero default [2][0] coordinate");
        assertEquals(src.m21, 0.0, "Non-zero default [2][1] coordinate");
        assertEquals(src.m22, 0.0, "Non-zero default [2][2] coordinate");
        assertEquals(src.m23, 0.0, "Non-zero default [2][3] coordinate");

        assertEquals(src.m30, 0.0, "Non-zero default [3][0] coordinate");
        assertEquals(src.m31, 0.0, "Non-zero default [3][1] coordinate");
        assertEquals(src.m32, 0.0, "Non-zero default [3][2] coordinate");
        assertEquals(src.m33, 0.0, "Non-zero default [3][3] coordinate");
    }

    private void checkIsIdentityMatrix(Matrix4d src)
    {
        assertEquals(src.m00, 1.0, "[0][0] not set to 1");
        assertEquals(src.m01, 0.0, "[0][1] not reset to 0");
        assertEquals(src.m02, 0.0, "[0][2] not reset to 0");
        assertEquals(src.m03, 0.0, "[0][3] not reset to 0");

        assertEquals(src.m10, 0.0, "[1][0] not reset to 0");
        assertEquals(src.m11, 1.0, "[1][1] not set to 1");
        assertEquals(src.m12, 0.0, "[1][2] not reset to 0");
        assertEquals(src.m13, 0.0, "[1][3] not reset to 0");

        assertEquals(src.m20, 0.0, "[2][0] not reset to 0");
        assertEquals(src.m21, 0.0, "[2][1] not reset to 0");
        assertEquals(src.m22, 1.0, "[2][2] not set to 1");
        assertEquals(src.m23, 0.0, "[2][3] not reset to 0");

        assertEquals(src.m30, 0.0, "[3][0] not reset to 0");
        assertEquals(src.m31, 0.0, "[3][1] not reset to 0");
        assertEquals(src.m32, 0.0, "[3][2] not reset to 0");
        assertEquals(src.m33, 1.0, "[3][3] not set to 1");
    }
}
