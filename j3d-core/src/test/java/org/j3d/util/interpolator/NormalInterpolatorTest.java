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

import org.j3d.maths.vector.Vector3d;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * A test case to check the functionality of the NormalInterpolator
 * implementation.
 * <p>
 *
 * The test aims to check insertion and key value generation of the
 * interpolator.
 *
 * @author Justin Couc
 * @version $Revision: 1.3 $
 */
public class NormalInterpolatorTest
{
    /** Keys to be used for testing */
    private static float[] keys = { 0.1f, 0.4f, 5f };

    /** Values to correspond to the test keys */
    private static float[][] values =
    {
        {1, 0, 0},
        {0, 1, 0},
        {0, 0, 1},
    };

    /**
     * Pre-test instance setup code. We check here to make sure that the key
     * and value arrays are the same length just in case someone has stuffed
     * it up when playing with this code.
     */
    @BeforeTest(groups = "unit")
    public void setUp()
    {
        assertEquals(keys.length, values.length, "Keys and values arrays are not the same size");
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testAddNullNormal() throws Exception
    {
        NormalInterpolator interpolator = new NormalInterpolator();
        interpolator.addKeyFrame(1.0f, null);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testAddNon3Normal() throws Exception
    {
        NormalInterpolator interpolator = new NormalInterpolator();
        interpolator.addKeyFrame(1.0f, new float[] { 0.5f, 0.2f });
    }

    /**
     * Test that we can create a basic array of values using float arrays
     * inserted linearly without generating exceptions.
     */
    @Test(groups = "unit")
    public void testCreateFloat()
    {
        int i;
        int num_keys = keys.length;

        NormalInterpolator interpolator = new NormalInterpolator();

        for(i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i], values[i]);

        // now fetch these values back again and make sure they are the same
        for(i = 0; i < num_keys; i++)
        {
            float[] vals = interpolator.floatValue(keys[i]);

            assertEquals(vals[0], values[i][0], i + " X normal not same");
            assertEquals(vals[1], values[i][1], i + " Y normal not same");
            assertEquals(vals[2], values[i][2], i + " Z normal not same");
        }
    }

    /**
     * Test that we can create an array of values with values being inserted
     * between other values.
     */
    @Test(groups = "unit")
    public void testValueInsertFloat()
    {
        int i;
        int num_keys = keys.length;

        assertTrue(num_keys > 2, "Not enough keys ( < 3) to do this test");

        NormalInterpolator interpolator = new NormalInterpolator();

        interpolator.addKeyFrame(keys[0], values[0]);
        interpolator.addKeyFrame(keys[2], values[2]);
        interpolator.addKeyFrame(keys[1], values[1]);

        // now fetch these values back again and make sure they are the same
        for(i = 0; i < num_keys; i++)
        {
            float[] vals = interpolator.floatValue(keys[i]);

            assertEquals(vals[0], values[i][0], i + " X normal not same");
            assertEquals(vals[1], values[i][1], i + " Y normal not same");
            assertEquals(vals[2], values[i][2], i + " Z normal not same");
        }
    }

    /**
     * Test that we can generate simple values for keys that are in range
     * easily. The earlier tests have made sure that we are returning the
     * right values when the key is exactly equal to one of the end values.
     * Now we are looking at a couple of normalinated points long each axis.
     */
    @Test(groups = "unit")
    public void testKeyGenFloat()
    {
        int i;
        int num_keys = keys.length;

        NormalInterpolator interpolator = new NormalInterpolator();

        for(i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i], values[i]);

        float mid_key = keys[0] + ((keys[1] - keys[0]) / 2);

        Vector3d p1 = new Vector3d();
        p1.set(values[0][0], values[0][1], values[0][2]);

        Vector3d p2 = new Vector3d();
        p2.set(values[1][0], values[1][1], values[1][2]);

        Vector3d mid_point = new Vector3d();
        mid_point.interpolate(p1, p2, 0.5);
        mid_point.normalise();

        float[] vals = interpolator.floatValue(mid_key);

        assertEquals(vals[0], mid_point.x, 0.001f, "1st X normal not same");
        assertEquals(vals[1], mid_point.y, 0.001f, "1st Y normal not same");
        assertEquals(vals[2], mid_point.z, 0.001f, "1st Z normal not same");

        mid_key = keys[1] + ((keys[2] - keys[1]) / 2);
        p1.set(values[1][0], values[1][1], values[1][2]);
        p2.set(values[2][0], values[2][1], values[2][2]);
        mid_point.interpolate(p1, p2, 0.5);
        mid_point.normalise();

        vals = interpolator.floatValue(mid_key);

        assertEquals(vals[0], mid_point.x, 0.001f, "1st X normal not same");
        assertEquals(vals[1], mid_point.y, 0.001f, "1st Y normal not same");
        assertEquals(vals[2], mid_point.z, 0.001f, "1st Z normal not same");
    }

    /**
     * Test that we can generate values that are clamped to the extent values
     * of the interpolator for keys that are out of range to those inserted.
     */
    @Test(groups = "unit")
    public void testClamping()
    {
        int i;
        int num_keys = keys.length;

        NormalInterpolator interpolator = new NormalInterpolator();

        for(i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i], values[i]);

        // a key value smaller than the smallest key
        float key = keys[0] - 1;

        float[] vals = interpolator.floatValue(key);

        assertEquals(vals[0], values[0][0], "Min X normal not same");
        assertEquals(vals[1], values[0][1], "Min Y normal not same");
        assertEquals(vals[2], values[0][2], "Min Z normal not same");

        // A key value larger than the largest key
        num_keys--;
        key = keys[num_keys] + 1;

        vals = interpolator.floatValue(key);

        assertEquals(vals[0], values[num_keys][0], "Max X normal not same");
        assertEquals(vals[1], values[num_keys][1], "Max Y normal not same");
        assertEquals(vals[2], values[num_keys][2], "Max Z normal not same");
    }
}

