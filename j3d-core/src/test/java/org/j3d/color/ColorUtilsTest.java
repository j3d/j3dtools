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

package org.j3d.color;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for the colour conversion utilities
 *
 * @author justin
 */
public class ColorUtilsTest
{
    @Test(groups = "unit", dataProvider = "test colours")
    public void testHSVRoundTrip(float r, float g, float b) throws Exception
    {
        float[] TEST_COLOUR = { r, g, b };
        float[] intermediate = new float[3];
        float[] result = new float[3];

        ColorUtils.convertRGBtoHSV(TEST_COLOUR, intermediate);
        ColorUtils.convertHSVtoRGB(intermediate, result);

        assertEquals(result[0], TEST_COLOUR[0], 0.01, "Red component conversion failed");
        assertEquals(result[1], TEST_COLOUR[1], 0.01, "Green component conversion failed");
        assertEquals(result[2], TEST_COLOUR[2], 0.01, "Blue component conversion failed");
    }

    @Test(groups = "unit", dataProvider = "test colours")
    public void testYUVRoundTrip(float r, float g, float b) throws Exception
    {
        float[] TEST_COLOUR = { r, g, b };
        float[] intermediate = new float[3];
        float[] result = new float[3];

        ColorUtils.convertRGBtoYUV(TEST_COLOUR, intermediate);
        ColorUtils.convertYUVtoRGB(intermediate, result);

        assertEquals(result[0], TEST_COLOUR[0], 0.01, "Red component conversion failed");
        assertEquals(result[1], TEST_COLOUR[1], 0.01, "Green component conversion failed");
        assertEquals(result[2], TEST_COLOUR[2], 0.01, "Blue component conversion failed");
    }


    @Test(groups = "unit", dataProvider = "test colours")
    public void testYIQRoundTrip(float r, float g, float b) throws Exception
    {
        float[] TEST_COLOUR = { r, g, b };
        float[] intermediate = new float[3];
        float[] result = new float[3];

        ColorUtils.convertRGBtoYIQ(TEST_COLOUR, intermediate);
        ColorUtils.convertYIQtoRGB(intermediate, result);

        assertEquals(result[0], TEST_COLOUR[0], 0.01, "Red component conversion failed");
        assertEquals(result[1], TEST_COLOUR[1], 0.01, "Green component conversion failed");
        assertEquals(result[2], TEST_COLOUR[2], 0.01, "Blue component conversion failed");
    }

    @DataProvider(name = "test colours")
    public Object[][] generateTestColourData()
    {
        return new Object[][]
        {
            { 0.0f, 0.0f, 0.0f },
            { 1.0f, 1.0f, 1.0f },
            { 1.0f, 0.0f, 0.0f },
            { 0.0f, 1.0f, 0.0f },
            { 0.0f, 0.0f, 1.0f },

            { 0.5f, 0.2f, 0.75f }
        };
    }
}
