/*****************************************************************************
 *                      J3D.org Copyright (c) 2000
 *                           Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.util.interpolator;

import org.j3d.maths.vector.Point3d;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * A test case to check the functionality of the PositionInterpolator
 * implementation.
 * <p>
 *
 * The test aims to check insertion and key value generation of the
 * interpolator.
 *
 * @author Justin Couc
 * @version $Revision: 1.3 $
 */
public class PositionInterpolatorTest
{
    /** Keys to be used for testing */
    private static float[] keys = { 0.1f, 0.4f, 5f };

    /** Values to correspond to the test keys */
    private static float[][] values = {
        {0, 0, 0},
        {1, 1, 1},
        {1, 5, 2}
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

    /**
     * Test that we can create a basic array of values using float arrays
     * inserted linearly without generating exceptions.
     */
    @Test(groups = "unit")
    public void testCreateFloat()
    {
        int i;
        int num_keys = keys.length;

        PositionInterpolator interpolator = new PositionInterpolator();

        for(i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i],
                                     values[i][0],
                                     values[i][1],
                                     values[i][2]);

        // now fetch these values back again and make sure they are the same
        for(i = 0; i < num_keys; i++)
        {
            float[] vals = interpolator.floatValue(keys[i]);

            assertEquals(values[i][0], vals[0], i + " X coord not same");
            assertEquals(values[i][1], vals[1],i + " Y coord not same");
            assertEquals(values[i][2], vals[2],i + " Z coord not same");
        }
    }

