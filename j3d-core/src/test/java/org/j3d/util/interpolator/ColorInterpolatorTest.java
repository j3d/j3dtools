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

package org.j3d.util.interpolator;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * A test case to check the functionality of the ColorInterpolator
 * implementation.
 * <p>
 *
 * The test aims to check insertion and key value generation of the
 * interpolator.
 *
 * @author Justin Couc
 * @version $Revision: 1.3 $
 */
public class ColorInterpolatorTest
{
    /** Keys to be used for testing */
    private static float[] keys = { 0.1f, 0.4f, 5f, 10f };

    /** Values to correspond to the test keys */
    private static float[][] rgbValues =
    {
        {1, 0, 0, 1},
        {0, 1, 0, 1},
        {0, 0, 1, 1},
        {1, 1, 1, 0},
    };

    private static float[][] hsvValues =
    {
        {Float.NaN, 0, 0, 1},
        {0, 1, 0, 1},
        {Float.NaN, 0, 1, 1},
        {Float.NaN, 1, 1, 0},
    };

    /**
     * Pre-test instance setup code. We check here to make sure that the key
     * and value arrays are the same length just in case someone has stuffed
     * it up when playing with this code.
     */
    @BeforeTest(groups = "unit")
    public void setUp()
    {
        assertEquals(keys.length, rgbValues.length, "Keys and rgbValues arrays are not the same size");
    }

    /**
     * Test that we can create a basic array of rgbValues using float arrays
     * inserted linearly without generating exceptions.
     */
    @Test(groups = "unit")
    public void testCreateRGBFloat()
    {
        int i;
        int num_keys = keys.length;

        ColorInterpolator interpolator = new ColorInterpolator();

        for(i = 0; i < num_keys; i++)
            interpolator.addRGBKeyFrame(keys[i], rgbValues[i][0], rgbValues[i][1], rgbValues[i][2], rgbValues[i][3]);

        // now fetch these rgbValues back again and make sure they are the same
        for(i = 0; i < num_keys; i++)
        {
            float[] vals = interpolator.floatRGBValue(keys[i]);

            assertEquals(vals[0], rgbValues[i][0], i + " Red component not same");
            assertEquals(vals[1], rgbValues[i][1], i + " Green component not same");
            assertEquals(vals[2], rgbValues[i][2], i + " Blue component not same");
        }
    }

    /**
     * Test that we can create an array of rgbValues with rgbValues being inserted
     * between other rgbValues.
     */
    @Test(groups = "unit")
    public void testValueInsertRGBFloat()
    {
        int i;
        int num_keys = keys.length;

        assertTrue(num_keys > 2, "Not enough keys ( < 3) to do this test");

        ColorInterpolator interpolator = new ColorInterpolator();

        interpolator.addRGBKeyFrame(keys[0], rgbValues[0][0], rgbValues[0][1], rgbValues[0][2], rgbValues[0][3]);
        interpolator.addRGBKeyFrame(keys[2], rgbValues[2][0], rgbValues[2][1], rgbValues[2][2], rgbValues[2][3]);
        interpolator.addRGBKeyFrame(keys[3], rgbValues[3][0], rgbValues[3][1], rgbValues[3][2], rgbValues[3][3]);
        interpolator.addRGBKeyFrame(keys[1], rgbValues[1][0], rgbValues[1][1], rgbValues[1][2], rgbValues[1][3]);

        // now fetch these rgbValues back again and make sure they are the same
        for(i = 0; i < num_keys; i++)
        {
            float[] vals = interpolator.floatRGBValue(keys[i]);

            assertEquals(vals[0], rgbValues[i][0], "Red component at " + i + " not same");
            assertEquals(vals[1], rgbValues[i][1], "Green component at " + i + " not same");
            assertEquals(vals[2], rgbValues[i][2], "Blue component at " + i + " not same");
            assertEquals(vals[3], rgbValues[i][3], "Alpha component at " + i + " not same");
        }
    }

