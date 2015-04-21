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

package org.j3d.loaders;

import java.awt.image.BufferedImage;

import org.j3d.terrain.TerrainData;
import org.j3d.util.interpolator.ColorInterpolator;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * Unit tests for the height map terrain source.
 *
 * @author justin
 */
public class HeightMapTerrainDataTest
{
    @Mock
    private HeightMapSource mockHeightMapSource;

    @BeforeMethod(groups = "unit")
    public void setupTest() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        final float[] TEST_GRID_STEP_SIZE = { 1, 1 };
        final float[][] TEST_HEIGHTS = { { 1, 2 }, { 3, 4 } };

        when(mockHeightMapSource.getGridStep()).thenReturn(TEST_GRID_STEP_SIZE);
        when(mockHeightMapSource.getHeights()).thenReturn(TEST_HEIGHTS);

        HeightMapTerrainData classUnderTest = new HeightMapTerrainData(mockHeightMapSource);

        assertEquals(classUnderTest.getSourceDataType(), TerrainData.STATIC_DATA, "Incorrect data type");
        assertFalse(classUnderTest.hasColor(), "Colour should not be available");
        assertFalse(classUnderTest.hasTexture(), "Texture should not be available");
        assertNull(classUnderTest.getTexture(), "No texture should be given by default");
        assertEquals(classUnderTest.getGridXStep(), (double) TEST_GRID_STEP_SIZE[0], "Wrong X step saved");
        assertEquals(classUnderTest.getGridYStep(), (double) TEST_GRID_STEP_SIZE[1], "Wrong Y step saved");

        ColorInterpolator testInterp = new ColorInterpolator();
        classUnderTest.setColorInterpolator(testInterp);

        assertTrue(classUnderTest.hasColor(), "Didn't find colour handler after set");

        classUnderTest.setColorInterpolator(null);

        assertFalse(classUnderTest.hasColor(), "Colour handler not removed properly");

        BufferedImage testImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        classUnderTest.setTexture(testImage);

        assertTrue(classUnderTest.hasTexture(), "Texture not registered after set");
        classUnderTest.setTexture(null);

