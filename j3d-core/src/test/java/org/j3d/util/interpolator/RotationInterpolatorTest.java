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

import org.j3d.maths.vector.AxisAngle4d;
import org.j3d.maths.vector.Quat4d;
import org.j3d.maths.vector.Vector3d;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * A test case to check the functionality of the RotationInterpolator
 * implementation.
 * <p>
 *
 * The test aims to check insertion and key value generation of the
 * interpolator.
 *
 * @author Justin Couc
 * @version $Revision: 1.3 $
 */
public class RotationInterpolatorTest
{
    /** Keys to be used for testing */
    private static float[] keys = { 0.1f, 0.4f, 5f };

    /** Values to correspond to the test keys */
    private static float[][] values =
    {
        {1, 0, 0, 0},
        {0, 1, 0, (float)(Math.PI * 0.5)},
        {0, 0, 1, 0},
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
    public void testAddNullAngle() throws Exception
    {
        RotationInterpolator interpolator = new RotationInterpolator();
        interpolator.addKeyFrame(1.0f, null);
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

        RotationInterpolator interpolator = new RotationInterpolator();

        for(i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i], values[i][0], values[i][1], values[i][2], values[i][3]);

        // now fetch these values back again and make sure they are the same
        for(i = 0; i < num_keys; i++)
        {
            float[] vals = interpolator.floatValue(keys[i]);

            assertEquals(vals[0], values[i][0], i + " X axis not same");
            assertEquals(vals[1], values[i][1], i + " Y axis not same");
            assertEquals(vals[2], values[i][2], i + " Z axis not same");
            assertEquals(vals[3], values[i][3], i + " Angle not same");
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

        RotationInterpolator interpolator = new RotationInterpolator();

        interpolator.addKeyFrame(keys[0], values[0][0], values[0][1], values[0][2], values[0][3]);
        interpolator.addKeyFrame(keys[2], values[2][0], values[2][1], values[2][2], values[2][3]);
        interpolator.addKeyFrame(keys[1], values[1][0], values[1][1], values[1][2], values[1][3]);

        // now fetch these values back again and make sure they are the same
        for(i = 0; i < num_keys; i++)
        {
            float[] vals = interpolator.floatValue(keys[i]);

            assertEquals(vals[0], values[i][0], i + " X axis not same");
            assertEquals(vals[1], values[i][1], i + " Y axis not same");
            assertEquals(vals[2], values[i][2], i + " Z axis not same");
            assertEquals(vals[3], values[i][3], i + " Z axis not same");
        }
    }

    /**
     * Test that we can generate simple values for keys that are in range
     * easily. The earlier tests have made sure that we are returning the
     * right values when the key is exactly equal to one of the end values.
     * Now we are looking at a couple of axisinated points long each axis.
     */
    @Test(groups = "unit")
    public void testKeyGenFloatQuaternionInterpolation()
    {
        int i;
        int num_keys = keys.length;

        // Only tests the axis interpolation, not the angle.
        RotationInterpolator interpolator = new RotationInterpolator(10, RotationInterpolator.QUATERNION);

        for(i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i], values[i][0], values[i][1], values[i][2], values[i][3]);

        float mid_key = keys[0] + ((keys[1] - keys[0]) / 2);

        AxisAngle4d p1 = new AxisAngle4d();
        p1.set(values[0][0], values[0][1], values[0][2], values[0][3]);

        AxisAngle4d p2 = new AxisAngle4d();
        p2.set(values[1][0], values[1][1], values[1][2], values[1][3]);

        Quat4d q1 = new Quat4d();
        q1.set(p1);

        Quat4d q2 = new Quat4d();
        q2.set(p2);

        Quat4d expectedQ = new Quat4d();
        expectedQ.interpolate(q1, q2, 0.5);

        AxisAngle4d expectedAngle = new AxisAngle4d();
        expectedQ.get(expectedAngle);

        float[] vals = interpolator.floatValue(mid_key);

        assertEquals(vals[0], expectedAngle.x, 0.001f, "1st X axis not same");
        assertEquals(vals[1], expectedAngle.y, 0.001f, "1st Y axis not same");
        assertEquals(vals[2], expectedAngle.z, 0.001f, "1st Z axis not same");
        assertEquals(vals[3], expectedAngle.angle, 0.001f, "1st angle not same");

        mid_key = keys[1] + ((keys[2] - keys[1]) / 2);
        p1.set(values[1][0], values[1][1], values[1][2], values[1][3]);
        p2.set(values[2][0], values[2][1], values[2][2], values[2][3]);

        q1.set(p1);
        q2.set(p2);
        expectedQ.interpolate(q1, q2, 0.5);
        expectedQ.get(expectedAngle);

        vals = interpolator.floatValue(mid_key);

        assertEquals(vals[0], expectedAngle.x, 0.001f, "2nd X axis not same");
        assertEquals(vals[1], expectedAngle.y, 0.001f, "2nd Y axis not same");
        assertEquals(vals[2], expectedAngle.z, 0.001f, "2nd Z axis not same");
        assertEquals(vals[3], expectedAngle.angle, 0.001f, "2nd angle not same");
    }

