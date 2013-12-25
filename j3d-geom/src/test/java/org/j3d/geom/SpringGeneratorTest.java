/*****************************************************************************
 *                      J3D.org Copyright (c) 2000
 *                           Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom;

// Standard imports

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * A test case to check the functionality of the SpringGenerator implementation.
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
public class SpringGeneratorTest
{
    /** A non-standard inner radius for testing */
    private static final float TEST_INNER_RADIUS = 0.5f;

    /** A non-standard outer radius for testing */
    private static final float TEST_OUTER_RADIUS = 6.3f;

    /** A non-standard loop spacing for testing */
    private static final float TEST_LOOP_SPACING = 3.3f;

    /** A non-standard loop count for testing */
    private static final int TEST_LOOP_COUNT = 7;

    /** A list of valid inner facet counts to make sure it generates correctly */
    private static final int[] VALID_INNER_FACETS = { 12, 32, 72 };

    /** A list of valid outer facet counts to make sure it generates correctly */
    private static final int[] VALID_OUTER_FACETS = { 10, 20, 32 };

    /**
     * Test that we can create the Spring generator with different constructors
     * and have it give us the right information for vertex counts.
     */
    @Test(groups = "unit")
    public void testCreate()
    {
        // test the default spring is 1 outer radius. This should give
        // sides: 16 facets * 16 facets * 4 vertex per facet * 4 loops
        // total => 4096 vertices
        SpringGenerator generator = new SpringGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.QUADS;

        generator.generate(data);

        assertEquals(data.vertexCount, 4096, "Default spring vertex count");

        float[] dimensions = generator.getDimensions();

        assertEquals(dimensions[0], 0.25f, "Default spring inner radius");
        assertEquals(dimensions[1], 1.0f, "Default spring outer radius");
        assertEquals(dimensions[2], 1.0f, "Default loop spacing");
        assertEquals((int)dimensions[3], 4, "Default loop count");

        // Now test changing the dimension on an existing spring
        generator.setDimensions(TEST_INNER_RADIUS, TEST_OUTER_RADIUS);

        data.coordinates = null;
        generator.generate(data);

        assertEquals(data.vertexCount, 4096, "Dimensioned vertex count");

        dimensions = generator.getDimensions();

        assertEquals(dimensions[0], TEST_INNER_RADIUS, "Dimensioned spring outer radius");
        assertEquals(dimensions[1], TEST_OUTER_RADIUS, "Dimensioned spring inner radius");
        assertEquals(dimensions[2], 1.0f, "Dimensioned loop spacing");
        assertEquals((int)dimensions[3], 4, "Dimensioned loop count");

        // Now try modifying the loop count
        // sides: 16 facets * 16 facets * 4 vertex per facet * 7 loops
        // total => 7168 vertices
        generator.setLoopDimensions(TEST_LOOP_SPACING, TEST_LOOP_COUNT);

        data.coordinates = null;
        generator.generate(data);

        assertEquals(data.vertexCount, 7168, "Looped spring vertex count is wrong");

        dimensions = generator.getDimensions();

        assertEquals(dimensions[0], TEST_INNER_RADIUS, "Looped spring outer radius");
        assertEquals(dimensions[1], TEST_OUTER_RADIUS, "Looped spring inner radius");
        assertEquals(dimensions[2], TEST_LOOP_SPACING, "Looped loop spacing");
        assertEquals((int)dimensions[3], TEST_LOOP_COUNT, "Looped loop count");

        // test the non-standard spring size
        generator = new SpringGenerator(TEST_INNER_RADIUS,
                                        TEST_OUTER_RADIUS,
                                        TEST_LOOP_SPACING,
                                        TEST_LOOP_COUNT);

        data.coordinates = null;
        generator.generate(data);

        assertEquals(data.vertexCount, 7168, "Test spring vertex count is wrong");

        dimensions = generator.getDimensions();

        assertEquals(dimensions[0], TEST_INNER_RADIUS, "Test spring outer radius");
        assertEquals(dimensions[1], TEST_OUTER_RADIUS, "Test spring inner radius");
        assertEquals(dimensions[2], TEST_LOOP_SPACING, "Test loop spacing");
        assertEquals((int)dimensions[3], TEST_LOOP_COUNT, "Test loop count");
    }

    /**
     * Test that the size of the array generated for coordinates is correct.
     * This also makes sure that the calculation routines do not generate
     * errors either
     */
    @Test(groups = "unit")
    public void testCoordinateArray()
    {
        // test the default spring is
        SpringGenerator generator = new SpringGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.QUADS;

        generator.generate(data);

        int vertices = data.vertexCount;
        float[] coords = data.coordinates;

        assertEquals(coords.length, vertices * 3, "Default spring coordinate length wrong");

        generator.setDimensions(TEST_INNER_RADIUS, TEST_OUTER_RADIUS);

        data.coordinates = null;
        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.coordinates;

        assertEquals(coords.length, vertices * 3, "Dimensioned spring coordinate length wrong");

        generator = new SpringGenerator(TEST_INNER_RADIUS, TEST_OUTER_RADIUS);

        data.coordinates = null;
        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.coordinates;

        assertEquals(coords.length, vertices * 3, "Test spring coordinate length wrong");
    }

    /**
     * Test that the size of the array generated for coordinates is correct.
     * This also makes sure that the calculation routines do not generate
     * errors either
     */
    @Test(groups = "unit")
    public void testNormalArray()
    {
        // test the default spring is 2, 2, 2
        SpringGenerator generator = new SpringGenerator();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.QUADS;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        generator.generate(data);

        int vertices = data.vertexCount;
        float[] coords = data.normals;

        assertEquals(coords.length, vertices * 3, "Default spring normal length wrong");


        generator.setDimensions(TEST_INNER_RADIUS, TEST_OUTER_RADIUS);

        data.normals = null;
        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.normals;

        assertEquals(coords.length, vertices * 3, "Dimensioned spring normal length wrong");

        generator = new SpringGenerator(TEST_INNER_RADIUS, TEST_OUTER_RADIUS);

        data.normals = null;
        generator.generate(data);

        vertices = data.vertexCount;
        coords = data.normals;

        assertEquals(coords.length, vertices * 3, "Test spring normal length wrong");
    }

    @Test(groups = "unit", dataProvider = "invalid inner facets", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidInnerFacetsConstructor(int numFacets)
    {
        new SpringGenerator(numFacets, 4);
    }

    @Test(groups = "unit", dataProvider = "invalid inner facets", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidInnerFacetsSetter(int numFacets)
    {
        SpringGenerator generator = new SpringGenerator();
        generator.setFacetCount(numFacets, 4);
    }

    @Test(groups = "unit", dataProvider = "invalid outer facets", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidOuterFacetsConstructor(int numFacets)
    {
        new SpringGenerator(4, numFacets);
    }

    @Test(groups = "unit", dataProvider = "invalid outer facets", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidOuterFacetsSetter(int numFacets)
    {
        SpringGenerator generator = new SpringGenerator();
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

        SpringGenerator generator = new SpringGenerator();
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.QUADS;

        generator.generate(data);

        assertEquals(VALID_INNER_FACETS.length, VALID_OUTER_FACETS.length, "Valid inner & outer facet lengths");

        // Test with a negative value, zero and value less than 3. All should
        // generate exceptions.
        for(i = 0; i < VALID_INNER_FACETS.length; i++)
        {
            generator = new SpringGenerator(VALID_INNER_FACETS[i], VALID_OUTER_FACETS[i]);
            // Facet counts * 4 vertex per facet * 4 loops
            reqd_count = VALID_INNER_FACETS[i] * VALID_OUTER_FACETS[i] * 16;

            data.coordinates = null;
            generator.generate(data);

            vtx_count = data.vertexCount;
            assertEquals(vtx_count, reqd_count, "Construct vertex count for inner facet " + VALID_INNER_FACETS[i]);

            // Now generate the vertices and look at the array
            reqd_count = reqd_count * 3;
            coords = data.coordinates;
            assertEquals(coords.length, reqd_count, "Generated initial vertex count for inner facet " + VALID_INNER_FACETS[i]);
        }


        // Same thing again but using the setFacet method
        generator = new SpringGenerator();
        for(i = 0; i < VALID_INNER_FACETS.length; i++)
        {
            generator.setFacetCount(VALID_INNER_FACETS[i],
                                    VALID_OUTER_FACETS[i]);
            // Facet counts * 4 vertex per facet * 4 loops

            reqd_count = VALID_INNER_FACETS[i] * VALID_OUTER_FACETS[i] * 16;
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
            { 10 }
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

