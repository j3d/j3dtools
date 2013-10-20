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

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the SVD util class.
 *
 * @author justin
 */
public class SingularValueDecompositionTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        SingularValueDecomposition classUnderTest = new SingularValueDecomposition();

        assertEquals(classUnderTest.rank(), 0, "Incorrect rank");
        assertEquals(classUnderTest.norm2(), 0.0, "Incorrect normal");

        // Internal divide by zero because S is 0, so should generate an NaN
        assertTrue(Double.isNaN(classUnderTest.cond()), "Incorrect conditional");
        assertTrue(classUnderTest.isGeneratingLeftVectors(), "Not generating left vectors");
        assertTrue(classUnderTest.isGeneratingRightVectors(), "Not generating right vectors");
    }

    @Test(groups = "unit")
    public void testIdentityDecomposition() throws Exception
    {
        double[][] testMatrix = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1}};

        SingularValueDecomposition classUnderTest = new SingularValueDecomposition();
        classUnderTest.decompose(testMatrix);

        assertEquals(classUnderTest.rank(), 3, "Incorrect rank 2D matrix");
        assertEquals(classUnderTest.norm2(), 1.0, "Incorrect normal 2D matrix");
        assertEquals(classUnderTest.cond(), 1.0, "Incorrect conditional 2D matrix");

        double[] resultS = new double[3];
        classUnderTest.getSingularValues(resultS);

        assertEquals(resultS[0], 1.0, "First scale value wrong");
        assertEquals(resultS[1], 1.0, "Second scale value wrong");
        assertEquals(resultS[2], 1.0, "Third scale value wrong");

        double[] testMatrix2 = { 1, 0, 0, 0, 1, 0, 0, 0, 1};
        classUnderTest.decompose(testMatrix2);

        assertEquals(classUnderTest.rank(), 3, "Incorrect rank 1D array");
        assertEquals(classUnderTest.norm2(), 1.0, "Incorrect normal 1D array");
        assertEquals(classUnderTest.cond(), 1.0, "Incorrect conditional 1D array");
    }

    @Test(groups = "unit")
    public void testScaleOnlyDecomposition() throws Exception
    {
        final double TEST_SCALE_1 = 2.5;
        final double TEST_SCALE_2 = 0.5;
        final double TEST_SCALE_3 = 1.4;

        double[] testMatrix = { TEST_SCALE_1, 0, 0, 0, TEST_SCALE_2, 0, 0, 0, TEST_SCALE_3};

        SingularValueDecomposition classUnderTest = new SingularValueDecomposition();
        classUnderTest.decompose(testMatrix);

        double[] resultS = new double[3];
        classUnderTest.getSingularValues(resultS);

        // Remember that S is ordered by eigenvector size, so SCALE_3 appears before SCALE_2
        assertEquals(resultS[0], TEST_SCALE_1, "First scale value wrong");
        assertEquals(resultS[1], TEST_SCALE_3, "Second scale value wrong");
        assertEquals(resultS[2], TEST_SCALE_2, "Third scale value wrong");

        double[] resultU = new double[9];
        classUnderTest.getLeftVectors(resultU);

        double[] expectedLeft = { -1, 0, 0,  0, 0, -1, 0, 1, 0 };
        for(int i = 0; i < resultU.length; i++)
            assertEquals(resultU[i], expectedLeft[i], 0.0001, "Left vector at index " + i + " incorrect");

        double[] resultV = new double[9];
        classUnderTest.getRightVectors(resultV);

        double[] expectedRight = { -1, 0, 0,  0, 0, -1, 0, 1, 0 };
        for(int i = 0; i < resultV.length; i++)
            assertEquals(resultV[i], expectedRight[i], 0.0001, "Right vector at index " + i + " incorrect");
    }

    @Test(groups = "unit")
    public void testRotationOnlyDecomposition() throws Exception
    {
        final double TEST_ANGLE = Math.PI * 0.5;
        final double SIN_ANGLE = Math.sin(TEST_ANGLE);
        final double COS_ANGLE = Math.cos(TEST_ANGLE);

        double[] testXRotationMatrix = { 1, 0, 0, 0, COS_ANGLE, -SIN_ANGLE, 0, SIN_ANGLE, COS_ANGLE };
        double[] testYRotationMatrix = { COS_ANGLE, 0, SIN_ANGLE, 0, 1, 0, -SIN_ANGLE, 0, COS_ANGLE };
        double[] testZRotationMatrix = { COS_ANGLE, -SIN_ANGLE, 0, SIN_ANGLE, 0, COS_ANGLE, 0, 0, 1 };

        SingularValueDecomposition classUnderTest = new SingularValueDecomposition();
        classUnderTest.decompose(testXRotationMatrix);

        double[] resultS = new double[3];
        classUnderTest.getSingularValues(resultS);

        assertEquals(resultS[0], 1.0, "X Rotation First scale not identity");
        assertEquals(resultS[1], 1.0, "X Rotation Second scale not identity");
        assertEquals(resultS[2], 1.0, "X Rotation Third scale not identity");

        classUnderTest.decompose(testYRotationMatrix);
        classUnderTest.getSingularValues(resultS);

        assertEquals(resultS[0], 1.0, "Y Rotation First scale not identity");
        assertEquals(resultS[1], 1.0, "Y Rotation Second scale not identity");
        assertEquals(resultS[2], 1.0, "Y Rotation Third scale not identity");

        classUnderTest.decompose(testZRotationMatrix);
        classUnderTest.getSingularValues(resultS);

        assertEquals(resultS[0], 1.0, "Z Rotation First scale not identity");
        assertEquals(resultS[1], 1.0, "Z Rotation Second scale not identity");
        assertEquals(resultS[2], 1.0, "Z Rotation Third scale not identity");
    }

    @Test(groups = "unit")
    public void testScaleRotateDecomposition() throws Exception
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

        double[] testXRotScaleMatrix = new double[16];
        double[] testYRotScaleMatrix = new double[16];
        double[] testZRotScaleMatrix = new double[16];

        multMatrix(testXRotationMatrix, testScaleMatrix, testXRotScaleMatrix);
        multMatrix(testYRotationMatrix, testScaleMatrix, testYRotScaleMatrix);
        multMatrix(testZRotationMatrix, testScaleMatrix, testZRotScaleMatrix);

        SingularValueDecomposition classUnderTest = new SingularValueDecomposition();
        classUnderTest.decompose(testXRotScaleMatrix);

        double[] resultS = new double[3];
        classUnderTest.getSingularValues(resultS);

        assertEquals(resultS[0], TEST_SCALE_1, "X Rotation First scale not identity");
        assertEquals(resultS[1], TEST_SCALE_3, "X Rotation Second scale not identity");
        assertEquals(resultS[2], TEST_SCALE_2, "X Rotation Third scale not identity");

        classUnderTest.decompose(testYRotScaleMatrix);
        classUnderTest.getSingularValues(resultS);

        assertEquals(resultS[0], TEST_SCALE_1, "Y Rotation First scale not identity");
        assertEquals(resultS[1], TEST_SCALE_3, "Y Rotation Second scale not identity");
        assertEquals(resultS[2], TEST_SCALE_2, "Y Rotation Third scale not identity");

        classUnderTest.decompose(testZRotScaleMatrix);
        classUnderTest.getSingularValues(resultS);

        assertEquals(resultS[0], TEST_SCALE_1, "Z Rotation First scale not identity");
        assertEquals(resultS[1], TEST_SCALE_3, "Z Rotation Second scale not identity");
        assertEquals(resultS[2], TEST_SCALE_2, "Z Rotation Third scale not identity");
    }

    @Test(groups = "unit", dependsOnMethods = "testScaleOnlyDecomposition")
    public void testGenerateFlags() throws Exception
    {
        final double TEST_SCALE_1 = 2.5;
        final double TEST_SCALE_2 = 0.5;
        final double TEST_SCALE_3 = 1.4;

        double[] testScaleMatrix = { TEST_SCALE_1, 0, 0, 0, TEST_SCALE_2, 0, 0, 0, TEST_SCALE_3};

        // Normal expectations if the flag is set.
        double[] expectedLeft = { -1, 0, 0,  0, 0, -1, 0, 1, 0 };
        double[] expectedRight = { -1, 0, 0,  0, 0, -1, 0, 1, 0 };


        SingularValueDecomposition classUnderTest = new SingularValueDecomposition();
        classUnderTest.generateRightVectors(false);
        classUnderTest.decompose(testScaleMatrix);

        double[] resultS = new double[3];
        classUnderTest.getSingularValues(resultS);

        assertEquals(resultS[0], TEST_SCALE_1, "X Rotation First scale not identity after ignoring right");
        assertEquals(resultS[1], TEST_SCALE_3, "X Rotation Second scale not identity after ignoring right");
        assertEquals(resultS[2], TEST_SCALE_2, "X Rotation Third scale not identity after ignoring right");

        double[] resultU = new double[9];
        classUnderTest.getLeftVectors(resultU);

        for(int i = 0; i < resultU.length; i++)
            assertEquals(resultU[i], expectedLeft[i], 0.0001, "Left vector at index " + i + " incorrect");

        double[] resultV = new double[9];
        classUnderTest.getRightVectors(resultV);

        for(int i = 0; i < resultV.length; i++)
            assertEquals(resultV[i], 0.0, 0.0001, "Right vector at index " + i + " not ignored");

        // Now turn the flags around and just test for right vectors
        classUnderTest.generateLeftVectors(false);
        classUnderTest.generateRightVectors(true);
        classUnderTest.decompose(testScaleMatrix);

        classUnderTest.getSingularValues(resultS);

        assertEquals(resultS[0], TEST_SCALE_1, "X Rotation First scale not identity after ignoring left");
        assertEquals(resultS[1], TEST_SCALE_3, "X Rotation Second scale not identity after ignoring left");
        assertEquals(resultS[2], TEST_SCALE_2, "X Rotation Third scale not identity after ignoring left");

        classUnderTest.getLeftVectors(resultU);

        for(int i = 0; i < resultU.length; i++)
            assertEquals(resultU[i], 0.0, 0.0001, "Left vector at index " + i + " not ignored");

        classUnderTest.getRightVectors(resultV);

        for(int i = 0; i < resultV.length; i++)
            assertEquals(resultV[i], expectedRight[i], 0.0001, "Right vector at index " + i + " incorrect");
    }

    /**
     * Convenience method to multiply m1 x m2 and put the result in the output matrix
     *
     * @param m1
     * @param m2
     * @param out
     */
    private void multMatrix(double[] m1, double[] m2, double[] out)
    {
        // |0 1 2|     |0 1 2|
        // |3 4 5|  x  |3 4 5|
        // |6 7 8|     |6 7 8|

        out[0] = m1[0] * m2[0] + m1[1] * m2[3] + m1[2] * m2[6];
        out[1] = m1[0] * m2[1] + m1[1] * m2[4] + m1[2] * m2[7];
        out[2] = m1[0] * m2[2] + m1[1] * m2[5] + m1[2] * m2[8];

        out[3] = m1[3] * m2[0] + m1[4] * m2[3] + m1[5] * m2[6];
        out[4] = m1[3] * m2[1] + m1[4] * m2[4] + m1[5] * m2[7];
        out[5] = m1[3] * m2[2] + m1[4] * m2[5] + m1[5] * m2[8];

        out[6] = m1[6] * m2[0] + m1[7] * m2[3] + m1[8] * m2[6];
        out[7] = m1[6] * m2[1] + m1[7] * m2[4] + m1[8] * m2[7];
        out[8] = m1[6] * m2[2] + m1[7] * m2[5] + m1[8] * m2[8];
    }
}
