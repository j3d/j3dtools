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

    @Test(groups = "unit")
    public void testUniformScaleIdentityMatrix() throws Exception
    {
        Matrix4d testMatrix = new Matrix4d();
        testMatrix.setIdentity();

        MatrixUtils classUnderTest = new MatrixUtils();

        // This should give a scale factor of 1.
        assertEquals(classUnderTest.getUniformScale(testMatrix), 1.0, 0.0001, "Incorrect scale for identity matrix");
    }

    /**
     * Test the SVD decomposition of a matrix. Uses the same test as in the
     * lower level SVD class test, but wraps the matrix stuff around it
     */
    @Test(groups = "unit")
    public void testSVDDecomposition() throws Exception
    {
        final double TEST_ANGLE = Math.PI * 0.5;
        final double SIN_ANGLE = Math.sin(TEST_ANGLE);
        final double COS_ANGLE = Math.cos(TEST_ANGLE);
        final double TEST_SCALE_1 = 2.5;
        final double TEST_SCALE_2 = 0.5;
        final double TEST_SCALE_3 = 1.4;

        double[] testScaleMatrix = { TEST_SCALE_1, 0, 0, 0, TEST_SCALE_2, 0, 0, 0, TEST_SCALE_3};

        double[] testXRotationMatrix = { 1, 0, 0, 0, COS_ANGLE, -SIN_ANGLE, 0, SIN_ANGLE, COS_ANGLE };
        double[] testYRotationMatrix = { COS_ANGLE, 0, SIN_ANGLE, 0, 1, 0, -SIN_ANGLE, 0, COS_ANGLE };
        double[] testZRotationMatrix = { COS_ANGLE, -SIN_ANGLE, 0, SIN_ANGLE, 0, COS_ANGLE, 0, 0, 1 };

        Matrix4d testXRotScaleMatrix = new Matrix4d();
        Matrix4d testYRotScaleMatrix = new Matrix4d();
        Matrix4d testZRotScaleMatrix = new Matrix4d();

        multMatrix(testXRotationMatrix, testScaleMatrix, testXRotScaleMatrix);
        multMatrix(testYRotationMatrix, testScaleMatrix, testYRotScaleMatrix);
        multMatrix(testZRotationMatrix, testScaleMatrix, testZRotScaleMatrix);

        MatrixUtils classUnderTest = new MatrixUtils();
        double[] resultU = new double[9];
        double[] resultV = new double[9];
        double[] resultS = new double[3];

        classUnderTest.decomposeSVD(testXRotScaleMatrix, resultU, resultV, resultS);

        assertEquals(resultS[0], TEST_SCALE_1, "X Rotation First scale not identity");
        assertEquals(resultS[1], TEST_SCALE_3, "X Rotation Second scale not identity");
        assertEquals(resultS[2], TEST_SCALE_2, "X Rotation Third scale not identity");

        classUnderTest.decomposeSVD(testYRotScaleMatrix, resultU, resultV, resultS);

        assertEquals(resultS[0], TEST_SCALE_1, "Y Rotation First scale not identity");
        assertEquals(resultS[1], TEST_SCALE_3, "Y Rotation Second scale not identity");
        assertEquals(resultS[2], TEST_SCALE_2, "Y Rotation Third scale not identity");

        classUnderTest.decomposeSVD(testZRotScaleMatrix, resultU, resultV, resultS);

        assertEquals(resultS[0], TEST_SCALE_1, "Z Rotation First scale not identity");
        assertEquals(resultS[1], TEST_SCALE_3, "Z Rotation Second scale not identity");
        assertEquals(resultS[2], TEST_SCALE_2, "Z Rotation Third scale not identity");

        // Check the results are the same for the X matrix when we ignore the U and/or V results

        classUnderTest.decomposeSVD(testXRotScaleMatrix, resultU, null, resultS);

        assertEquals(resultS[0], TEST_SCALE_1, "V ignored First scale not identity");
        assertEquals(resultS[1], TEST_SCALE_3, "V ignored Second scale not identity");
        assertEquals(resultS[2], TEST_SCALE_2, "V ignored Third scale not identity");

        classUnderTest.decomposeSVD(testXRotScaleMatrix, null, resultV, resultS);

        assertEquals(resultS[0], TEST_SCALE_1, "U ignored First scale not identity");
        assertEquals(resultS[1], TEST_SCALE_3, "U ignored Second scale not identity");
        assertEquals(resultS[2], TEST_SCALE_2, "U ignored Third scale not identity");


        classUnderTest.decomposeSVD(testXRotScaleMatrix, null, null, resultS);

        assertEquals(resultS[0], TEST_SCALE_1, "U & V ignored First scale not identity");
        assertEquals(resultS[1], TEST_SCALE_3, "U & V ignored Second scale not identity");
        assertEquals(resultS[2], TEST_SCALE_2, "U & V ignored Third scale not identity");
    }

    /**
     * Test the SVD decomposition of a matrix. Uses the same test as in the
     * lower level SVD class test, but wraps the matrix stuff around it
     */
    @Test(groups = "unit")
    public void testGetUniformScale() throws Exception
    {
        final double TEST_ANGLE = Math.PI * 0.5;
        final double SIN_ANGLE = Math.sin(TEST_ANGLE);
        final double COS_ANGLE = Math.cos(TEST_ANGLE);
        final double TEST_SCALE_1 = 2.5;
        final double TEST_SCALE_2 = 0.5;
        final double TEST_SCALE_3 = 1.4;

        double[] testScaleMatrix = { TEST_SCALE_1, 0, 0, 0, TEST_SCALE_2, 0, 0, 0, TEST_SCALE_3};

        double[] testXRotationMatrix = { 1, 0, 0, 0, COS_ANGLE, -SIN_ANGLE, 0, SIN_ANGLE, COS_ANGLE };

        Matrix4d testXRotScaleMatrix = new Matrix4d();

        multMatrix(testXRotationMatrix, testScaleMatrix, testXRotScaleMatrix);

        MatrixUtils classUnderTest = new MatrixUtils();

        double result = classUnderTest.getUniformScale(testXRotScaleMatrix);

        assertEquals(result, TEST_SCALE_1, "Incorrect largest scale factor found");
    }

    @Test(groups = "unit")
    public void testRotateXIdentity() throws Exception {
        Matrix4d testMatrix = new Matrix4d();
        testMatrix.setIdentity();

        MatrixUtils classUnderTest = new MatrixUtils();
        classUnderTest.rotateX(0, testMatrix);

        checkIsIdentityMatrix(testMatrix);

        classUnderTest.rotateX(Math.PI * 0.5, testMatrix);

        checkIsIdentityMatrix(testMatrix);

        classUnderTest.rotateX(Math.PI * -0.5, testMatrix);

        checkIsIdentityMatrix(testMatrix);
    }

    /** Convenience method to check the matrix is an identity matrix, will allowing
     * for some error.
     *
     * @param src The matrix to test for identity
     */
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

    /**
     * Convenience method to multiply m1 x m2 and put the result in the output matrix
     *
     * @param m1 The left multiplicand
     * @param m2 The right multiplicand
     * @param out THe place to put the output matrix
     */
    private void multMatrix(double[] m1, double[] m2, Matrix4d out)
    {
        // |0 1 2|     |0 1 2|
        // |3 4 5|  x  |3 4 5|
        // |6 7 8|     |6 7 8|

        out.m00 = m1[0] * m2[0] + m1[1] * m2[3] + m1[2] * m2[6];
        out.m01 = m1[0] * m2[1] + m1[1] * m2[4] + m1[2] * m2[7];
        out.m02 = m1[0] * m2[2] + m1[1] * m2[5] + m1[2] * m2[8];

        out.m10 = m1[3] * m2[0] + m1[4] * m2[3] + m1[5] * m2[6];
        out.m11 = m1[3] * m2[1] + m1[4] * m2[4] + m1[5] * m2[7];
        out.m12 = m1[3] * m2[2] + m1[4] * m2[5] + m1[5] * m2[8];

        out.m20 = m1[6] * m2[0] + m1[7] * m2[3] + m1[8] * m2[6];
        out.m21 = m1[6] * m2[1] + m1[7] * m2[4] + m1[8] * m2[7];
        out.m22 = m1[6] * m2[2] + m1[7] * m2[5] + m1[8] * m2[8];
    }
}