    /**
     * Test that we can create a basic array of values using Point3d
     * inserted linearly without generating exceptions.
     */
    @Test(groups = "unit")
    public void testCreatePoint()
    {
        int i;
        int num_keys = keys.length;
        Point3d point;

        PositionInterpolator interpolator = new PositionInterpolator();

        for(i = 0; i < num_keys; i++)
        {
            point = new Point3d();
            point.x = values[i][0];
            point.y = values[i][1];
            point.z = values[i][2];
            interpolator.addKeyFrame(keys[i], point);
        }

        // now fetch these values back again and make sure they are the same
        for(i = 0; i < num_keys; i++)
        {
            Point3d vals = interpolator.pointValue(keys[i]);

            assertEquals(vals.x, (double)values[i][0], i + " X coord not same");
            assertEquals(vals.y, (double)values[i][1], i + " Y coord not same");
            assertEquals(vals.z, (double)values[i][2], i + " Z coord not same");
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

        PositionInterpolator interpolator = new PositionInterpolator();

        interpolator.addKeyFrame(keys[0],
                values[0][0],
                values[0][1],
                values[0][2]);

        interpolator.addKeyFrame(keys[2],
                                 values[2][0],
                                 values[2][1],
                                 values[2][2]);

        interpolator.addKeyFrame(keys[1],
                                 values[1][0],
                                 values[1][1],
                                 values[1][2]);

        // now fetch these values back again and make sure they are the same
        for(i = 0; i < num_keys; i++)
        {
            float[] vals = interpolator.floatValue(keys[i]);

            assertEquals(vals[0], values[i][0], i + " X coord not same");
            assertEquals(vals[1], values[i][1], i + " Y coord not same");
            assertEquals(vals[2], values[i][2], i + " Z coord not same");
        }
    }

    /**
     * Test that we can create an array of values with values being inserted
     * between other values.
     */
    @Test(groups = "unit")
    public void testValueInsertPoint()
    {
        int i;
        int num_keys = keys.length;
        Point3d point;

        assertTrue(num_keys > 2, "Not enough keys ( < 3) to do this test");

        PositionInterpolator interpolator = new PositionInterpolator();

        point = new Point3d();
        point.x = values[0][0];
        point.y = values[0][1];
        point.z = values[0][2];
        interpolator.addKeyFrame(keys[0], point);

        point = new Point3d();
        point.x = values[2][0];
        point.y = values[2][1];
        point.z = values[2][2];
        interpolator.addKeyFrame(keys[2], point);

        point = new Point3d();
        point.x = values[1][0];
        point.y = values[1][1];
        point.z = values[1][2];
        interpolator.addKeyFrame(keys[1], point);

        // now fetch these values back again and make sure they are the same
        for(i = 0; i < num_keys; i++)
        {
            Point3d vals = interpolator.pointValue(keys[i]);

            assertEquals(vals.x, (double)values[i][0], i + " X coord not same");
            assertEquals(vals.y, (double)values[i][1], i + " Y coord not same");
            assertEquals(vals.z, (double)values[i][2], i + " Z coord not same");
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
        int i;
        int num_keys = keys.length;

        PositionInterpolator interpolator = new PositionInterpolator();

        for(i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i],
                                     values[i][0],
                                     values[i][1],
                                     values[i][2]);

        float mid_key = keys[0] + ((keys[1] - keys[0]) / 2);
        float x_val = values[0][0] + ((values[1][0] - values[0][0]) / 2);
        float y_val = values[0][0] + ((values[1][1] - values[0][1]) / 2);
        float z_val = values[0][0] + ((values[1][2] - values[0][2]) / 2);

        float[] vals = interpolator.floatValue(mid_key);

        assertEquals(x_val, vals[0], "1st X coord not same");
        assertEquals(y_val, vals[1], "1st Y coord not same");
        assertEquals(z_val, vals[2], "1st Z coord not same");

        mid_key = keys[1] + ((keys[2] - keys[1]) / 2);
        x_val = values[1][0] + ((values[2][0] - values[1][0]) / 2);
        y_val = values[1][1] + ((values[2][1] - values[1][1]) / 2);
        z_val = values[1][2] + ((values[2][2] - values[1][2]) / 2);

        vals = interpolator.floatValue(mid_key);

        assertEquals(x_val, vals[0], "2nd X coord not same");
        assertEquals(y_val, vals[1], "2nd Y coord not same");
        assertEquals(z_val, vals[2], "2nd Z coord not same");
    }

    /**
     * Test that we can generate simple values for keys that are in range
     * easily. The earlier tests have made sure that we are returning the
     * right values when the key is exactly equal to one of the end values.
     * Now we are looking at a couple of coordinated points long each axis.
     */
    @Test(groups = "unit")
    public void testKeyGenPoint()
    {
        int i;
        int num_keys = keys.length;
        Point3d point;

        PositionInterpolator interpolator = new PositionInterpolator();

        for(i = 0; i < num_keys; i++)
        {
            point = new Point3d();
            point.x = values[i][0];
            point.y = values[i][1];
            point.z = values[i][2];
            interpolator.addKeyFrame(keys[i], point);
        }

        float mid_key = keys[0] + ((keys[1] - keys[0]) / 2);
        double x_val = values[0][0] + ((values[1][0] - values[0][0]) / 2);
        double y_val = values[0][0] + ((values[1][1] - values[0][1]) / 2);
        double z_val = values[0][0] + ((values[1][2] - values[0][2]) / 2);

        Point3d vals = interpolator.pointValue(mid_key);

        assertEquals(vals.x, x_val, "1st X coord not same");
        assertEquals(vals.y, y_val, "1st Y coord not same");
        assertEquals(vals.z, z_val, "1st Z coord not same");

        mid_key = keys[1] + ((keys[2] - keys[1]) / 2);
        x_val = values[1][0] + ((values[2][0] - values[1][0]) / 2);
        y_val = values[1][1] + ((values[2][1] - values[1][1]) / 2);
        z_val = values[1][2] + ((values[2][2] - values[1][2]) / 2);

        vals = interpolator.pointValue(mid_key);

        assertEquals(vals.x, x_val, "2nd X coord not same");
        assertEquals(vals.y, y_val, "2nd Y coord not same");
        assertEquals(vals.z, z_val, "2nd Z coord not same");
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

        PositionInterpolator interpolator = new PositionInterpolator();

        for(i = 0; i < num_keys; i++)
            interpolator.addKeyFrame(keys[i],
                                     values[i][0],
                                     values[i][1],
                                     values[i][2]);

        // a key value smaller than the smallest key
        float key = keys[0] - 1;

        float[] vals = interpolator.floatValue(key);

        assertEquals(vals[0], values[0][0], "Min X coord not same");
        assertEquals(vals[1], values[0][1], "Min Y coord not same");
        assertEquals(vals[2], values[0][2], "Min Z coord not same");

        // A key value larger than the largest key
        num_keys--;
        key = keys[num_keys] + 1;

        vals = interpolator.floatValue(key);

        assertEquals(vals[0], values[num_keys][0], "Max X coord not same");
        assertEquals(vals[1], values[num_keys][1], "Max Y coord not same");
        assertEquals(vals[2], values[num_keys][2], "Max Z coord not same");
    }
}