        assertFalse(classUnderTest.hasTexture(), "Texture flag not cleared correctly");
        assertNull(classUnderTest.getTexture(), "Texture image not removed");
    }

    @Test(groups = "unit")
    public void testCoordinateFetching() throws Exception
    {
        final float[] TEST_GRID_STEP_SIZE = { 1, 1 };
        final float[][] TEST_HEIGHTS = { { 1, 2 }, { 3, 4 } };

        when(mockHeightMapSource.getGridStep()).thenReturn(TEST_GRID_STEP_SIZE);
        when(mockHeightMapSource.getHeights()).thenReturn(TEST_HEIGHTS);

        HeightMapTerrainData classUnderTest = new HeightMapTerrainData(mockHeightMapSource);

        float rnd_r = (float)Math.random();
        float rnd_g = (float)Math.random();
        float rnd_b = (float)Math.random();

        float[] resultCoord = new float[3];
        float[] resultTex = new float[2];
        float[] resultColor = { rnd_r, rnd_g, rnd_b };

        float[] expectedColor = { rnd_r, rnd_g, rnd_b };

        // Check the basic grid spot heights are correct and also check that we don't
        // overwrite the provided colour array if there was no colour interpolator set
        for(int i = 0; i < TEST_HEIGHTS.length; i++)
        {
            for(int j = 0; j < TEST_HEIGHTS[i].length; j++)
            {
                assertEquals(classUnderTest.getHeightFromGrid(i, j),
                             TEST_HEIGHTS[i][j],
                             "Data at " + i + "," + j + " is incorrect");

                classUnderTest.getCoordinate(resultCoord, i, j);

                assertEquals(resultCoord[1],
                             TEST_HEIGHTS[i][j],
                             "Basic coordinate height at " + i + "," + j + " is incorrect");

                classUnderTest.getCoordinateWithColor(resultCoord, resultColor, i, j);

                assertEquals(resultCoord[1],
                             TEST_HEIGHTS[i][j],
                             "Color coordinate height at " + i + "," + j + " is incorrect");
                checkColour(resultColor, expectedColor, "Color coordinate", i, j);

                classUnderTest.getCoordinateWithTexture(resultCoord, resultTex, i, j, 0, 0);

                assertEquals(resultCoord[1],
                             TEST_HEIGHTS[i][j],
                             "Texture coordinate height at " + i + "," + j + " is incorrect");

                classUnderTest.getCoordinate(resultCoord, resultTex, resultColor, i, j);

                assertEquals(resultCoord[1],
                             TEST_HEIGHTS[i][j],
                             "All parts coordinate height at " + i + "," + j + " is incorrect");

                checkColour(resultColor, expectedColor, "All parts", i, j);
            }
        }
    }

    @Test(groups = "unit")
    public void testCoordinateFetchingWithColour() throws Exception
    {
        final float[] TEST_GRID_STEP_SIZE = { 1, 1 };
        final float[][] TEST_HEIGHTS = { { 1, 2 }, { 3, 4 } };
        final float[][][] TEST_COLOURS = new float[2][2][3];

        ColorInterpolator testInterp = new ColorInterpolator();

        for(int i = 0; i < TEST_COLOURS.length; i++)
        {
            for(int j = 0; j < TEST_COLOURS[i].length; j++)
            {
                for(int k = 0; k < TEST_COLOURS[i][j].length; k++)
                {
                    TEST_COLOURS[i][j][k] = (float)Math.random();
                }

                testInterp.addRGBKeyFrame(TEST_HEIGHTS[i][j],
                                          TEST_COLOURS[i][j][0],
                                          TEST_COLOURS[i][j][1],
                                          TEST_COLOURS[i][j][2],
                                          1.0f);
            }
        }

        when(mockHeightMapSource.getGridStep()).thenReturn(TEST_GRID_STEP_SIZE);
        when(mockHeightMapSource.getHeights()).thenReturn(TEST_HEIGHTS);

        HeightMapTerrainData classUnderTest = new HeightMapTerrainData(mockHeightMapSource);
        classUnderTest.setColorInterpolator(testInterp);

        float[] resultCoord = new float[3];
        float[] resultTex = new float[2];
        float[] resultColor = new float[3];

        // Check the basic grid spot heights are correct and also check that we don't
        // overwrite the provided colour array if there was no colour interpolator set
        for(int i = 0; i < TEST_HEIGHTS.length; i++)
        {
            for(int j = 0; j < TEST_HEIGHTS[i].length; j++)
            {
                classUnderTest.getCoordinateWithColor(resultCoord, resultColor, i, j);

                assertEquals(resultCoord[1],
                             TEST_HEIGHTS[i][j],
                             "Color coordinate height at " + i + "," + j + " is incorrect");
                checkColour(resultColor, TEST_COLOURS[i][j], "Color coordinate", i, j);

                classUnderTest.getCoordinate(resultCoord, resultTex, resultColor, i, j);

                assertEquals(resultCoord[1],
                             TEST_HEIGHTS[i][j],
                             "All parts coordinate height at " + i + "," + j + " is incorrect");

                checkColour(resultColor, TEST_COLOURS[i][j], "All parts", i, j);
            }
        }
    }

    @Test(groups = "unit")
    public void testHeightInterpolation() throws Exception
    {
        final float[] TEST_GRID_STEP_SIZE = { 1.5f, 1.5f };
        final float[][] TEST_HEIGHTS = { { 1, 2 }, { 2, 1 } };

        when(mockHeightMapSource.getGridStep()).thenReturn(TEST_GRID_STEP_SIZE);
        when(mockHeightMapSource.getHeights()).thenReturn(TEST_HEIGHTS);

        HeightMapTerrainData classUnderTest = new HeightMapTerrainData(mockHeightMapSource);

        // Check the basic grid spot heights base on the grid step size. Should give the same height
        // as the grid coordinates
        for(int i = 0; i < TEST_HEIGHTS.length; i++)
        {
            for(int j = 0; j < TEST_HEIGHTS[i].length; j++)
            {
                float result = classUnderTest.getHeight(i * TEST_GRID_STEP_SIZE[0], j * TEST_GRID_STEP_SIZE[1]);

                assertEquals(result,
                             TEST_HEIGHTS[i][j],
                             "Basic coordinate height at " + (i * TEST_GRID_STEP_SIZE[0]) +
                                 "," + (j * TEST_GRID_STEP_SIZE[1])+ " is incorrect");

            }
        }

        // Exact mid grid test gives different answer depending on whether we build a peak
        // or trough with high or low points of the quad
        for(int i = 0; i < TEST_HEIGHTS.length - 1; i++)
        {
            for(int j = 0; j < TEST_HEIGHTS[i].length - 1; j++)
            {
                float result = classUnderTest.getHeight(i * TEST_GRID_STEP_SIZE[0] + 0.5f * TEST_GRID_STEP_SIZE[0],
                                                        j * TEST_GRID_STEP_SIZE[1] + 0.5f * TEST_GRID_STEP_SIZE[1]);

                // Set up the two possible outcomes, depending on whether we start with the
                // x-axis or the z-axis as the initial one we interpolate across (peak v trough)
                float h1 = TEST_HEIGHTS[i][j];
                float h2 = TEST_HEIGHTS[i][j + 1];
                float h3 = TEST_HEIGHTS[i + 1][j];
                float h4 = TEST_HEIGHTS[i + 1][j + 1];

                float hx_a = h1 + (h2 - h1) * 0.5f;
                float hx_b = h3 + (h4 - h3) * 0.5f;

                float hy_a = h1 + (h3 - h1) * 0.5f;
                float hy_b = h2 + (h4 - h2) * 0.5f;

                float expected_option_1 = hx_a + (hx_b - hx_a) * 0.5f;
                float expected_option_2 = hy_a + (hy_b - hy_a) * 0.5f;

                assertTrue(equal(result, expected_option_1, 0.0001f) || equal(result, expected_option_2, 0.0001f),
                           "Basic coordinate height at " + (i * TEST_GRID_STEP_SIZE[0]) +
                               "," + (j * TEST_GRID_STEP_SIZE[1]) + " is incorrect. Expected " +
                               expected_option_1 + " or " + expected_option_2 + ", found " + result);

            }
        }

    }

    private boolean equal(float actual, float expected, float delta)
    {
        float diff = actual - expected;

        return Math.abs(diff) < delta;
    }

    /**
     * Convenience method to check the colours returned against expected.
     */
    private void checkColour(float[] result,
                             float[] expected,
                             String prefixMsg,
                             int gridXIndex,
                             int gridYIndex)
    {
        assertEquals(result[0],
                     expected[0],
                     0.01f,
                     prefixMsg + " red at " + gridXIndex + "," + gridYIndex + " is incorrect");
        assertEquals(result[1],
                     expected[1],
                     0.01f,
                     prefixMsg + " blue at " + gridXIndex + "," + gridYIndex + " is incorrect");
        assertEquals(result[2],
                     expected[2],
                     0.01f,
                     prefixMsg + " blue at " + gridXIndex + "," + gridYIndex + " is incorrect");

    }
}
