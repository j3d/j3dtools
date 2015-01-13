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

/**
 * A test case to check the functionality of the TorusGenerator implementation.
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
public class TorusGeneratorTest
{
    /** A non-standard inner outer radius for testing */
    private static final float TEST_INNER_RADIUS = 0.5f;

    /** A non-standard outer outer radius for testing */
    private static final float TEST_OUTER_RADIUS = 6.3f;

    /** A list of valid inner facet counts to make sure it generates correctly */
    private static final int[] VALID_INNER_FACETS = { 12, 32, 72 };

    /** A list of valid outer facet counts to make sure it generates correctly */
    private static final int[] VALID_OUTER_FACETS = { 10, 20, 32 };

    /**
     * Test that we can create the Torus generator with different constructors
     * and have it give us the right information for vertex counts.
     */
    @Test(groups = "unit")
    public void testCreate()
    {
        // test the default sphere is 1 outer radius. This should give
        // sides: 16 facets * 16 facets * 4 vertex per facet
        // total => 1024 vertices
        TorusGenerator generator = new TorusGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.QUADS;

        generator.generate(data);

        assertEquals(data.vertexCount, 1024, "Default torus vertex count");

        float[] dimensions = generator.getDimensions();

        assertEquals(dimensions[0], 0.25f, "Default torus inner radius");
        assertEquals(dimensions[1], 1.0f, "Default torus outer radius");

        // Now test changing the dimension on an existing torus
        generator.setDimensions(TEST_INNER_RADIUS, TEST_OUTER_RADIUS);

        data.coordinates = null;
        generator.generate(data);

        assertEquals(data.vertexCount, 1024, "Dimensioned vertex count is wrong");

        dimensions = generator.getDimensions();

        assertEquals(dimensions[0], TEST_INNER_RADIUS, "Dimensioned torus outer radius wrong");
        assertEquals(dimensions[1], TEST_OUTER_RADIUS, "Dimensioned torus inner radius wrong");

        // test the default torus is 2, 2, 2
        generator = new TorusGenerator(TEST_INNER_RADIUS, TEST_OUTER_RADIUS);

        data.coordinates = null;
        generator.generate(data);

        assertEquals(data.vertexCount, 1024, "Test torus vertex count is wrong");

        dimensions = generator.getDimensions();

        assertEquals(dimensions[0], TEST_INNER_RADIUS, "Test torus outer radius wrong");
        assertEquals(dimensions[1], TEST_OUTER_RADIUS, "Test torus inner radius wrong");
    }

    /**
     * Test that the size of the array generated for coordinates is correct.
     * This also makes sure that the calculation routines do not generate
     * errors either
     */
    @Test(groups = "unit")
    public void testCoordinateArray()
    {
        // test the default torus is
        TorusGenerator generator = new TorusGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.QUADS;

        generator.generate(data);

        int vertices = data.vertexCount;
        float[] coords = data.coordinates;

        assertEquals(coords.length, vertices * 3, "Default torus coordinate length wrong");

        generator.setDimensions(TEST_INNER_RADIUS, TEST_OUTER_RADIUS);

        data.coordinates = null;
        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.coordinates;

        assertEquals(coords.length, vertices * 3, "Dimensioned torus coordinate length wrong");

        generator = new TorusGenerator(TEST_INNER_RADIUS, TEST_OUTER_RADIUS);

        data.coordinates = null;
        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.coordinates;

        assertEquals(coords.length, vertices * 3, "Test torus coordinate length wrong");
    }

    /**
     * Test that the size of the array generated for coordinates is correct.
     * This also makes sure that the calculation routines do not generate
     * errors either
     */
    @Test(groups = "unit")
    public void testNormalArray()
    {
        // test the default torus is 2, 2, 2
        TorusGenerator generator = new TorusGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.QUADS;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        generator.generate(data);

        int vertices = data.vertexCount;
        float[] coords = data.normals;

        assertEquals(coords.length, vertices * 3, "Default torus normal length wrong");


        generator.setDimensions(TEST_INNER_RADIUS, TEST_OUTER_RADIUS);

        data.normals = null;
        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.normals;

        assertEquals(coords.length, vertices * 3, "Dimensioned torus normal length wrong");

        generator = new TorusGenerator(TEST_INNER_RADIUS, TEST_OUTER_RADIUS);

        data.normals = null;
        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.normals;

        assertEquals(coords.length, vertices * 3, "Test torus normal length wrong");
    }

    @Test(groups = "unit", dataProvider = "invalid inner facets", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidInnerFacetsConstructor(int numFacets)
    {
        new TorusGenerator(numFacets, 4);
    }

    @Test(groups = "unit", dataProvider = "invalid inner facets", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidInnerFacetsSetter(int numFacets)
    {
        TorusGenerator generator = new TorusGenerator();
        generator.setFacetCount(numFacets, 4);
    }

    @Test(groups = "unit", dataProvider = "invalid outer facets", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidOuterFacetsConstructor(int numFacets)
    {
        new TorusGenerator(4, numFacets);
    }

    @Test(groups = "unit", dataProvider = "invalid outer facets", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidOuterFacetsSetter(int numFacets)
    {
        TorusGenerator generator = new TorusGenerator();
        generator.setFacetCount(4, numFacets);
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
        float[] coords;

        assertEquals(VALID_INNER_FACETS.length, VALID_OUTER_FACETS.length, "Valid inner & outer facet lengths");

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.QUADS;

        // Test with a negative value, zero and value less than 3. All should
        // generate exceptions.
        for(i = 0; i < VALID_INNER_FACETS.length; i++)
        {
            TorusGenerator generator = new TorusGenerator(VALID_INNER_FACETS[i],
                                           VALID_OUTER_FACETS[i]);

            data.coordinates = null;
            generator.generate(data);

            reqd_count = VALID_INNER_FACETS[i] * VALID_OUTER_FACETS[i] * 4;

            vtx_count = data.vertexCount;
            assertEquals(vtx_count, reqd_count, "Construct vertex count for inner facet" + VALID_INNER_FACETS[i]);

            // Now generate the vertices and look at the array
            reqd_count = reqd_count * 3;
            coords = data.coordinates;
            assertEquals(coords.length, reqd_count, "Generated initial vertex count for inner facet" + VALID_INNER_FACETS[i]);
        }


        // Same thing again but using the setFacet method
        TorusGenerator generator = new TorusGenerator();
        for(i = 0; i < VALID_INNER_FACETS.length; i++)
        {
            generator.setFacetCount(VALID_INNER_FACETS[i],
                                    VALID_OUTER_FACETS[i]);
            reqd_count = VALID_INNER_FACETS[i] * VALID_OUTER_FACETS[i] * 4;

            data.coordinates = null;
            generator.generate(data);

            vtx_count = data.vertexCount;
            assertEquals(vtx_count, reqd_count, "Construct vertex count for inner facet" + VALID_INNER_FACETS[i]);

            // Now generate the vertices and look at the array
            reqd_count = reqd_count * 3;
            coords = data.coordinates;
            assertEquals(coords.length, reqd_count, "Generated initial vertex count for inner facet" + VALID_INNER_FACETS[i]);

        }
    }

    @DataProvider(name = "invalid inner facets")
    public Object[][] generateInvalidInnerFacetsData() {
        return new Object[][] {
            { -5 },
            { 0 },
            { 2 },
            { 7 }
        };
    }

    @DataProvider(name = "invalid outer facets")
    public Object[][] generateInvalidOuterFacetsData() {
        return new Object[][] {
            { -5 },
            { 0 },
            { 2 }
        };
    }
}

