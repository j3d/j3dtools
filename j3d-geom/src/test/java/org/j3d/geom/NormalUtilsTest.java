/*
 * j3d.org Copyright (c) 2001-2015
 *                                 Java Source
 *
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read docs/LGPL.txt for more information
 *
 *  This software comes with the standard NO WARRANTY disclaimer for any
 *  purpose. Use it at your own risk. If there's a problem you get to fix it.
 */

package org.j3d.geom;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class NormalUtilsTest
{

    @Test(groups = "unit")
    public void testGetSharedInstance() throws Exception
    {
        NormalUtils testInstance = NormalUtils.getSharedInstance();

        assertNotNull(testInstance, "Did not create the initial shared instance");

        assertSame(NormalUtils.getSharedInstance(), testInstance, "Wasn't a proper shared instance");
    }

    @Test(groups = "unit")
    public void test1DNegationInPlace() throws Exception
    {
        // not a correct, unit length normal, but good enough for this test
        float[] testNormal = { 1.0f, 1.0f, 1.0f };

        NormalUtils classUnderTest = new NormalUtils();
        classUnderTest.negate(testNormal, 1);

        assertEquals(testNormal[0], -1.0f, "Didn't negate x component");
        assertEquals(testNormal[1], -1.0f, "Didn't negate y component");
        assertEquals(testNormal[2], -1.0f, "Didn't negate z component");
    }

    @Test(groups = "unit")
    public void test1DPartialNegationInPlace() throws Exception
    {
        // not a correct, unit length normal, but good enough for this test
        float[] testNormal = { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f };

        NormalUtils classUnderTest = new NormalUtils();
        classUnderTest.negate(testNormal, 1);

        assertEquals(testNormal[0], -1.0f, "Didn't negate x component");
        assertEquals(testNormal[1], -1.0f, "Didn't negate y component");
        assertEquals(testNormal[2], -1.0f, "Didn't negate z component");

        assertEquals(testNormal[3], 1.0f, "Should not have negated second normal x");
        assertEquals(testNormal[4], 1.0f, "Should not have negated second normal y");
        assertEquals(testNormal[5], 1.0f, "Should not have negated second normal z");
    }

    @Test(groups = "unit")
    public void test2DNegationInPlace() throws Exception
    {
        // not a correct, unit length normal, but good enough for this test
        float[][] testNormal = { { 1.0f, 1.0f, 1.0f } };

        NormalUtils classUnderTest = new NormalUtils();
        classUnderTest.negate(testNormal, 1);

        assertEquals(testNormal[0][0], -1.0f, "Didn't negate x component");
        assertEquals(testNormal[0][1], -1.0f, "Didn't negate y component");
        assertEquals(testNormal[0][2], -1.0f, "Didn't negate z component");
    }

    @Test(groups = "unit")
    public void test2DPartialNegationInPlace() throws Exception
    {
        // not a correct, unit length normal, but good enough for this test.
        // Also make [0] a bit longer and make sure it doesn't take additional
        // indices
        float[][] testNormal = { { 1.0f, 1.0f, 1.0f, 2.0f } , { 1.0f, 1.0f, 1.0f } };

        NormalUtils classUnderTest = new NormalUtils();
        classUnderTest.negate(testNormal, 1);

        assertEquals(testNormal[0][0], -1.0f, "Didn't negate x component");
        assertEquals(testNormal[0][1], -1.0f, "Didn't negate y component");
        assertEquals(testNormal[0][2], -1.0f, "Didn't negate z component");
        assertEquals(testNormal[0][3], 2.0f, "Should not have negated more than 3 components");

        assertEquals(testNormal[1][0], 1.0f, "Should not have negated second normal x");
        assertEquals(testNormal[1][1], 1.0f, "Should not have negated second normal y");
        assertEquals(testNormal[1][2], 1.0f, "Should not have negated second normal z");
    }

    @Test(groups = "unit")
    public void test1DNegation() throws Exception
    {
        // not a correct, unit length normal, but good enough for this test
        float[] testNormal = { 1.0f, 1.0f, 1.0f };
        float[] resultNormals = new float[testNormal.length];

        NormalUtils classUnderTest = new NormalUtils();
        classUnderTest.negate(testNormal, 1, resultNormals);

        assertEquals(resultNormals[0], -testNormal[0], "Didn't negate x component");
        assertEquals(resultNormals[1], -testNormal[1], "Didn't negate y component");
        assertEquals(resultNormals[2], -testNormal[2], "Didn't negate z component");
    }

    @Test(groups = "unit")
    public void test1DPartialNegation() throws Exception
    {
        // not a correct, unit length normal, but good enough for this test
        float[] testNormal = { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f };
        float[] resultNormals = new float[testNormal.length];

        NormalUtils classUnderTest = new NormalUtils();
        classUnderTest.negate(testNormal, 1, resultNormals);

        assertEquals(resultNormals[0], -testNormal[0], "Didn't negate x component");
        assertEquals(resultNormals[1], -testNormal[1], "Didn't negate y component");
        assertEquals(resultNormals[2], -testNormal[2], "Didn't negate z component");

        assertEquals(resultNormals[3], 0.0f, "Should not have copied the second normal x");
        assertEquals(resultNormals[4], 0.0f, "Should not have copied the second normal y");
        assertEquals(resultNormals[5], 0.0f, "Should not have copied the second normal z");
    }

    @Test(groups = "unit")
    public void test2DNegation() throws Exception
    {
        // not a correct, unit length normal, but good enough for this test
        float[][] testNormal = { { 1.0f, 1.0f, 1.0f } };
        float[][] resultNormals = new float[1][3];

        NormalUtils classUnderTest = new NormalUtils();
        classUnderTest.negate(testNormal, 1, resultNormals);

        assertEquals(resultNormals[0][0], -testNormal[0][0], "Didn't negate x component");
        assertEquals(resultNormals[0][1], -testNormal[0][1], "Didn't negate y component");
        assertEquals(resultNormals[0][2], -testNormal[0][2], "Didn't negate z component");
    }

    @Test(groups = "unit")
    public void test2DPartialNegation() throws Exception
    {
        // not a correct, unit length normal, but good enough for this test.
        // Also make [0] a bit longer and make sure it doesn't take additional
        // indices
        float[][] testNormal = { { 1.0f, 1.0f, 1.0f, 2.0f } , { 1.0f, 1.0f, 1.0f } };
        float[][] resultNormals = new float[2][4];

        NormalUtils classUnderTest = new NormalUtils();
        classUnderTest.negate(testNormal, 1, resultNormals);

        assertEquals(resultNormals[0][0], -testNormal[0][0], "Didn't negate x component");
        assertEquals(resultNormals[0][1], -testNormal[0][1], "Didn't negate y component");
        assertEquals(resultNormals[0][2], -testNormal[0][2], "Didn't negate z component");
        assertEquals(resultNormals[0][3], 0.0f, "Should not have copied the first normal extra data");

        assertEquals(resultNormals[1][0], 0.0f, "Should not have copied the second normal x");
        assertEquals(resultNormals[1][1], 0.0f, "Should not have copied the second normal y");
        assertEquals(resultNormals[1][2], 0.0f, "Should not have copied the second normal z");
    }

    @Test(groups = "unit", dataProvider = "face normal")
    public void test1DFaceNormalGeneration(float[] p1, float[] p2, float[] p3, float[] expected) throws Exception
    {
        float[] pointData = { p1[0], p1[1], p1[2], p2[0], p2[1], p2[2], p3[0], p3[1], p3[2] };
        float[] resultNormal = new float[3];

        NormalUtils classUnderTest = new NormalUtils();
        classUnderTest.createFaceNormal(pointData, 0, 3, 6, resultNormal, 0);

        assertEquals(resultNormal[0], expected[0], 0.001f, "X component of normal not correct");
        assertEquals(resultNormal[1], expected[1], 0.001f, "Y component of normal not correct");
        assertEquals(resultNormal[2], expected[2], 0.001f, "Z component of normal not correct");
    }

    @Test(groups = "unit", dataProvider = "face normal")
    public void test2DFaceNormalGeneration(float[] p1, float[] p2, float[] p3, float[] expected) throws Exception
    {
        float[][] pointData = { p1, p2, p3 };
        float[][] resultNormal = new float[1][3];

        NormalUtils classUnderTest = new NormalUtils();
        classUnderTest.createFaceNormal(pointData, 0, 1, 2, resultNormal, 0);

        assertEquals(resultNormal[0][0], expected[0], 0.001f, "X component of normal not correct");
        assertEquals(resultNormal[0][1], expected[1], 0.001f, "Y component of normal not correct");
        assertEquals(resultNormal[0][2], expected[2], 0.001f, "Z component of normal not correct");
    }

    @Test(groups = "unit", dataProvider = "radial normal")
    public void test1DRadialNormalGeneration(float[] pointData, float[] expected) throws Exception
    {
        float[] resultNormal = new float[3];

        NormalUtils classUnderTest = new NormalUtils();
        classUnderTest.createRadialNormal(pointData, 0, resultNormal, 0);

        assertEquals(resultNormal[0], expected[0], 0.001f, "X component of normal not correct");
        assertEquals(resultNormal[1], expected[1], 0.001f, "Y component of normal not correct");
        assertEquals(resultNormal[2], expected[2], 0.001f, "Z component of normal not correct");
    }

    @Test(groups = "unit", dataProvider = "radial normal")
    public void test2DRadialNormalGeneration(float[] sourcePoint, float[] expected) throws Exception
    {
        float[][] pointData = { sourcePoint };
        float[][] resultNormal = new float[1][3];

        NormalUtils classUnderTest = new NormalUtils();
        classUnderTest.createRadialNormal(pointData, 0, resultNormal, 0);

        assertEquals(resultNormal[0][0], expected[0], 0.001f, "X component of normal not correct");
        assertEquals(resultNormal[0][1], expected[1], 0.001f, "Y component of normal not correct");
        assertEquals(resultNormal[0][2], expected[2], 0.001f, "Z component of normal not correct");
    }

    @Test(groups = "unit", dataProvider = "offset radial normal")
    public void test1DOffsetRadialNormalGeneration(float[] point, float[] origin, float[] expected) throws Exception
    {
        float[] resultNormal = new float[3];

        NormalUtils classUnderTest = new NormalUtils();
        classUnderTest.createRadialNormal(point, 0, origin, 0, resultNormal, 0);

        assertEquals(resultNormal[0], expected[0], 0.001f, "X component of normal not correct");
        assertEquals(resultNormal[1], expected[1], 0.001f, "Y component of normal not correct");
        assertEquals(resultNormal[2], expected[2], 0.001f, "Z component of normal not correct");
    }

    @Test(groups = "unit", dataProvider = "offset radial normal")
    public void test2DOffsetRadialNormalGeneration(float[] point, float[] origin, float[] expected) throws Exception
    {
        float[][] pointData = { point };
        float[][] originData = { origin };
        float[][] resultNormal = new float[1][3];

        NormalUtils classUnderTest = new NormalUtils();
        classUnderTest.createRadialNormal(pointData, 0, originData, 0, resultNormal, 0);

        assertEquals(resultNormal[0][0], expected[0], 0.001f, "X component of normal not correct");
        assertEquals(resultNormal[0][1], expected[1], 0.001f, "Y component of normal not correct");
        assertEquals(resultNormal[0][2], expected[2], 0.001f, "Z component of normal not correct");
    }

    @DataProvider(name = "face normal")
    public Object[][] generateFaceNormalData() {
        Object[][] retval =  new Object[5][4];

        retval[0][0] = new float[] { 0, 0, 0 };
        retval[0][1] = new float[] { 1, 0, 0 };
        retval[0][2] = new float[] { 0, 1, 0 };
        retval[0][3] = new float[] { 0, 0, -1 };

        retval[1][0] = new float[] { 0, 0, 0 };
        retval[1][1] = new float[] { 0, 1, 0 };
        retval[1][2] = new float[] { 1, 0, 0 };
        retval[1][3] = new float[] { 0, 0, 1 };

        retval[2][0] = new float[] { 0, 0, 0 };
        retval[2][1] = new float[] { -1, 0, 0 };
        retval[2][2] = new float[] { 0, 1, 0 };
        retval[2][3] = new float[] { 0, 0, 1 };

        retval[3][0] = new float[] { 0, 0, 0 };
        retval[3][1] = new float[] { 0, -1, 0 };
        retval[3][2] = new float[] { 1, 0, 0 };
        retval[3][3] = new float[] { 0, 0, -1 };

        retval[4][0] = new float[] { 0, 0, 0 };
        retval[4][1] = new float[] { 0, 1, 1 };
        retval[4][2] = new float[] { 1, 0, 0 };
        retval[4][3] = new float[] { 0, 0.7071f, 0.7071f };

        return retval;
    }

    @DataProvider(name = "radial normal")
    public Object[][] generateRadialNormalData() {
        Object[][] retval =  new Object[3][2];

        retval[0][0] = new float[] { 1, 0, 0 };
        retval[0][1] = new float[] { 1, 0, 0 };

        retval[1][0] = new float[] { 0, -1, 0 };
        retval[1][1] = new float[] { 0, -1, 0 };

        retval[2][0] = new float[] { 0, 1, 1 };
        retval[2][1] = new float[] { 0, 0.7071f, 0.7071f };

        return retval;
    }

    @DataProvider(name = "offset radial normal")
    public Object[][] generateOffsetRadialNormalData() {
        Object[][] retval =  new Object[4][3];

        retval[0][0] = new float[] { 1, 0, 0 };
        retval[0][1] = new float[] { 0, 0, 0 };
        retval[0][2] = new float[] { 1, 0, 0 };

        retval[1][0] = new float[] { 2, 0, 0 };
        retval[1][1] = new float[] { 1, 0, 0 };
        retval[1][2] = new float[] { 1, 0, 0 };

        retval[2][0] = new float[] { 0, -1, 0 };
        retval[2][1] = new float[] { 1,  -1, 0 };
        retval[2][2] = new float[] { -1, 0, 0 };

        retval[3][0] = new float[] { 0, 2, 2 };
        retval[3][1] = new float[] { 0, 1, 1 };
        retval[3][2] = new float[] { 0, 0.7071f, 0.7071f };

        return retval;
    }
}
