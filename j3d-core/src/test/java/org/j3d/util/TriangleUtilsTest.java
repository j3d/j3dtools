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

package org.j3d.util;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class TriangleUtilsTest
{
    private static int[] TEST_INDICES = { 0, 1, 2 };
    private static float[] TEST_COORDS =
    {
        0.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f
    };

    private static float[] TEST_NORMALS =
    {
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f
    };

    private static float[] TEST_TEXCOORDS =
    {
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f
    };

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testTangentWithIndexNullIndices() throws Exception {
        float[] result = new float[9];

        TriangleUtils.createTangents(1, null, TEST_COORDS, TEST_NORMALS, TEST_TEXCOORDS, result);
    }


    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testTangentWithIndexNullCoords() throws Exception {
        float[] result = new float[9];

        TriangleUtils.createTangents(1, TEST_INDICES, null, TEST_NORMALS, TEST_TEXCOORDS, result);
    }


    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testTangentWithIndexNullNormals() throws Exception {
        float[] result = new float[9];

        TriangleUtils.createTangents(1, TEST_INDICES, TEST_COORDS, null, TEST_TEXCOORDS, result);
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testTangentWithIndexNullTexCoords() throws Exception {
        float[] result = new float[9];

        TriangleUtils.createTangents(1, TEST_INDICES, TEST_COORDS, TEST_NORMALS, null, result);
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testTangentWithIndexNullResult() throws Exception {
        float[] result = new float[9];

        TriangleUtils.createTangents(1, TEST_INDICES, TEST_COORDS, TEST_NORMALS, TEST_TEXCOORDS, null);
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testTangentNullIndices() throws Exception {
        float[] result = new float[9];

        TriangleUtils.createTangents(1, null, TEST_NORMALS, TEST_TEXCOORDS, result);
    }


    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testTangentNullCoords() throws Exception {
        float[] result = new float[9];

        TriangleUtils.createTangents(1, null, TEST_NORMALS, TEST_TEXCOORDS, result);
    }


    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testTangentNullNormals() throws Exception {
        float[] result = new float[9];

        TriangleUtils.createTangents(1, TEST_COORDS, null, TEST_TEXCOORDS, result);
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testTangentNullTexCoords() throws Exception {
        float[] result = new float[9];

        TriangleUtils.createTangents(1, TEST_COORDS, TEST_NORMALS, null, result);
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testTangentNullResult() throws Exception {
        float[] result = new float[9];

        TriangleUtils.createTangents(1, TEST_COORDS, TEST_NORMALS, TEST_TEXCOORDS, null);
    }
}
