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
 * A test case to check the functionality of the CoordinateInterpolator
 * implementation.
 * <p>
 *
 * The test aims to check insertion and key value generation of the
 * interpolator.
 *
 * @author Justin Couc
 * @version $Revision: 1.2 $
 */
public class CoordinateInterpolatorTest
{
    /** Keys to be used for testing */
    private static float keys[] = { 0.1f, 0.4f, 5f };

    /** Values to correspond to the test keys */
    private static float values[][] =
    {
        { 0f, 0f, 0f, 1f, 1f, 1f },
        { 1f, 1f, 1f, 2f, 2f, 2f },
        { 1f, 5f, 2f, 5f, 5f, 5f }
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
    public void testInvalidInsertNullValues()
    {
        CoordinateInterpolator interpolator = new CoordinateInterpolator();
        interpolator.addKeyFrame(0, null);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidInsertZeroLengthValues()
    {
        CoordinateInterpolator interpolator = new CoordinateInterpolator();

        float af[] = new float[0];
        interpolator.addKeyFrame(0, af);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidInsertLessThan3Values()
    {
        CoordinateInterpolator interpolator = new CoordinateInterpolator();

        float[] af = { 0, 1, };
        interpolator.addKeyFrame(0, af);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidInsertNotMutl3()
    {
        CoordinateInterpolator interpolator = new CoordinateInterpolator();

        float[] af = { 0, 1, 3, 5 };
        interpolator.addKeyFrame(0, af);
    }

    /**
     * Test that we can create a basic array of values using float arrays
     * inserted linearly without generating exceptions.
     */
    @Test(groups = "unit")
    public void testCreateFloat()
    {
        int i;
        int len = keys.length;

        CoordinateInterpolator interpolator = new CoordinateInterpolator();

        for(i = 0; i < len; i++)
            interpolator.addKeyFrame(keys[i], values[i]);

        for(i = 0; i < len; i++)
        {
            float new_vals[] = interpolator.floatValue(keys[i]);

            for(int j = 0; j < new_vals.length; j++)
                assertEquals(new_vals[j], values[i][j], "key " + i + " coord " + j + " not same");
        }
    }

    /**
     * Test that we can create an array of values with values being inserted
     * between other values.
     */
    @Test(groups = "unit")
    public void testValueInsertFloat()
    {
        CoordinateInterpolator interpolator = new CoordinateInterpolator();

        int j = keys.length;
        assertTrue(j > 2, "Not enough keys ( < 3) to do this test");
        interpolator.addKeyFrame(keys[0], values[0]);
        interpolator.addKeyFrame(keys[2], values[2]);
        interpolator.addKeyFrame(keys[1], values[1]);
        for(int i = 0; i < j; i++)
        {
            float af[] = interpolator.floatValue(keys[i]);
            for(int len = 0; len < af.length; len++)
                assertEquals(values[i][len], af[len], 0.001, "key " + i + " coord " + len + " not same");

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
        CoordinateInterpolator interpolator = new CoordinateInterpolator();

        int j = keys.length;
        for(int i = 0; i < j; i++)
            interpolator.addKeyFrame(keys[i], values[i]);

        float f = keys[0] + (keys[1] - keys[0]) / 2;
        float f1 = values[0][0] + (values[1][0] - values[0][0]) / 2;
        float f2 = values[0][0] + (values[1][1] - values[0][1]) / 2;
        float f3 = values[0][0] + (values[1][2] - values[0][2]) / 2;

        float af[] = interpolator.floatValue(f);

        assertEquals(f1, af[0], 0.0, "1st X coord not same");
        assertEquals(f2, af[1], 0.0, "1st Y coord not same");
        assertEquals(f3, af[2], 0.0, "1st Z coord not same");

        f = keys[1] + (keys[2] - keys[1]) / 2;
        f1 = values[1][0] + (values[2][0] - values[1][0]) / 2;
        f2 = values[1][1] + (values[2][1] - values[1][1]) / 2;
        f3 = values[1][2] + (values[2][2] - values[1][2]) / 2;

        af = interpolator.floatValue(f);

        assertEquals(f1, af[0], 0.0, "2nd X coord not same");
        assertEquals(f2, af[1], 0.0, "2nd Y coord not same");
        assertEquals(f3, af[2], 0.0, "2nd Z coord not same");
    }

    /**
     * Test that we can generate values that are clamped to the extent values
     * of the interpolator for keys that are out of range to those inserted.
     */
    @Test(groups = "unit")
    public void testClamping()
    {
        CoordinateInterpolator interpolator = new CoordinateInterpolator();

        int j = keys.length;
        for(int i = 0; i < j; i++)
            interpolator.addKeyFrame(keys[i], values[i]);

        float f = keys[0] - 1;
        float af[] = interpolator.floatValue(f);
        assertEquals(af[0], values[0][0], "Min X coord not same");
        assertEquals(af[0], values[0][1], "Min Y coord not same");
        assertEquals(af[0], values[0][2], "Min Z coord not same");
        j--;

        f = keys[j] + 1;
        af = interpolator.floatValue(f);
        assertEquals(af[0], values[j][0], "Max X coord not same");
        assertEquals(af[1], values[j][1], "Max Y coord not same");
        assertEquals(af[2], values[j][2], "Max Z coord not same");
    }
}
