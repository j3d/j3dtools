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

    @Test(groups = "unit")
    public void testProjectionMatrix() throws Exception
    {
        final double LEFT = -2;
        final double RIGHT = 2;
        final double TOP = 2;
        final double BOTTOM = -2;
        final double NEAR = -0.1;
        final double FAR = -100;

        Matrix4d result = new Matrix4d();

        MatrixUtils classUnderTest = new MatrixUtils();
        classUnderTest.generateProjectionMatrix(LEFT, RIGHT, TOP, BOTTOM, NEAR, FAR, result);

        assertEquals(result.m00, (2 * NEAR) / (RIGHT - LEFT), 0.001, "Invalid 0,0 calc");
        assertEquals(result.m01, 0.0, "Invalid 0,1 calc");
        assertEquals(result.m02, 0.0, "Invalid 0,2 calc");
        assertEquals(result.m03, 0.0, "Invalid 0,3 calc");

        assertEquals(result.m10, 0.0, "Invalid 1,0 calc");
        assertEquals(result.m11, (2 * NEAR) / (TOP - BOTTOM), 0.001, "Invalid 1,1 calc");
        assertEquals(result.m12, 0.0, "Invalid 1,2 calc");
        assertEquals(result.m13, 0.0, "Invalid 1,3 calc");

        assertEquals(result.m20, (RIGHT + LEFT) / (RIGHT - LEFT), 0.001, "Invalid 2,0 calc");
        assertEquals(result.m21, (TOP + BOTTOM) / (TOP - BOTTOM), 0.001, "Invalid 2,1 calc");
        assertEquals(result.m22, -(FAR + NEAR) / (FAR - NEAR), 0.001, "Invalid 2,2 calc");
        assertEquals(result.m23, -1.0, "Invalid 2,3 calc");

        assertEquals(result.m30, 0.0, "Invalid 3,0 calc");
        assertEquals(result.m31, 0.0, "Invalid 3,1 calc");
        assertEquals(result.m32, (-2 * FAR * NEAR) / (FAR - NEAR), 0.001, "Invalid 3,2 calc");
        assertEquals(result.m33, 0.0, "Invalid 3,3 calc");
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
    public void testRotateXIdentity() throws Exception
    {
        Matrix4d testMatrix = new Matrix4d();
        testMatrix.setIdentity();

        MatrixUtils classUnderTest = new MatrixUtils();
        classUnderTest.rotateX(0, testMatrix);

        checkIsIdentityMatrix(testMatrix);

        classUnderTest.rotateX(Math.PI * 0.5, testMatrix);


        // Should be
        // 1 0 0 0
        // 0 0 -1 0
        // 0 1 0 0
        // 0 0 0 1
        assertEquals(testMatrix.m00, 1.0, 0.001, "[0][0] not set to 1");
        assertEquals(testMatrix.m01, 0.0, 0.001, "[0][1] not set to 0");
        assertEquals(testMatrix.m02, 0.0, 0.001, "[0][2] not set to 0");
        assertEquals(testMatrix.m03, 0.0, 0.001, "[0][3] not set to 0");

        assertEquals(testMatrix.m10, 0.0, 0.001, "[1][0] not set to 0");
        assertEquals(testMatrix.m11, 0.0, 0.001, "[1][1] not set to 0");
        assertEquals(testMatrix.m12, -1.0, 0.001, "[1][2] not set to -1");
        assertEquals(testMatrix.m13, 0.0, 0.001, "[1][3] not set to 0");

        assertEquals(testMatrix.m20, 0.0, 0.001, "[2][0] not set to 0");
        assertEquals(testMatrix.m21, 1.0, 0.001, "[2][1] not set to 1");
        assertEquals(testMatrix.m22, 0.0, 0.001, "[2][2] not set to 0");
        assertEquals(testMatrix.m23, 0.0, 0.001, "[2][3] not set to 0");

        assertEquals(testMatrix.m30, 0.0, 0.001, "[3][0] not set to 0");
        assertEquals(testMatrix.m31, 0.0, 0.001, "[3][1] not set to 0");
        assertEquals(testMatrix.m32, 0.0, 0.001, "[3][2] not set to 0");
        assertEquals(testMatrix.m33, 1.0, 0.001, "[3][3] not set to 1");

        classUnderTest.rotateX(Math.PI * -0.5, testMatrix);

        // Should be
        // 1 0 0 0
        // 0 0 1 0
        // 0 -1 0 0
        // 0 0 0 1
        assertEquals(testMatrix.m00, 1.0, 0.001, "[0][0] not set to 1");
        assertEquals(testMatrix.m01, 0.0, 0.001, "[0][1] not set to 0");
        assertEquals(testMatrix.m02, 0.0, 0.001, "[0][2] not set to 0");
        assertEquals(testMatrix.m03, 0.0, 0.001, "[0][3] not set to 0");

        assertEquals(testMatrix.m10, 0.0, 0.001, "[1][0] not set to 0");
        assertEquals(testMatrix.m11, 0.0, 0.001, "[1][1] not set to 0");
        assertEquals(testMatrix.m12, 1.0, 0.001, "[1][2] not set to 1");
        assertEquals(testMatrix.m13, 0.0, 0.001, "[1][3] not set to 0");

        assertEquals(testMatrix.m20, 0.0, 0.001, "[2][0] not set to 0");
        assertEquals(testMatrix.m21, -1.0, 0.001, "[2][1] not set to -1");
        assertEquals(testMatrix.m22, 0.0, 0.001, "[2][2] not set to 0");
        assertEquals(testMatrix.m23, 0.0, 0.001, "[2][3] not set to 0");

        assertEquals(testMatrix.m30, 0.0, 0.001, "[3][0] not set to 0");
        assertEquals(testMatrix.m31, 0.0, 0.001, "[3][1] not set to 0");
        assertEquals(testMatrix.m32, 0.0, 0.001, "[3][2] not set to 0");
        assertEquals(testMatrix.m33, 1.0, 0.001, "[3][3] not set to 1");
    }

    @Test(groups = "unit")
    public void testRotateYIdentity() throws Exception
    {
        Matrix4d testMatrix = new Matrix4d();
        testMatrix.setIdentity();

        MatrixUtils classUnderTest = new MatrixUtils();
        classUnderTest.rotateY(0, testMatrix);

        checkIsIdentityMatrix(testMatrix);

        classUnderTest.rotateY(Math.PI * 0.5, testMatrix);


        // Should be
        //  0  0  1  0
        //  0  1  0  0
        // -1  0  0  0
        //  0  0  0  1
        assertEquals(testMatrix.m00, 0.0, 0.001, "[0][0] not set to 0");
        assertEquals(testMatrix.m01, 0.0, 0.001, "[0][1] not set to 0");
        assertEquals(testMatrix.m02, 1.0, 0.001, "[0][2] not set to 1");
        assertEquals(testMatrix.m03, 0.0, 0.001, "[0][3] not set to 0");

        assertEquals(testMatrix.m10, 0.0, 0.001, "[1][0] not set to 0");
        assertEquals(testMatrix.m11, 1.0, 0.001, "[1][1] not set to 1");
        assertEquals(testMatrix.m12, 0.0, 0.001, "[1][2] not set to 0");
        assertEquals(testMatrix.m13, 0.0, 0.001, "[1][3] not set to 0");

        assertEquals(testMatrix.m20, -1.0, 0.001, "[2][0] not set to -1");
        assertEquals(testMatrix.m21, 0.0, 0.001, "[2][1] not set to 0");
        assertEquals(testMatrix.m22, 0.0, 0.001, "[2][2] not set to 0");
        assertEquals(testMatrix.m23, 0.0, 0.001, "[2][3] not set to 0");

        assertEquals(testMatrix.m30, 0.0, 0.001, "[3][0] not set to 0");
        assertEquals(testMatrix.m31, 0.0, 0.001, "[3][1] not set to 0");
        assertEquals(testMatrix.m32, 0.0, 0.001, "[3][2] not set to 0");
        assertEquals(testMatrix.m33, 1.0, 0.001, "[3][3] not set to 1");

        classUnderTest.rotateY(Math.PI * -0.5, testMatrix);

        //  0  0 -1  0
        //  0  1  0  0
        //  1  0  0  0
        //  0  0  0  1
        assertEquals(testMatrix.m00, 0.0, 0.001, "[0][0] not set to 0");
        assertEquals(testMatrix.m01, 0.0, 0.001, "[0][1] not set to 0");
        assertEquals(testMatrix.m02, -1.0, 0.001, "[0][2] not set to -1");
        assertEquals(testMatrix.m03, 0.0, 0.001, "[0][3] not set to 0");

        assertEquals(testMatrix.m10, 0.0, 0.001, "[1][0] not set to 0");
        assertEquals(testMatrix.m11, 1.0, 0.001, "[1][1] not set to 1");
        assertEquals(testMatrix.m12, 0.0, 0.001, "[1][2] not set to 0");
        assertEquals(testMatrix.m13, 0.0, 0.001, "[1][3] not set to 0");

        assertEquals(testMatrix.m20, 1.0, 0.001, "[2][0] not set to 1");
        assertEquals(testMatrix.m21, 0.0, 0.001, "[2][1] not set to 0");
        assertEquals(testMatrix.m22, 0.0, 0.001, "[2][2] not set to 0");
        assertEquals(testMatrix.m23, 0.0, 0.001, "[2][3] not set to 0");

        assertEquals(testMatrix.m30, 0.0, 0.001, "[3][0] not set to 0");
        assertEquals(testMatrix.m31, 0.0, 0.001, "[3][1] not set to 0");
        assertEquals(testMatrix.m32, 0.0, 0.001, "[3][2] not set to 0");
        assertEquals(testMatrix.m33, 1.0, 0.001, "[3][3] not set to 1");
    }

    @Test(groups = "unit")
    public void testRotateZIdentity() throws Exception
    {
        Matrix4d testMatrix = new Matrix4d();
        testMatrix.setIdentity();

        MatrixUtils classUnderTest = new MatrixUtils();
        classUnderTest.rotateZ(0, testMatrix);

        checkIsIdentityMatrix(testMatrix);

        classUnderTest.rotateZ(Math.PI * 0.5, testMatrix);


        // Should be
        //  0 -1  0  0
        //  1  0  0  0
        //  0  0  1  0
        //  0  0  0  1
        assertEquals(testMatrix.m00, 0.0, 0.001, "[0][0] not set to 0");
        assertEquals(testMatrix.m01, -1.0, 0.001, "[0][1] not set to -1");
        assertEquals(testMatrix.m02, 0.0, 0.001, "[0][2] not set to 0");
        assertEquals(testMatrix.m03, 0.0, 0.001, "[0][3] not set to 0");

        assertEquals(testMatrix.m10, 1.0, 0.001, "[1][0] not set to 1");
        assertEquals(testMatrix.m11, 0.0, 0.001, "[1][1] not set to 0");
        assertEquals(testMatrix.m12, 0.0, 0.001, "[1][2] not set to 0");
        assertEquals(testMatrix.m13, 0.0, 0.001, "[1][3] not set to 0");

        assertEquals(testMatrix.m20, 0.0, 0.001, "[2][0] not set to 0");
        assertEquals(testMatrix.m21, 0.0, 0.001, "[2][1] not set to 0");
        assertEquals(testMatrix.m22, 1.0, 0.001, "[2][2] not set to 1");
        assertEquals(testMatrix.m23, 0.0, 0.001, "[2][3] not set to 0");

        assertEquals(testMatrix.m30, 0.0, 0.001, "[3][0] not set to 0");
        assertEquals(testMatrix.m31, 0.0, 0.001, "[3][1] not set to 0");
        assertEquals(testMatrix.m32, 0.0, 0.001, "[3][2] not set to 0");
        assertEquals(testMatrix.m33, 1.0, 0.001, "[3][3] not set to 1");

        classUnderTest.rotateZ(Math.PI * -0.5, testMatrix);

        //  0  1  0  0
        // -1  0  0  0
        //  0  0  0  0
        //  0  0  0  1
        assertEquals(testMatrix.m00, 0.0, 0.001, "[0][0] not set to 0");
        assertEquals(testMatrix.m01, 1.0, 0.001, "[0][1] not set to 1");
        assertEquals(testMatrix.m02, 0.0, 0.001, "[0][2] not set to 0");
        assertEquals(testMatrix.m03, 0.0, 0.001, "[0][3] not set to 0");

        assertEquals(testMatrix.m10, -1.0, 0.001, "[1][0] not set to -1");
        assertEquals(testMatrix.m11, 0.0, 0.001, "[1][1] not set to 0");
        assertEquals(testMatrix.m12, 0.0, 0.001, "[1][2] not set to 0");
        assertEquals(testMatrix.m13, 0.0, 0.001, "[1][3] not set to 0");

        assertEquals(testMatrix.m20, 0.0, 0.001, "[2][0] not set to 0");
        assertEquals(testMatrix.m21, 0.0, 0.001, "[2][1] not set to 0");
        assertEquals(testMatrix.m22, 1.0, 0.001, "[2][2] not set to 1");
        assertEquals(testMatrix.m23, 0.0, 0.001, "[2][3] not set to 0");

        assertEquals(testMatrix.m30, 0.0, 0.001, "[3][0] not set to 0");
        assertEquals(testMatrix.m31, 0.0, 0.001, "[3][1] not set to 0");
        assertEquals(testMatrix.m32, 0.0, 0.001, "[3][2] not set to 0");
        assertEquals(testMatrix.m33, 1.0, 0.001, "[3][3] not set to 1");
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
