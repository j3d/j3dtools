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

import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Point3d;
import org.j3d.util.MatrixUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for the view frustum class
 *
 * @author justin
 */
public class ViewFrustumTest
{
    final double NEAR_DISTANCE = -0.0001;
    final double FAR_DISTANCE = -1.0;
    
    private class TestFrustum extends ViewFrustum
    {
        MatrixUtils mu = new MatrixUtils();

        TestFrustum(int count)
        {
            super(count);


        }

        @Override
        protected void getInverseWorldProjection(int id, Matrix4d matrix)
        {
            mu.generateProjectionMatrix(-1, 1, 1, -1, NEAR_DISTANCE, FAR_DISTANCE, matrix);
            mu.inverse(matrix, matrix);
        }
    }

    @Test(groups = "unit")
    public void testBounds() throws Exception
    {
        ViewFrustum classUnderTest = new TestFrustum(1);
        classUnderTest.viewingPlatformMoved();

        Point3d result_min = new Point3d();
        Point3d result_max = new Point3d();
        
        classUnderTest.getBounds(result_min, result_max);
        
        assertEquals(result_min.x, -1.0, 0.001, "Minimum X bound incorrect");
        assertEquals(result_min.y, -1.0, 0.001, "Minimum Y bound incorrect");
        assertEquals(result_min.z, NEAR_DISTANCE, 0.001, "Minimum Z bound incorrect");

        assertEquals(result_max.x, 1.0, 0.001, "Maximum X bound incorrect");
        assertEquals(result_max.y, 1.0, 0.001, "Maximum Y bound incorrect");
        assertEquals(result_max.z, FAR_DISTANCE, 0.001, "Maximum Z bound incorrect");
    }


    @Test(groups = "unit", dataProvider = "triangle intersection")
    public void testIsTriangleInFrustumPrimitives(Float[] p1, Float[] p2, Float[] p3, int expected) throws Exception
    {

    }

    @Test(enabled = false, groups = "unit", dataProvider = "triangle intersection")
    public void testIsTriangleInFrustumPoints(Float[] p1, Float[] p2, Float[] p3, int expected) throws Exception
    {
        Point3d pt1 = new Point3d();
        pt1.set(p1[0], p1[1], p1[2]);

        Point3d pt2 = new Point3d();
        pt2.set(p2[0], p2[1], p2[2]);

        Point3d pt3 = new Point3d();
        pt3.set(p3[0], p3[1], p3[2]);

        ViewFrustum classUnderTest = new TestFrustum(1);
        classUnderTest.viewingPlatformMoved();

        assertEquals(classUnderTest.isTriangleInFrustum(pt1, pt2, pt3), expected, "Triangle intersection wrong");
    }

    @Test(enabled = false, groups = "unit", dataProvider = "point intersection")
    public void testIsPointInFrustum(float x, float y, float z, int inside) throws Exception
    {
        Point3d pt = new Point3d();
        pt.set(x, y, z);

        ViewFrustum classUnderTest = new TestFrustum(1);
        classUnderTest.viewingPlatformMoved();
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
        retval[0][3] = ViewFrustum.IN;

        retval[1][0] = 0;
        retval[1][1] = 0.5f;
        retval[1][2] = 0;
        retval[1][3] = ViewFrustum.IN;

        retval[2][0] = 0;
        retval[2][1] = -0.5f;
        retval[2][2] = 0;
        retval[2][3] = ViewFrustum.IN;

        retval[3][0] = 0;
        retval[3][1] = 1.5f;
        retval[3][2] = 0;
        retval[3][3] = ViewFrustum.OUT;

        retval[4][0] = 0;
        retval[4][1] = -1.5f;
        retval[4][2] = 0;
        retval[4][3] = ViewFrustum.OUT;

        retval[5][0] = 0.5f;
        retval[5][1] = 0;
        retval[5][2] = 0;
        retval[5][3] = ViewFrustum.IN;

        retval[6][0] = -0.5f;
        retval[6][1] = 0;
        retval[6][2] = 0;
        retval[6][3] = ViewFrustum.IN;

        retval[7][0] = 1.5f;
        retval[7][1] = 0;
        retval[7][2] = 0;
        retval[7][3] = ViewFrustum.OUT;

        retval[8][0] = -1.5f;
        retval[8][1] = 0;
        retval[8][2] = 0;
        retval[8][3] = ViewFrustum.OUT;

        retval[9][0] = 0;
        retval[9][1] = 0;
        retval[9][2] = 0.5f;
        retval[9][3] = ViewFrustum.IN;

        retval[10][0] = 0;
        retval[10][1] = 0;
        retval[10][2] = -0.5f;
        retval[10][3] = ViewFrustum.IN;

        retval[11][0] = 0;
        retval[11][1] = 0;
        retval[11][2] = 1.5f;
        retval[11][3] = ViewFrustum.OUT;

        retval[12][0] = 0;
        retval[12][1] = 0;
        retval[12][2] = -1.5f;
        retval[12][3] = ViewFrustum.OUT;

        return retval;
    }
}