    /**
     * Test that we can generate simple values for keys that are in range
     * easily. The earlier tests have made sure that we are returning the
     * right values when the key is exactly equal to one of the end values.
     * Now we are looking at a couple of axisinated points long each axis.
     */
    @Test(groups = "unit")
    public void testKeyGenFloatLinearInterpolation()
    {
        int i;
        int num_keys = keys.length;

        // Only tests the axis interpolation, not the angle.
        RotationInterpolator interpolator = new RotationInterpolator(10, RotationInterpolator.LINEAR);

        for(i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i], values[i][0], values[i][1], values[i][2], values[i][3]);

        float mid_key = keys[0] + ((keys[1] - keys[0]) / 2);

        Vector3d p1 = new Vector3d();
        p1.set(values[0][0], values[0][1], values[0][2]);

        Vector3d p2 = new Vector3d();
        p2.set(values[1][0], values[1][1], values[1][2]);

        Vector3d mid_point = new Vector3d();
        mid_point.interpolate(p1, p2, 0.5);

        float[] vals = interpolator.floatValue(mid_key);

        assertEquals(vals[0], mid_point.x, 0.001f, "1st X axis not same");
        assertEquals(vals[1], mid_point.y, 0.001f, "1st Y axis not same");
        assertEquals(vals[2], mid_point.z, 0.001f, "1st Z axis not same");

        mid_key = keys[1] + ((keys[2] - keys[1]) / 2);
        p1.set(values[1][0], values[1][1], values[1][2]);
        p2.set(values[2][0], values[2][1], values[2][2]);
        mid_point.interpolate(p1, p2, 0.5);

        vals = interpolator.floatValue(mid_key);

        assertEquals(vals[0], mid_point.x, 0.001f, "1st X axis not same");
        assertEquals(vals[1], mid_point.y, 0.001f, "1st Y axis not same");
        assertEquals(vals[2], mid_point.z, 0.001f, "1st Z axis not same");
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

        RotationInterpolator interpolator = new RotationInterpolator();

        for(i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i], values[i][0], values[i][1], values[i][2], values[i][3]);

        // a key value smaller than the smallest key
        float key = keys[0] - 1;

        float[] vals = interpolator.floatValue(key);

        assertEquals(vals[0], values[0][0], "Min X axis not same");
        assertEquals(vals[1], values[0][1], "Min Y axis not same");
        assertEquals(vals[2], values[0][2], "Min Z axis not same");

        // A key value larger than the largest key
        num_keys--;
        key = keys[num_keys] + 1;

        vals = interpolator.floatValue(key);

        assertEquals(vals[0], values[num_keys][0], "Max X axis not same");
        assertEquals(vals[1], values[num_keys][1], "Max Y axis not same");
        assertEquals(vals[2], values[num_keys][2], "Max Z axis not same");
    }
}

