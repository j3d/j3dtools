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

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class CoordinateUtilsTest
{
    @Test(groups = "unit")
    public void testGetSharedInstance() throws Exception
    {
        CoordinateUtils testInstance = CoordinateUtils.getSharedInstance();

        assertNotNull(testInstance, "Did not create the initial shared instance");

        assertSame(CoordinateUtils.getSharedInstance(), testInstance, "Wasn't a proper shared instance");
    }

    @Test(groups = "unit")
    public void test1DTranslateInPlace() throws Exception
    {
        float[] testPoint = { 1.0f, 2.0f, 3.0f };

        CoordinateUtils classUnderTest = new CoordinateUtils();
        classUnderTest.translate(testPoint, 1, 1, 1, 1);

        assertEquals(testPoint[0], 2.0f, "Incorrect X translation");
        assertEquals(testPoint[1], 3.0f, "Incorrect Y translation");
        assertEquals(testPoint[2], 4.0f, "Incorrect Z translation");
    }

    @Test(groups = "unit")
    public void test1DPartialTranslateInPlace() throws Exception
    {
        float[] testPoint = { 1.0f, 2.0f, 3.0f, -1.0f, -2.0f, -3.0f };

        CoordinateUtils classUnderTest = new CoordinateUtils();
        classUnderTest.translate(testPoint, 1, 1, 1, 1);

        assertEquals(testPoint[0], 2.0f, "Incorrect X translation");
        assertEquals(testPoint[1], 3.0f, "Incorrect Y translation");
        assertEquals(testPoint[2], 4.0f, "Incorrect Z translation");

        assertEquals(testPoint[3], -1.0f, "Incorrect X translation");
        assertEquals(testPoint[4], -2.0f, "Incorrect Y translation");
        assertEquals(testPoint[5], -3.0f, "Incorrect Z translation");
    }

    @Test(groups = "unit")
    public void test2DTranslateInPlace() throws Exception
    {
        float[][] testPoint = { { 1.0f, 2.0f, 3.0f } };

        CoordinateUtils classUnderTest = new CoordinateUtils();
        classUnderTest.translate(testPoint, 1, 1, 1, 1);

        assertEquals(testPoint[0][0], 2.0f, "Incorrect X translation");
        assertEquals(testPoint[0][1], 3.0f, "Incorrect Y translation");
        assertEquals(testPoint[0][2], 4.0f, "Incorrect Z translation");
    }

    @Test(groups = "unit")
    public void test2DPartialTranslateInPlace() throws Exception
    {
        float[][] testPoint = { { 1.0f, 2.0f, 3.0f } , { -1.0f, -2.0f, -3.0f } };

        CoordinateUtils classUnderTest = new CoordinateUtils();
        classUnderTest.translate(testPoint, 1, 1, 1, 1);

        assertEquals(testPoint[0][0], 2.0f, "Incorrect X translation");
        assertEquals(testPoint[0][1], 3.0f, "Incorrect Y translation");
        assertEquals(testPoint[0][2], 4.0f, "Incorrect Z translation");

        assertEquals(testPoint[1][0], -1.0f, "Incorrect X translation");
        assertEquals(testPoint[1][1], -2.0f, "Incorrect Y translation");
        assertEquals(testPoint[1][2], -3.0f, "Incorrect Z translation");
    }


    @Test(groups = "unit")
    public void test1DTranslate() throws Exception
    {
        float[] testPoint = { 1.0f, 2.0f, 3.0f };
        float[] result = new float[testPoint.length];

        CoordinateUtils classUnderTest = new CoordinateUtils();
        classUnderTest.translate(testPoint, 1, result, 1, 1, 1);

        assertEquals(result[0], 2.0f, "Incorrect X translation");
        assertEquals(result[1], 3.0f, "Incorrect Y translation");
        assertEquals(result[2], 4.0f, "Incorrect Z translation");
    }

    @Test(groups = "unit")
    public void test1DPartialTranslate() throws Exception
    {
        float[] testPoint = { 1.0f, 2.0f, 3.0f, -1.0f, -2.0f, -3.0f };
        float[] result = new float[testPoint.length];

        CoordinateUtils classUnderTest = new CoordinateUtils();
        classUnderTest.translate(testPoint, 1, result, 1, 1, 1);

        assertEquals(result[0], 2.0f, "Incorrect X translation");
        assertEquals(result[1], 3.0f, "Incorrect Y translation");
        assertEquals(result[2], 4.0f, "Incorrect Z translation");

        assertEquals(result[3], 0.0f, "Should not copy the second X component");
        assertEquals(result[4], 0.0f, "Should not copy the second Y component");
        assertEquals(result[5], 0.0f, "Should not copy the second Z component");
    }

    @Test(groups = "unit")
    public void test2DTranslate() throws Exception
    {
        float[][] testPoint = { { 1.0f, 2.0f, 3.0f } };
        float[][] result = new float[1][3];

        CoordinateUtils classUnderTest = new CoordinateUtils();
        classUnderTest.translate(testPoint, 1, result, 1, 1, 1);

        assertEquals(result[0][0], 2.0f, "Incorrect X translation");
        assertEquals(result[0][1], 3.0f, "Incorrect Y translation");
        assertEquals(result[0][2], 4.0f, "Incorrect Z translation");
    }

    @Test(groups = "unit")
    public void test2DPartialTranslate() throws Exception
    {
        float[][] testPoint = { { 1.0f, 2.0f, 3.0f } , { -1.0f, -2.0f, -3.0f } };
        float[][] result = new float[2][3];

        CoordinateUtils classUnderTest = new CoordinateUtils();
        classUnderTest.translate(testPoint, 1, result, 1, 1, 1);

        assertEquals(result[0][0], 2.0f, "Incorrect X translation");
        assertEquals(result[0][1], 3.0f, "Incorrect Y translation");
        assertEquals(result[0][2], 4.0f, "Incorrect Z translation");

        assertEquals(result[1][0], 0.0f, "Should not copy the second X component");
        assertEquals(result[1][1], 0.0f, "Should not copy the second Y component");
        assertEquals(result[1][2], 0.0f, "Should not copy the second Z component");
    }
}
