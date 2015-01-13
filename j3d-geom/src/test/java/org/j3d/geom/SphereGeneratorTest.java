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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

// Local imports
// None

/**
 * A test case to check the functionality of the SphereGenerator implementation.
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
public class SphereGeneratorTest
{
    /** A non-standard radius for testing */
    private static final float TEST_RADIUS = 0.5f;

    /** A list of valid facet counts to make sure it generates correctly */
    private static final int[] VALID_FACETS = { 12, 32, 72 };

    /**
     * Test that we can create the Sphere generator with different constructors
     * and have it give us the right information for vertex counts.
     */
    @Test(groups = "unit")
    public void testCreate()
    {
        // test the default sphere is 1 radius. This should give
        // sides: 16 facets * 16 facets * 6 vertex per facet
        // total => 1024 vertices
        SphereGenerator generator = new SphereGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;

        generator.generate(data);

        assertFalse(generator.isHalf(), "Default sphere is missing the bottom");
        assertEquals(data.vertexCount, 1536, "Default sphere vertex count is wrong");

        float radius = generator.getDimension();

        assertEquals(radius, 1.0f, "Default sphere radius wrong");

        // Now test changing the dimension on an existing sphere
        generator.setDimensions(TEST_RADIUS, false);
        data.coordinates = null;
        generator.generate(data);

        assertFalse(generator.isHalf(), "Dimensioned sphere is missing the bottom");

        assertEquals(data.vertexCount, 1536, "Dimensioned vertex count is wrong");
        radius = generator.getDimension();

        assertEquals(radius, TEST_RADIUS, "Dimensioned sphere radius wrong");

        // check that the bottom flag is set independently
        generator.setDimensions(TEST_RADIUS, true);

        assertTrue(generator.isHalf(), "Dimensioned sphere bottom check is wrong");

        // test the sphere is radius 0.5
        generator = new SphereGenerator(TEST_RADIUS);
        data.coordinates = null;
        generator.generate(data);

        assertFalse(generator.isHalf(), "Test sphere is missing the bottom");
        assertEquals(data.vertexCount, 1536, "Test sphere vertex count is wrong");

        radius = generator.getDimension();

        assertEquals(radius, TEST_RADIUS, "Test sphere radius wrong");
    }

    /**
     * Test that the size of the array generated for coordinates is correct.
     * This also makes sure that the calculation routines do not generate
     * errors either
     */
    @Test(groups = "unit")
    public void testCoordinateArray()
    {
        // test the default sphere is radius 1
        SphereGenerator generator = new SphereGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;

        generator.generate(data);

        int vertices = data.vertexCount;
        float[] coords = data.coordinates;

        assertEquals(coords.length, vertices * 3, "Default sphere coordinate length wrong");

        generator.setDimensions(TEST_RADIUS, false);

        data.coordinates = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.coordinates;

        assertEquals(coords.length, vertices * 3, "Dimensioned sphere coordinate length wrong");

        generator = new SphereGenerator(TEST_RADIUS);

        data.coordinates = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.coordinates;

        assertEquals(coords.length, vertices * 3, "Test sphere coordinate length wrong");

        // Now check the same things again, but without the bottom
        generator = new SphereGenerator(TEST_RADIUS, true);

        int old_vertices = vertices;

        data.coordinates = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.coordinates;

        assertEquals(vertices, old_vertices / 2, "No-bottom sphere vertex count wrong");
        assertEquals(coords.length, vertices * 3, "No-bottom sphere coordinate length wrong");
    }

    /**
     * Test that the size of the array generated for coordinates is correct.
     * This also makes sure that the calculation routines do not generate
     * errors either
     */
    @Test(groups = "unit")
    public void testNormalArray()
    {
        // test the default sphere is 2, 2, 2
        SphereGenerator generator = new SphereGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        generator.generate(data);

        int vertices = data.vertexCount;
        float[] coords = data.normals;

        assertEquals(coords.length, vertices * 3, "Default sphere normal length wrong");


        generator.setDimensions(TEST_RADIUS, false);

        data.normals = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.normals;

        assertEquals(coords.length, vertices * 3, "Dimensioned sphere normal length wrong");

        generator = new SphereGenerator(TEST_RADIUS);

        data.normals = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.normals;

        assertEquals(coords.length, vertices * 3, "Test sphere normal length wrong");

        // Now check the same things again, but without the bottom
        generator = new SphereGenerator(TEST_RADIUS, true);

        int old_vertices = vertices;

        data.normals = null;

        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.normals;

        assertEquals(vertices, old_vertices / 2, "No-bottom sphere normal vertex count wrong");
        assertEquals(coords.length, vertices * 3, "No-bottom sphere normal length wrong");
    }


    @Test(groups = "unit", dataProvider = "invalid facets", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidFacetsConstructor(int numFacets)
    {
        new SphereGenerator(2, numFacets);
    }

    @Test(groups = "unit", dataProvider = "invalid facets", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidFacetsSetter(int numFacets)
    {
        SphereGenerator generator = new SphereGenerator();
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

        for(i = 0; i < VALID_FACETS.length; i++)
        {
            SphereGenerator generator = new SphereGenerator(1, VALID_FACETS[i]);
            reqd_count = VALID_FACETS[i] * VALID_FACETS[i] * 6;

            data.coordinates = null;
            generator.generate(data);

            vtx_count = data.vertexCount;
            assertEquals(vtx_count, reqd_count, "Construct vertex count wrong for " + VALID_FACETS[i]);

            // Now generate the vertices and look at the array
            reqd_count = reqd_count * 3;
            assertEquals(data.coordinates.length, reqd_count, "Generated initial vertex count wrong for " + VALID_FACETS[i]);

        }


        // Same thing again but using the setFacet method
        SphereGenerator generator = new SphereGenerator();
        for(i = 0; i < VALID_FACETS.length; i++)
        {
            generator.setFacetCount(VALID_FACETS[i]);
            reqd_count = VALID_FACETS[i] * VALID_FACETS[i] * 6;
            data.coordinates = null;
            generator.generate(data);

            vtx_count = data.vertexCount;
            assertEquals(vtx_count, reqd_count,  "Set vertex count wrong for " + VALID_FACETS[i]);

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
            { 7 }
        };
    }
}
