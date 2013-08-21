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

// Local imports
// None

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * A test case to check the functionality of the CylinderGenerator implementation.
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
public class CylinderGeneratorTest
{
    /** A non-standard height for testing */
    private static final float TEST_HEIGHT = 1.4f;

    /** A non-standard radius for testing */
    private static final float TEST_RADIUS = 0.5f;

    /** A list of valid facet counts to make sure it generates correctly */
    private static final int[] VALID_FACETS = { 13, 6, 10 };

    /**
     * Test that we can create the Cylinder generator with different constructors
     * and have it give us the right information for vertex counts.
     */
    @Test(groups = "unit")
    public void testCreate()
    {
        // test the default cylinder is 2 height, 1 radius. This should give
        // sides: 16 facets * 6 vertex per face
        // top:   16 facets * 3 vertex per face * 2 (top + bottom)
        // total => 192 vertices
        CylinderGenerator generator = new CylinderGenerator();
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;

        generator.generate(data);

        assertTrue(generator.hasEnds(), "Default cylinder is missing the bottom");
        assertEquals(data.vertexCount, 192, "Default cylinder vertex count is wrong");

        float[] dimensions = generator.getDimensions();

        assertEquals(dimensions[0], 2.0f, "Default cylinder height wrong");
        assertEquals(dimensions[1], 1.0f, "Default cylinder radius wrong");

        // Now test changing the dimension on an existing cylinder
        generator.setDimensions(TEST_HEIGHT, TEST_RADIUS, true, true);
        data.coordinates = null;
        generator.generate(data);

        assertTrue(generator.hasEnds(), "Dimensioned cylinder is missing the bottom");
        assertEquals(data.vertexCount, 192, "Dimensioned vertex count is wrong");

        dimensions = generator.getDimensions();

        assertEquals(dimensions[0], TEST_HEIGHT, "Dimensioned cylinder radius wrong");
        assertEquals(dimensions[1], TEST_RADIUS, "Dimensioned cylinder height wrong");

        // check that the bottom flag is set independently
        generator.setDimensions(TEST_HEIGHT, TEST_RADIUS, false, true);
        data.coordinates = null;
        generator.generate(data);

        assertTrue(!generator.hasEnds(), "Dimensioned cylinder bottom check is wrong");

        // test the default cylinder is 2, 2, 2
        generator = new CylinderGenerator(TEST_HEIGHT, TEST_RADIUS);
        data.coordinates = null;
        generator.generate(data);

        assertTrue(generator.hasEnds(), "Test cylinder is missing the bottom");
        assertEquals(data.vertexCount, 192, "Test cylinder vertex count is wrong");

        dimensions = generator.getDimensions();

        assertEquals(dimensions[0], TEST_HEIGHT, "Test cylinder radius wrong");
        assertEquals(dimensions[1], TEST_RADIUS, "Test cylinder height wrong");
    }

    /**
     * Test that the size of the array generated for coordinates is correct.
     * This also makes sure that the calculation routines do not generate
     * errors either
     */
    @Test(groups = "unit")
    public void testCoordinateArray()
    {
        // test the default cylinder is 2, 2, 2
        CylinderGenerator generator = new CylinderGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;

        generator.generate(data);

        int vertices = data.vertexCount;
        float[] coords = data.coordinates;

        assertEquals(vertices * 3, coords.length, "Default cylinder coordinate length wrong");

        generator.setDimensions(TEST_HEIGHT, TEST_RADIUS, true, true);

        data.coordinates = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.coordinates;

        assertEquals(vertices * 3, coords.length, "Dimensioned cylinder coordinate length wrong");

        generator = new CylinderGenerator(TEST_HEIGHT, TEST_RADIUS);

        data.coordinates = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.coordinates;

        assertEquals(vertices * 3, coords.length, "Test cylinder coordinate length wrong");

        // Now check the same things again, but without the bottom
        generator = new CylinderGenerator(TEST_HEIGHT, TEST_RADIUS, false, true);

        int old_vertices = vertices;

        data.coordinates = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.coordinates;

        assertEquals(vertices, old_vertices / 2, "No-bottom cylinder vertex count wrong");
        assertEquals(vertices * 3, coords.length, "No-bottom cylinder coordinate length wrong");
    }

    /**
     * Test that the size of the array generated for coordinates is correct.
     * This also makes sure that the calculation routines do not generate
     * errors either
     */
    @Test(groups = "unit")
    public void testNormalArray()
    {
        // test the default cylinder is 2, 2, 2
        CylinderGenerator generator = new CylinderGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        generator.generate(data);

        int vertices = data.vertexCount;
        float[] coords = data.normals;

        assertEquals(vertices * 3, coords.length, "Default cylinder normal length wrong");

        generator.setDimensions(TEST_HEIGHT, TEST_RADIUS, true, true);

        data.normals = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.normals;

        assertEquals(vertices * 3, coords.length, "Dimensioned cylinder normal length wrong");

        generator = new CylinderGenerator(TEST_HEIGHT, TEST_RADIUS);

        data.normals = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.normals;

        assertEquals(vertices * 3, coords.length, "Test cylinder normal length wrong");

        // Now check the same things again, but without the bottom
        generator = new CylinderGenerator(TEST_HEIGHT, TEST_RADIUS, false, true);

        int old_vertices = vertices;

        data.normals = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.normals;

        assertEquals(vertices, old_vertices / 2, "No-bottom code vertex count wrong");
        assertEquals(vertices * 3, coords.length, "No-bottom cylinder coordinate length wrong");

        // Now check the same things again, but without the bottom and top as separate calls
        generator = new CylinderGenerator(TEST_HEIGHT, TEST_RADIUS, false, false, true);

        data.normals = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.normals;

        assertEquals(vertices, old_vertices / 2, "No-bottom code vertex count wrong");

        assertEquals(vertices * 3, coords.length, "No-bottom cylinder coordinate length wrong");
    }

    @Test(groups = "unit", dataProvider = "invalid facets", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidFacetsConstructor(int numFacets)
    {
        new CylinderGenerator(2, 1, numFacets);
    }

    @Test(groups = "unit", dataProvider = "invalid facets", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidFacetsSetter(int numFacets)
    {
        CylinderGenerator generator = new CylinderGenerator();
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
            CylinderGenerator generator = new CylinderGenerator(2, 1, VALID_FACETS[i]);
            reqd_count = VALID_FACETS[i] * 12;

            data.coordinates = null;
            generator.generate(data);
            vtx_count = data.vertexCount;

            assertEquals(vtx_count, reqd_count, "Construct vertex count wrong for " + VALID_FACETS[i]);

            // Now generate the vertices and look at the array
            reqd_count = reqd_count * 3;

            assertEquals(data.coordinates.length, reqd_count, "Generated initial vertex count wrong for " + VALID_FACETS[i]);
        }


        // Same thing again but using the setFacet method
        CylinderGenerator generator = new CylinderGenerator();
        for(i = 0; i < VALID_FACETS.length; i++)
        {
            generator.setFacetCount(VALID_FACETS[i]);
            reqd_count = VALID_FACETS[i] * 12;
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
            { 2 },
        };
    }

}

