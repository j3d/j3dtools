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

public class TriangulationUtilsTest
{
    @Test(groups = "unit")
    public void testConcaveTest() throws Exception
    {
        float[] testVertices = { 0, 0, 0,  1, 0, 0,  0, 1, 0};
        float[] testNormal = { 0, 0, -1 };
        float[] testReverseNormal = { 0, 0, 1 };

        // Basic ordering
        assertTrue(TriangulationUtils.isConvexVertex(testVertices, 3, 0, 6, testNormal), "Basic");
        assertFalse(TriangulationUtils.isConvexVertex(testVertices, 3, 0, 6, testReverseNormal),
                    "Reverse normal basic");

        // Reverse the vertex ordering, should flip the results.
        assertFalse(TriangulationUtils.isConvexVertex(testVertices, 6, 0, 3, testNormal), "Inverted order basic");
        assertTrue(TriangulationUtils.isConvexVertex(testVertices, 6, 0, 3, testReverseNormal),
                   "Inverted order reverse basic");
    }

    @Test(groups = "unit")
    public void testConcavePolygonSimpleTriangle() throws Exception
    {
        float[] testPolygon = { 0, 0, 0,  1, 0, 0,  0, 1, 0};
        float[] testPolygonNormal = { 0, 0, -1 };

        int[] resultCoordIndex = new int[4];

        // Test that triangulating a triangle returns the original triangle
        TriangulationUtils classUnderTest = new TriangulationUtils();
        assertEquals(classUnderTest.triangulateConcavePolygon(testPolygon, 0, 3, resultCoordIndex, testPolygonNormal),
                     1,
                     "Basic triangle generated wrong number of output triangles");

        // validate that the indices in the output are in the 0-2 range, to make
        // sure it is not generating wild outputs
        for(int i = 0; i < 3; i++)
        {
            // Turn this into triangle indices for test, not array index
            int vertexIndex = resultCoordIndex[i] / 3;
            assertTrue(vertexIndex < 3 && vertexIndex >= 0,
                       "Result coordinate[" + i + "] is " + vertexIndex + " and not within [0,2]");
        }
    }

    @Test(groups = "unit")
    public void testConcavePolygonSimpleQuad() throws Exception
    {
        float[] testPolygon = { 0, 0, 0,  1, 0, 0,  1, 1, 0,  0, 1, 0};
        float[] testPolygonNormal = { 0, 0, 1 };

        int[] resultCoordIndex = new int[6];

        // Correctly defined quad should always end up as 2 triangles
        TriangulationUtils classUnderTest = new TriangulationUtils();
        assertEquals(classUnderTest.triangulateConcavePolygon(testPolygon, 0, 4, resultCoordIndex, testPolygonNormal),
                     2,
                     "Quad should generate only 2 triangles");

        // validate that the indices in the output are in the 0-2 range, to make
        // sure it is not generating wild outputs
        for(int i = 0; i < 4; i++)
        {
            // Turn this into triangle indices for test, not array index
            int vertexIndex = resultCoordIndex[i] / 3;
            assertTrue(vertexIndex < 4 && vertexIndex >= 0,
                       "Result coordinate[" + i + "] is " + vertexIndex + " and not within [0,3]");
        }
    }

    @Test(groups = "unit")
    public void testConcavePolygonQuadWindingAndNormalDisagree() throws Exception
    {
        float[] testPolygon = { 0, 0, 0,  1, 0, 0,  1, 1, 0,  0, 1, 0};
        float[] testPolygonNormal = { 0, 0, -1 };

        int[] resultCoordIndex = new int[6];

        // If the ordering of vertices and the face normal disagree on direction, we should not
        // end up with any triangles in the output because it would assume that everything is
        // a convex vertex, not a concave vertex.
        TriangulationUtils classUnderTest = new TriangulationUtils();
        assertEquals(classUnderTest.triangulateConcavePolygon(testPolygon, 0, 4, resultCoordIndex, testPolygonNormal),
                     0,
                     "When RH winding and normal disagree, there should be no output");
    }

    @Test(groups = "unit")
    public void testBowtieQuad() throws Exception
    {
        float[] testPolygon = { 0, 0, 0,  1, 1, 0,  1, 0, 0,  0, 1, 0};
        float[] testPolygonNormal = { 0, 0, 1 };

        int[] resultCoordIndex = new int[6];

        // A bowtie shaped quad has convex vertices, meaning it should result in no output.
        TriangulationUtils classUnderTest = new TriangulationUtils();
        assertEquals(classUnderTest.triangulateConcavePolygon(testPolygon, 0, 4, resultCoordIndex, testPolygonNormal),
                     0,
                     "In a bowtie quad, there should be no output");
    }
}
