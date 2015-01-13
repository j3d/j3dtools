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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * A test case to check the functionality of the ConeGenerator implementation.
 * <p>
 *
 * The test aims to check the basic calculation routines to make sure there
 * are no array overruns and the geometry is updated correctly. It does not
 * do a check on the coordinates generated. That is a visual test and is
 * performed by the example code.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class ConeGeneratorTest
{
    /** A non-standard height for testing */
    private static final float TEST_HEIGHT = 1.4f;

    /** A non-standard radius for testing */
    private static final float TEST_RADIUS = 0.5f;

    /** A list of valid facet counts to make sure it generates correctly */
    private static final int[] VALID_FACETS = { 13, 6, 10 };

    /**
     * Test that we can create the Cone generator with different constructors
     * and have it give us the right information for vertex counts.
     */
    @Test(groups = "unit")
    public void testCreate()
    {
        // test the default cone is 2 height, 1 radius. This should give
        // 16 facets * 3 vertex per face * 2 (top + bottom) = 96 vertices.
        ConeGenerator generator = new ConeGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;

        generator.generate(data);

        assertTrue(generator.hasBottom(), "Default cone is missing the bottom");
        assertEquals(data.vertexCount, 96, "Default cone vertex count is wrong");

        float[] dimensions = generator.getDimensions();

        assertEquals(dimensions[0], 2.0f, "Default cone height wrong");
        assertEquals(dimensions[1], 1.0f, "Default cone radius wrong");

        // Now test changing the dimension on an existing cone
        generator.setDimensions(TEST_HEIGHT, TEST_RADIUS, true);

        data.coordinates = null;
        generator.generate(data);

        assertTrue(generator.hasBottom(), "Dimensioned cone is missing the bottom");

        assertEquals(data.vertexCount, 96, "Dimensioned vertex count is wrong");

        dimensions = generator.getDimensions();

        assertEquals(dimensions[0], TEST_HEIGHT, "Dimensioned cone radius wrong");
        assertEquals(dimensions[1], TEST_RADIUS, "Dimensioned cone height wrong");

        // check that the bottom flag is set independently
        generator.setDimensions(TEST_HEIGHT, TEST_RADIUS, false);

        assertTrue(!generator.hasBottom(), "Dimensioned cone bottom check is wrong");

        // test the default cone is 2, 2, 2
        generator = new ConeGenerator(TEST_HEIGHT, TEST_RADIUS);

        data.coordinates = null;
        generator.generate(data);


        assertTrue(generator.hasBottom(), "Test cone is missing the bottom");
        assertEquals(data.vertexCount, 96, "Test cone vertex count is wrong");

        dimensions = generator.getDimensions();

        assertEquals(dimensions[0], TEST_HEIGHT, "Test cone radius wrong");
        assertEquals(dimensions[1], TEST_RADIUS, "Test cone height wrong");
    }

    /**
     * Test that the size of the array generated for coordinates is correct.
     * This also makes sure that the calculation routines do not generate
     * errors either
     */
    @Test(groups = "unit")
    public void testCoordinateArray()
    {
        // test the default cone is 2, 2, 2
        ConeGenerator generator = new ConeGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;

        generator.generate(data);

        int vertices = data.vertexCount;
        float[] coords = data.coordinates;

        assertEquals(coords.length, vertices * 3, "Default cone coordinate length wrong");

        generator.setDimensions(TEST_HEIGHT, TEST_RADIUS, true);

        data.coordinates = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.coordinates;

        assertEquals(coords.length, vertices * 3, "Dimensioned cone coordinate length wrong");

        generator = new ConeGenerator(TEST_HEIGHT, TEST_RADIUS);

        data.coordinates = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.coordinates;

        assertEquals(coords.length, vertices * 3, "Test cone coordinate length wrong");

        // Now check the same things again, but without the bottom
        int old_vertices = vertices;
        generator = new ConeGenerator(TEST_HEIGHT, TEST_RADIUS, true, false);

        data.coordinates = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.coordinates;

        assertEquals(vertices, old_vertices / 2, "No-bottom code vertex count wrong");
        assertEquals(coords.length, vertices * 3, "No-bottom cone coordinate length wrong");
    }

    /**
     * Test that the size of the array generated for coordinates is correct.
     * This also makes sure that the calculation routines do not generate
     * errors either
     */
    @Test(groups = "unit")
    public void testNormalArray()
    {
        // test the default cone is 2, 2, 2
        ConeGenerator generator = new ConeGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        generator.generate(data);

        int vertices = data.vertexCount;
        float[] coords = data.normals;

        assertEquals(coords.length, vertices * 3, "Default cone normal length wrong");

        generator.setDimensions(TEST_HEIGHT, TEST_RADIUS, true);

        data.normals = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.normals;

        assertEquals(coords.length, vertices * 3, "Dimensioned cone normal length wrong");

        generator = new ConeGenerator(TEST_HEIGHT, TEST_RADIUS);

        data.normals = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.normals;

        assertEquals(coords.length, vertices * 3, "Test cone normal length wrong");

        // Now check the same things again, but without the bottom
        generator = new ConeGenerator(TEST_HEIGHT, TEST_RADIUS, true, false);


        int old_vertices = vertices;

        data.normals = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.normals;

        assertEquals(vertices, old_vertices / 2, "No-bottom code vertex count wrong");
        assertEquals(coords.length, vertices * 3, "No-bottom cone coordinate length wrong");
    }

    @Test(groups = "unit", dataProvider = "invalid facets", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidFacetsConstructor(int numFacets)
    {
        new ConeGenerator(2, 1, numFacets);
    }

    @Test(groups = "unit", dataProvider = "invalid facets", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidFacetsSetter(int numFacets)
    {
        ConeGenerator generator = new ConeGenerator();
        generator.setFacetCount(numFacets);
    }

    /**
     * Test to makes sure the vertex count has been updated properly and
     * the generated array lengths are correct.
     */
    @Test(groups = "unit")
    public void testValidFacets()
    {
        int i;
        int reqd_count;
        int vtx_count;
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;

        // Test with a negative value, zero and value less than 3. All should
        // generate exceptions.
        for(i = 0; i < VALID_FACETS.length; i++)
        {
            ConeGenerator generator = new ConeGenerator(2, 1, VALID_FACETS[i]);
            reqd_count = VALID_FACETS[i] * 6;

            data.coordinates = null;
            generator.generate(data);

            vtx_count = data.vertexCount;
            assertEquals(vtx_count, reqd_count, "Construct vertex count wrong for " + VALID_FACETS[i]);

            // Now generate the vertices and look at the array
            reqd_count = reqd_count * 3;

            assertEquals(data.coordinates.length, reqd_count, "Generated initial vertex count wrong for " + VALID_FACETS[i]);

        }


        // Same thing again but using the setFacet method
        ConeGenerator generator = new ConeGenerator();
        for(i = 0; i < VALID_FACETS.length; i++)
        {
            generator.setFacetCount(VALID_FACETS[i]);
            reqd_count = VALID_FACETS[i] * 6;
            data.coordinates = null;
            generator.generate(data);

            vtx_count = data.vertexCount;
            assertEquals(vtx_count, reqd_count, "Set vertex count wrong for " + VALID_FACETS[i]);

            reqd_count = reqd_count * 3;
            assertEquals(data.coordinates.length, reqd_count, "Generated set vertex count wrong for " + VALID_FACETS[i]);
        }
    }

    @DataProvider(name = "invalid facets")
    public Object[][] generateInvalidFacetsData() {
        return new Object[][] {
            { -5 },
            { 0 },
            { 2 }
        };
    }
}

