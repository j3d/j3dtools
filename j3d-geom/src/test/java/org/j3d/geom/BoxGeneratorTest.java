/*****************************************************************************
 *                      J3D.org Copyright (c) 2000
 *                           Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom;

// External imports
// None

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * A test case to check the functionality of the BoxGenerator implementation.
 * <p>
 *
 * The test aims to check the basic calculation routines to make sure there
 * are no array overruns and the geometry is updated correctly. It does not
 * do a check on the coordinates generated. That is a visual test and is
 * performed by the example code.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class BoxGeneratorTest
{
    /** A non-standard box shape for testing */
    private static final float[] TEST_BOX = {0.4f, 1, 6.8f};

    /**
     * Test that we can create the Box generator with different constructors
     * and have it give us the right information for vertex counts.
     */
    @Test(groups = "unit")
    public void testCreate()
    {
        // test the default box is 2, 2, 2
        BoxGenerator generator = new BoxGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;

        generator.generate(data);

        assertEquals(data.vertexCount, 36, "Default box vertex count is wrong");

        float[] dimensions = generator.getDimensions();

        assertEquals(dimensions[0], 2.0f, "Default box width wrong");
        assertEquals(dimensions[1], 2.0f, "Default box height wrong");
        assertEquals(dimensions[2], 2.0f, "Default box depth wrong");

        // Now test changing the dimension on an existing box
        generator.setDimensions(TEST_BOX[0], TEST_BOX[1], TEST_BOX[2]);
        data.coordinates = null;
        generator.generate(data);

        assertEquals(data.vertexCount, 36, "Dimensions vertex count is wrong");

        dimensions = generator.getDimensions();

        assertEquals(dimensions[0], TEST_BOX[0], "Dimensions width wrong");
        assertEquals(dimensions[1], TEST_BOX[1], "Dimensions height wrong");
        assertEquals(dimensions[2], TEST_BOX[2], "Dimensions depth wrong");

        // test the default box is 2, 2, 2
        generator = new BoxGenerator(TEST_BOX[0], TEST_BOX[1], TEST_BOX[2]);
        generator.generate(data);

        assertEquals(data.vertexCount, 36, "Test box vertex count is wrong");

        dimensions = generator.getDimensions();

        assertEquals(dimensions[0], TEST_BOX[0], "Test box width wrong");
        assertEquals(dimensions[1], TEST_BOX[1], "Test box height wrong");
        assertEquals(dimensions[2], TEST_BOX[2], "Test box depth wrong");

    }

    /**
     * Test that the size of the array generated for coordinates is correct.
     * This also makes sure that the calculation routines do not generate
     * errors either
     */
    @Test(groups = "unit")
    public void testCoordinateArray()
    {
        // test the default box is 2, 2, 2
        BoxGenerator generator = new BoxGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;

        generator.generate(data);

        int vertices = data.vertexCount;
        float[] coords = data.coordinates;

        assertEquals(coords.length, vertices * 3, "Default box coordinate length wrong");

        generator.setDimensions(TEST_BOX[0], TEST_BOX[1], TEST_BOX[2]);

        data.coordinates = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.coordinates;

        assertEquals(coords.length, vertices * 3, "Dimensioned box coordinate length wrong");

        generator = new BoxGenerator(TEST_BOX[0], TEST_BOX[1], TEST_BOX[2]);

        data.coordinates = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.coordinates;

        assertEquals(coords.length, vertices * 3, "Test box coordinate length wrong");
    }

    /**
     * Test that the size of the array generated for coordinates is correct.
     * This also makes sure that the calculation routines do not generate
     * errors either
     */
    @Test(groups = "unit")
    public void testNormalArray()
    {
        // test the default box is 2, 2, 2
        BoxGenerator generator = new BoxGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        generator.generate(data);

        int vertices = data.vertexCount;
        float[] coords = data.normals;


        assertEquals(coords.length, vertices * 3, "Default box normal length wrong");

        generator.setDimensions(TEST_BOX[0], TEST_BOX[1], TEST_BOX[2]);

        data.normals = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.normals;

        assertEquals(coords.length, vertices * 3, "Dimensioned box normal length wrong");

        generator = new BoxGenerator(TEST_BOX[0], TEST_BOX[1], TEST_BOX[2]);

        data.normals = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.normals;

        assertEquals(coords.length, vertices * 3, "Test box normal length wrong");
    }
}

