/*****************************************************************************
 *                      J3D.org Copyright (c) 2000
 *                           Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.util.interpolator;


import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * A test case to check the functionality of the ScalarInterpolator
 * implementation.
 * <p>
 *
 * The test aims to check insertion and key value generation of the
 * interpolator.
 *
 * @author Justin Couc
 * @version $Revision: 1.3 $
 */
public class ScalarInterpolatorTest
{
    /** Keys to be used for testing */
    private static float[] keys = { 0.1f, 0.4f, 5f };

    /** Values to correspond to the test keys */
    private static float[] values = { 1, 5, 2 };

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
    public void testCreate()
    {
        int i;
        int num_keys = keys.length;

        ScalarInterpolator interpolator = new ScalarInterpolator();

        for(i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i], values[i]);

        // now fetch these values back again and make sure they are the same
        for(i = 0; i < num_keys; i++)
        {
            float value = interpolator.floatValue(keys[i]);

            assertEquals(value, values[i], i + " value not same for key " + keys[i]);
        }
    }

    /**
     * Test that we can create an array of values with values being inserted
     * between other values.
     */
    @Test(groups = "unit")
    public void testValueInsert()
    {
        int i;
        int num_keys = keys.length;

        assertTrue(num_keys > 2, "Not enough keys ( < 3) to do this test");

        ScalarInterpolator interpolator = new ScalarInterpolator();

        interpolator.addKeyFrame(keys[0], values[0]);
        interpolator.addKeyFrame(keys[2], values[2]);
        interpolator.addKeyFrame(keys[1], values[1]);

        // now fetch these values back again and make sure they are the same
        for(i = 0; i < num_keys; i++)
        {
            float value = interpolator.floatValue(keys[i]);

            assertEquals(value, values[i], i + " value not same for key " + keys[i]);
        }
    }

    /**
     * Test that we can generate simple values for keys that are in range
     * easily. The earlier tests have made sure that we are returning the
     * right values when the key is exactly equal to one of the end values.
     * Now we are looking at a couple of coordinated points long each axis.
     */
    @Test(groups = "unit")
    public void testKeyGenLinear()
    {
        int i;
        int num_keys = keys.length;

        ScalarInterpolator interpolator = new ScalarInterpolator();

        for(i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i], values[i]);

        float mid_key = keys[0] + ((keys[1] - keys[0]) / 2);
        float test_value = values[0] + ((values[1] - values[0]) / 2);

        float value = interpolator.floatValue(mid_key);

        assertEquals(test_value, value, "1st value not same");

        mid_key = keys[1] + ((keys[2] - keys[1]) / 2);
        test_value = values[1] + ((values[2] - values[1]) / 2);

        value = interpolator.floatValue(mid_key);

        assertEquals(test_value, value, "2nd value not same");
    }

    /**
     * Test that we can generate simple values for keys that are in range
     * easily. The earlier tests have made sure that we are returning the
     * right values when the key is exactly equal to one of the end values.
     * Now we are looking at a couple of coordinated points long each axis.
     */
    @Test(groups = "unit")
    public void testKeyGenStep()
    {
        // readjust the interpolator type from the default
        ScalarInterpolator interpolator = new ScalarInterpolator(3, Interpolator.STEP);

        int i;
        int num_keys = keys.length;

        for(i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i], values[i]);

        float mid_key = keys[0] + ((keys[1] - keys[0]) / 2);

        float value = interpolator.floatValue(mid_key);

        assertEquals(values[0], value, "1st value not same");

        mid_key = keys[1] + ((keys[2] - keys[1]) / 2);

        value = interpolator.floatValue(mid_key);

        assertEquals(values[1], value, "2nd value not same");
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

        ScalarInterpolator interpolator = new ScalarInterpolator();

        for(i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i], values[i]);

        // a key value smaller than the smallest key
        float key = keys[0] - 1;

        float value = interpolator.floatValue(key);

        assertEquals(value, values[0], "Min value not same");

        // A key value larger than the largest key
        num_keys--;
        key = keys[num_keys] + 1;

        value = interpolator.floatValue(key);

        assertEquals(value, values[num_keys], "Max value not same");
    }
}

