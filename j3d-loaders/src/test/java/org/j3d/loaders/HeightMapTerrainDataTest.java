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

        assertNull(classUnderTest.getTexture(), "No texture should be given by default");
    }

    @Test(groups = "unit")
    public void testCoordinateFetching() throws Exception
    {
        final float[] TEST_GRID_STEP_SIZE = { 1, 1 };
        final float[][] TEST_HEIGHTS = { { 1, 2 }, { 3, 4 } };

        when(mockHeightMapSource.getGridStep()).thenReturn(TEST_GRID_STEP_SIZE);
        when(mockHeightMapSource.getHeights()).thenReturn(TEST_HEIGHTS);

        HeightMapTerrainData classUnderTest = new HeightMapTerrainData(mockHeightMapSource);

        float[] resultCoord = new float[3];
        float[] resultTex = new float[2];
        float[] resultColor = new float[3];

        // Check the basic grid spot heights are correct
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

                classUnderTest.getCoordinateWithTexture(resultCoord, resultTex, i, j, 0, 0);

                assertEquals(resultCoord[1],
                             TEST_HEIGHTS[i][j],
                             "Texture coordinate height at " + i + "," + j + " is incorrect");

                classUnderTest.getCoordinate(resultCoord, resultTex, resultColor, i, j);

                assertEquals(resultCoord[1],
                             TEST_HEIGHTS[i][j],
                             "All parts coordinate height at " + i + "," + j + " is incorrect");
            }
        }
    }
}
