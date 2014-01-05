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
 * A test case to check the functionality of the BooleanInterpolator
 * implementation.
 * <p>
 *
 * The test aims to check insertion and key value generation of the
 * interpolator.
 *
 * @author Justin Couc
 * @version $Revision: 1.3 $
 */
public class BooleanInterpolatorTest
{
    /** Keys to be used for testing */
    private static float[] keys = { 0.1f, 0.4f, 5f };

    /** Values to correspond to the test keys */
    private static boolean[] values = { true, false, true };
                                                     
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

    /**
     * Test that we can create a basic array of values using float arrays
     * inserted linearly without generating exceptions.
     */
    @Test(groups = "unit")
    public void testCreateFloat()
    {
        int i;
        int num_keys = keys.length;

        BooleanInterpolator interpolator = new BooleanInterpolator();

        for(i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i], values[i]);

        // now fetch these values back again and make sure they are the same
        for(i = 0; i < num_keys; i++)
        {
            boolean val = interpolator.booleanValue(keys[i]);

            assertEquals(val, values[i], i + " value not same");
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

        BooleanInterpolator interpolator = new BooleanInterpolator();

        interpolator.addKeyFrame(keys[0], values[0]);
        interpolator.addKeyFrame(keys[2], values[2]);
        interpolator.addKeyFrame(keys[1], values[1]);

        // now fetch these values back again and make sure they are the same
        for(i = 0; i < num_keys; i++)
        {
            boolean val = interpolator.booleanValue(keys[i]);

            assertEquals(val, values[i], i + " value not same");
        }
    }

    /**
     * Test that we can generate simple values for keys that are in range
     * easily. The earlier tests have made sure that we are returning the
     * right values when the key is exactly equal to one of the end values.
     * Now we are looking at a couple of coordinated points long each axis.
     */
    @Test(groups = "unit")
    public void testKeyGenFloat()
    {
        int num_keys = keys.length;

        BooleanInterpolator interpolator = new BooleanInterpolator();

        for(int i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i], values[i]);

        float mid_key = keys[0] + ((keys[1] - keys[0]) / 2) - 0.0001f;

        boolean val = interpolator.booleanValue(mid_key);

        assertEquals(val, values[0], "1st value not same");

        mid_key = keys[1] + ((keys[2] - keys[1]) / 2) + 0.0001f;

        val = interpolator.booleanValue(mid_key);

        assertEquals(val, values[2], "2nd value not same");
    }

    /**
     * Test that we can generate values that are clamped to the extent values
     * of the interpolator for keys that are out of range to those inserted.
     */
    @Test(groups = "unit")
    public void testClamping()
    {
        int num_keys = keys.length;

        BooleanInterpolator interpolator = new BooleanInterpolator();

        for(int i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i], values[i]);

        // a key value smaller than the smallest key
        float key = keys[0] - 1;

        boolean val = interpolator.booleanValue(key);

        assertEquals(val, values[0], "Min value not same");

        // A key value larger than the largest key
        num_keys--;
        key = keys[num_keys] + 1;

        val = interpolator.booleanValue(key);

        assertEquals(val, values[num_keys], "Max value not same");
    }
}