    /**
     * Test that we can generate simple rgbValues for keys that are in range
     * easily. The earlier tests have made sure that we are returning the
     * right rgbValues when the key is exactly equal to one of the end rgbValues.
     * Now we are looking at a couple of normalinated points long each axis.
     */
    @Test(groups = "unit")
    public void testKeyGenRGBFloat()
    {
        int i;
        int num_keys = keys.length;

        ColorInterpolator interpolator = new ColorInterpolator();

        for(i = 0; i < num_keys; i++)
            interpolator.addRGBKeyFrame(keys[i], rgbValues[i][0], rgbValues[i][1], rgbValues[i][2], rgbValues[i][3]);

        float mid_key = keys[0] + ((keys[1] - keys[0]) / 2);

        float r_val = rgbValues[0][0] + ((rgbValues[1][0] - rgbValues[0][0]) / 2);
        float g_val = rgbValues[0][1] + ((rgbValues[1][1] - rgbValues[0][1]) / 2);
        float b_val = rgbValues[0][2] + ((rgbValues[1][2] - rgbValues[0][2]) / 2);
        float a_val = rgbValues[0][3] + ((rgbValues[1][3] - rgbValues[0][3]) / 2);

        float[] vals = interpolator.floatRGBValue(mid_key);

        assertEquals(vals[0], r_val, 0.001f, "1st red component not same");
        assertEquals(vals[1], g_val, 0.001f, "1st green component not same");
        assertEquals(vals[2], b_val, 0.001f, "1st blue component not same");
        assertEquals(vals[3], a_val, 0.001f, "1st alpha component not same");

        mid_key = keys[2] + ((keys[3] - keys[2]) / 2);
        r_val = rgbValues[2][0] + ((rgbValues[3][0] - rgbValues[2][0]) / 2);
        g_val = rgbValues[2][1] + ((rgbValues[3][1] - rgbValues[2][1]) / 2);
        b_val = rgbValues[2][2] + ((rgbValues[3][2] - rgbValues[2][2]) / 2);
        a_val = rgbValues[2][3] + ((rgbValues[3][3] - rgbValues[2][3]) / 2);

        vals = interpolator.floatRGBValue(mid_key);

        assertEquals(vals[0], r_val, 0.001f, "Last red component not same");
        assertEquals(vals[1], g_val, 0.001f, "Last green component not same");
        assertEquals(vals[2], b_val, 0.001f, "Last blue component not same");
        assertEquals(vals[3], a_val, 0.001f, "Last alpha component not same");
    }

    /**
     * Test that we can generate rgbValues that are clamped to the extent rgbValues
     * of the interpolator for keys that are out of range to those inserted.
     */
    @Test(groups = "unit")
    public void testRGBClamping()
    {
        int i;
        int num_keys = keys.length;

        ColorInterpolator interpolator = new ColorInterpolator();

        for(i = 0; i < num_keys; i++)
            interpolator.addRGBKeyFrame(keys[i], rgbValues[i][0], rgbValues[i][1], rgbValues[i][2], rgbValues[i][3]);

        // a key value smaller than the smallest key
        float key = keys[0] - 1;

        float[] vals = interpolator.floatRGBValue(key);

        assertEquals(vals[0], rgbValues[0][0], "Min Red component not same");
        assertEquals(vals[1], rgbValues[0][1], "Min Green component not same");
        assertEquals(vals[2], rgbValues[0][2], "Min Blue component not same");

        // A key value larger than the largest key
        num_keys--;
        key = keys[num_keys] + 1;

        vals = interpolator.floatRGBValue(key);

        assertEquals(vals[0], rgbValues[num_keys][0], "Max Red component not same");
        assertEquals(vals[1], rgbValues[num_keys][1], "Max Green component not same");
        assertEquals(vals[2], rgbValues[num_keys][2], "Max Blue component not same");
    }

    /**
     * Test that we can create a basic array of hsvValues using float arrays
     * inserted linearly without generating exceptions.
     */
    @Test(groups = "unit")
    public void testCreateHSVFloat()
    {
        int i;
        int num_keys = keys.length;

        ColorInterpolator interpolator = new ColorInterpolator(4, ColorInterpolator.HSV_SPACE);

        for(i = 0; i < num_keys; i++)
            interpolator.addHSVKeyFrame(keys[i], hsvValues[i][0], hsvValues[i][1], hsvValues[i][2], hsvValues[i][3]);

        // now fetch these hsvValues back again and make sure they are the same
        for(i = 0; i < num_keys; i++)
        {
            float[] vals = interpolator.floatHSVValue(keys[i]);

            assertEquals(vals[0], hsvValues[i][0], i + " Red component not same");
            assertEquals(vals[1], hsvValues[i][1], i + " Green component not same");
            assertEquals(vals[2], hsvValues[i][2], i + " Blue component not same");
        }
    }

    /**
     * Test that we can create an array of hsvValues with hsvValues being inserted
     * between other hsvValues.
     */
    @Test(groups = "unit")
    public void testValueInsertHSVFloat()
    {
        int i;
        int num_keys = keys.length;

        assertTrue(num_keys > 2, "Not enough keys ( < 3) to do this test");

        ColorInterpolator interpolator = new ColorInterpolator(4, ColorInterpolator.HSV_SPACE);

        interpolator.addHSVKeyFrame(keys[0], hsvValues[0][0], hsvValues[0][1], hsvValues[0][2], hsvValues[0][3]);
        interpolator.addHSVKeyFrame(keys[2], hsvValues[2][0], hsvValues[2][1], hsvValues[2][2], hsvValues[2][3]);
        interpolator.addHSVKeyFrame(keys[3], hsvValues[3][0], hsvValues[3][1], hsvValues[3][2], hsvValues[3][3]);
        interpolator.addHSVKeyFrame(keys[1], hsvValues[1][0], hsvValues[1][1], hsvValues[1][2], hsvValues[1][3]);

        // now fetch these hsvValues back again and make sure they are the same
        for(i = 0; i < num_keys; i++)
        {
            float[] vals = interpolator.floatHSVValue(keys[i]);

            assertEquals(vals[0], hsvValues[i][0], "Red component at " + i + " not same");
            assertEquals(vals[1], hsvValues[i][1], "Green component at " + i + " not same");
            assertEquals(vals[2], hsvValues[i][2], "Blue component at " + i + " not same");
            assertEquals(vals[3], hsvValues[i][3], "Alpha component at " + i + " not same");
        }
    }

