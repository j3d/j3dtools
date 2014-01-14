/*
 * j3d.org Copyright (c) 2001-2014
 *                                 Java Source
 *
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read docs/LGPL.txt for more information
 *
 *  This software comes with the standard NO WARRANTY disclaimer for any
 *  purpose. Use it at your own risk. If there's a problem you get to fix it.
 */

package org.j3d.util.frustum;

import org.j3d.maths.vector.Point3d;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the Canvas3D frustum representation.
 *
 * @author justin
 */
public class Canvas3DFrustumTest
{
    private float[][] testFrustumPlanes =
    {
        {  0,  1,  0, 1 },
        {  0, -1,  0, 1 },
        {  0,  0,  1, 1 },
        {  0,  0, -1, 1 },
        {  1,  0,  0, 1 },
        { -1,  0,  0, 1 }
    };

    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        Canvas3DFrustum classUnderTest = new Canvas3DFrustum();

        Point3d test_point1 = new Point3d();

        // Make sure there's always 6 planes for this frustum
        assertNotNull(classUnderTest.frustumPlanes, "No frustum plane array created");
        assertEquals(classUnderTest.frustumPlanes.length, 6, "Wrong number of frustum planes created");

        for(int i = 0; i < 6; i++)
            assertNotNull(classUnderTest.frustumPlanes[i], "Missing frustum plane vector at index " + i);

        assertFalse(classUnderTest.isPointInFrustum(test_point1), "test point found in empty planes");
    }


    @Test(groups = "unit", dataProvider = "triangle intersection")
    public void testIsTriangleInFrustum(Float[] p1, Float[] p2, Float[] p3, int expected) throws Exception
    {
        Canvas3DFrustum classUnderTest = setupTestCanvas();

        Point3d pt1 = new Point3d();
        pt1.set(p1[0], p1[1], p1[2]);

        Point3d pt2 = new Point3d();
        pt2.set(p2[0], p2[1], p2[2]);

        Point3d pt3 = new Point3d();
        pt3.set(p3[0], p3[1], p3[2]);

        assertEquals(classUnderTest.isTriangleInFrustum(pt1, pt2, pt3), expected, "Triangle intersection wrong");
    }

    @Test(groups = "unit", dataProvider = "point intersection")
    public void testIsPointInFrustum(float x, float y, float z, boolean inside) throws Exception
    {
        Canvas3DFrustum classUnderTest = setupTestCanvas();

        Point3d pt = new Point3d();
        pt.set(x, y, z);

        assertEquals(classUnderTest.isPointInFrustum(pt), inside, "Incorrect point determination");
    }

    @DataProvider(name = "triangle intersection")
    public Object[][] generateTriangleIntersectionData()
    {
        Object[][] retval = new Object[3][4];

        retval[0][0] = new Float[] { 0f, 0f, 0f };
        retval[0][1] = new Float[] { 0f, 0.5f, 0f };
        retval[0][2] = new Float[] { 0f, -0.5f, 0f };
        retval[0][3] = Canvas3DFrustum.IN;

        retval[1][0] = new Float[] { 2f, 2f, 2f };
        retval[1][1] = new Float[] { 2f, 2.5f, 2f };
        retval[1][2] = new Float[] { 2f, -2.5f, 2f };
        retval[1][3] = Canvas3DFrustum.OUT;

        retval[2][0] = new Float[] { 2f, 2f, 2f };
        retval[2][1] = new Float[] { 0f, 0.5f, 0f };
        retval[2][2] = new Float[] { 2f, -0.5f, 2f };
        retval[2][3] = Canvas3DFrustum.CLIPPED;

        return retval;
    }

    @DataProvider(name = "point intersection")
    public Object[][] generatePointIntersectionData()
    {
        Object[][] retval = new Object[13][4];

        retval[0][0] = 0;
        retval[0][1] = 0;
        retval[0][2] = 0;
        retval[0][3] = true;

        retval[1][0] = 0;
        retval[1][1] = 0.5f;
        retval[1][2] = 0;
        retval[1][3] = true;

        retval[2][0] = 0;
        retval[2][1] = -0.5f;
        retval[2][2] = 0;
        retval[2][3] = true;

        retval[3][0] = 0;
        retval[3][1] = 1.5f;
        retval[3][2] = 0;
        retval[3][3] = false;

        retval[4][0] = 0;
        retval[4][1] = -1.5f;
        retval[4][2] = 0;
        retval[4][3] = false;

        retval[5][0] = 0.5f;
        retval[5][1] = 0;
        retval[5][2] = 0;
        retval[5][3] = true;

        retval[6][0] = -0.5f;
        retval[6][1] = 0;
        retval[6][2] = 0;
        retval[6][3] = true;

        retval[7][0] = 1.5f;
        retval[7][1] = 0;
        retval[7][2] = 0;
        retval[7][3] = false;

        retval[8][0] = -1.5f;
        retval[8][1] = 0;
        retval[8][2] = 0;
        retval[8][3] = false;

        retval[9][0] = 0;
        retval[9][1] = 0;
        retval[9][2] = 0.5f;
        retval[9][3] = true;

        retval[10][0] = 0;
        retval[10][1] = 0;
        retval[10][2] = -0.5f;
        retval[10][3] = true;

        retval[11][0] = 0;
        retval[11][1] = 0;
        retval[11][2] = 1.5f;
        retval[11][3] = false;

        retval[12][0] = 0;
        retval[12][1] = 0;
        retval[12][2] = -1.5f;
        retval[12][3] = false;

        return retval;
    }

    /**
     * Convenience to setup the canvas with test planes.
     *
     * @return
     */
    private Canvas3DFrustum setupTestCanvas()
    {
        Canvas3DFrustum retval = new Canvas3DFrustum();

        for(int i = 0; i < 6; i++)
        {
            retval.frustumPlanes[i].set(testFrustumPlanes[i][0],
                                                testFrustumPlanes[i][1],
                                                testFrustumPlanes[i][2],
                                                testFrustumPlanes[i][3]);
        }

        return retval;
    }
}
