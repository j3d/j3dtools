package org.j3d.util;

import org.j3d.maths.vector.Matrix4d;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for the MatrixUtils class
 *
 * @author justin
 */
public class MatrixUtilsTest
{
    @Test(groups = "unit")
    public void testInverse() throws Exception
    {
        Matrix4d testMatrix = new Matrix4d();
        testMatrix.m00 = 1;
        testMatrix.m01 = 2;
        testMatrix.m02 = 2;
        testMatrix.m03 = 2;

        testMatrix.m10 = 2;
        testMatrix.m11 = 1;
        testMatrix.m12 = 2;
        testMatrix.m13 = 2;

        testMatrix.m20 = 2;
        testMatrix.m21 = 2;
        testMatrix.m22 = 1;
        testMatrix.m23 = 2;

        testMatrix.m30 = 2;
        testMatrix.m31 = 2;
        testMatrix.m32 = 2;
        testMatrix.m33 = 1;

        MatrixUtils classUnderTest = new MatrixUtils();

        Matrix4d outputMatrix = new Matrix4d();

        classUnderTest.inverse(testMatrix, outputMatrix);

        Matrix4d result = new Matrix4d();
        result.mul(testMatrix, outputMatrix);

        // check that it is an identity matrix afterwards.
        checkIsIdentityMatrix(result);
    }

    private void checkIsIdentityMatrix(Matrix4d src)
    {
        assertEquals(src.m00, 1.0, 0.001, "[0][0] not set to 1");
        assertEquals(src.m01, 0.0, 0.001, "[0][1] not reset to 0");
        assertEquals(src.m02, 0.0, 0.001, "[0][2] not reset to 0");
        assertEquals(src.m03, 0.0, 0.001, "[0][3] not reset to 0");

        assertEquals(src.m10, 0.0, 0.001, "[1][0] not reset to 0");
        assertEquals(src.m11, 1.0, 0.001, "[1][1] not set to 1");
        assertEquals(src.m12, 0.0, 0.001, "[1][2] not reset to 0");
        assertEquals(src.m13, 0.0, 0.001, "[1][3] not reset to 0");

        assertEquals(src.m20, 0.0, 0.001, "[2][0] not reset to 0");
        assertEquals(src.m21, 0.0, 0.001, "[2][1] not reset to 0");
        assertEquals(src.m22, 1.0, 0.001, "[2][2] not set to 1");
        assertEquals(src.m23, 0.0, 0.001, "[2][3] not reset to 0");

        assertEquals(src.m30, 0.0, 0.001, "[3][0] not reset to 0");
        assertEquals(src.m31, 0.0, 0.001, "[3][1] not reset to 0");
        assertEquals(src.m32, 0.0, 0.001, "[3][2] not reset to 0");
        assertEquals(src.m33, 1.0, 0.001, "[3][3] not set to 1");
    }
}