    /**
     * Test that we can generate simple hsvValues for keys that are in range
     * easily. The earlier tests have made sure that we are returning the
     * right hsvValues when the key is exactly equal to one of the end hsvValues.
     * Now we are looking at a couple of normalinated points long each axis.
     */
    @Test(groups = "unit")
    public void testKeyGenHSVFloat()
    {
        int i;
        int num_keys = keys.length;

        ColorInterpolator interpolator = new ColorInterpolator(4, ColorInterpolator.HSV_SPACE);

        for(i = 0; i < num_keys; i++)
            interpolator.addHSVKeyFrame(keys[i], hsvValues[i][0], hsvValues[i][1], hsvValues[i][2], hsvValues[i][3]);

        float mid_key = keys[0] + ((keys[1] - keys[0]) / 2);

        float h1 = Float.isNaN(hsvValues[0][0]) ? 0.0f:  hsvValues[0][0];
        float h2 = Float.isNaN(hsvValues[1][0]) ? 0.0f:  hsvValues[1][0];

        float h_val = h1 + ((h2 - h1) / 2);
        float s_val = hsvValues[0][1] + ((hsvValues[1][1] - hsvValues[0][1]) / 2);
        float v_val = hsvValues[0][2] + ((hsvValues[1][2] - hsvValues[0][2]) / 2);
        float a_val = hsvValues[0][3] + ((hsvValues[1][3] - hsvValues[0][3]) / 2);

        float[] vals = interpolator.floatHSVValue(mid_key);

        assertEquals(vals[0], h_val, 0.001f, "1st red component not same");
        assertEquals(vals[1], s_val, 0.001f, "1st green component not same");
        assertEquals(vals[2], v_val, 0.001f, "1st blue component not same");
        assertEquals(vals[3], a_val, 0.001f, "1st alpha component not same");

        mid_key = keys[2] + ((keys[3] - keys[2]) / 2);

        s_val = hsvValues[2][1] + ((hsvValues[3][1] - hsvValues[2][1]) / 2);
        v_val = hsvValues[2][2] + ((hsvValues[3][2] - hsvValues[2][2]) / 2);
        a_val = hsvValues[2][3] + ((hsvValues[3][3] - hsvValues[2][3]) / 2);

        vals = interpolator.floatHSVValue(mid_key);

        assertTrue(Float.isNaN(vals[0]), "Last red component not NaN");
        assertEquals(vals[1], s_val, 0.001f, "Last green component not same");
        assertEquals(vals[2], v_val, 0.001f, "Last blue component not same");
        assertEquals(vals[3], a_val, 0.001f, "Last alpha component not same");
    }

    /**
     * Test that we can generate hsvValues that are clamped to the extent hsvValues
     * of the interpolator for keys that are out of range to those inserted.
     */
    @Test(groups = "unit")
    public void testHSVClamping()
    {
        int i;
        int num_keys = keys.length;

        ColorInterpolator interpolator = new ColorInterpolator(4, ColorInterpolator.HSV_SPACE);

        for(i = 0; i < num_keys; i++)
            interpolator.addHSVKeyFrame(keys[i], hsvValues[i][0], hsvValues[i][1], hsvValues[i][2], hsvValues[i][3]);

        // a key value smaller than the smallest key
        float key = keys[0] - 1;

        float[] vals = interpolator.floatHSVValue(key);

        assertEquals(vals[0], hsvValues[0][0], "Min Red component not same");
        assertEquals(vals[1], hsvValues[0][1], "Min Green component not same");
        assertEquals(vals[2], hsvValues[0][2], "Min Blue component not same");

        // A key value larger than the largest key
        num_keys--;
        key = keys[num_keys] + 1;

        vals = interpolator.floatHSVValue(key);

        assertEquals(vals[0], hsvValues[num_keys][0], "Max Red component not same");
        assertEquals(vals[1], hsvValues[num_keys][1], "Max Green component not same");
        assertEquals(vals[2], hsvValues[num_keys][2], "Max Blue component not same");
    }
}

