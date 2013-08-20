/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.util.interpolator;

// Standard imports
import javax.vecmath.Point3f;

// Application specific imports
// none

/**
 * An interpolator that works with integer values.
 * <P>
 *
 * For linear interpolation this class will operate as such:
 *    On interval n to n+1 where n=v0 and n+1=v1
 *    the value will be v0 for all values from n to (n + (n+1 - n) / 2)
 *    and v1 from > (n + (n+1 - n) / 2) to n + 1
 * <p>
 * The interpolation routine is either a stepwise or simple linear
 * interpolation between each of the points. The interpolator may take
 * arbitrarily spaced keyframes and compute correct values.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class IntegerInterpolator extends Interpolator
{
    /** The key values where the indicies match the keys */
    private int[] keyValues;

    /**
     * Create a new linear interpolator instance with the default size for the
     * number of key values.
     */
    public IntegerInterpolator()
    {
        this(DEFAULT_SIZE, LINEAR);
    }

    /**
     * Create a linear interpolator with the given basic size.
     *
     * @param size The starting number of items in interpolator
     */
    public IntegerInterpolator(int size)
    {
        this(size, LINEAR);
    }

    /**
     * Create a interpolator with the given basic size using the interpolation
     * type.
     *
     * @param size The starting number of items in interpolator
     * @param type The type of interpolation scheme to use
     */
    public IntegerInterpolator(int size, int type)
    {
        super(size, type);

        keyValues = new int[size];
    }

    /**
     * Add a key frame set of values at the given key point. This will insert
     * the values at the correct position within the array for the given key.
     * If two keys have the same value, the new key is inserted before the old
     * one.
     *
     * @param key The value of the key to use
     * @param value The scalar value at this key
     */
    public void addKeyFrame(float key, int value)
    {
        int loc = findKeyIndex(key);

        // loc is now the largest key less than the new key.
        // adjust loc up to the first key greater than the new key.
        if(loc < 0)
            loc = 0;
        while (loc<currentSize && keys[loc]<=key)
            loc++;

        realloc();

        int[] new_val;

        if(loc >= currentSize)
        {
            // append to the end
            keyValues[currentSize] = value;
        }
        else
        {
            // insert. Shuffle everything up one spot
            int num_moving = currentSize - loc;

            System.arraycopy(keyValues, loc, keyValues, loc + 1, num_moving);
            System.arraycopy(keys, loc, keys, loc + 1, num_moving);

            keyValues[loc] = value;
        }

        keys[loc] = key;
        currentSize++;
    }

    /**
     * Get the interpolated value of the point at the given key value. If the
     * key lies outside the range of the values defined, it will be clamped to
     * the end point value. For speed reasons, this will return a reusable
     * float array. Do not modify the values or keep a reference to this as
     * it will change values between calls.
     *
     * @param key The key value to get the position for
     * @return An array of the values at that position [x, y, z]
     */
    public int intValue(float key)
    {
        int loc = findKeyIndex(key);
        int ret_val;

        if(loc < 0)
        {
           ret_val = keyValues[0];
        }
        else if(loc >= (currentSize - 1))
        {
           ret_val = keyValues[currentSize - 1];
        }
        else
        {
            switch(interpolationType)
            {
                case LINEAR:
                    int p1 = keyValues[loc + 1];
                    int p0 = keyValues[loc];

                    float fraction = 0;

                    // just in case we get two keys the same
                    float prev_key = keys[loc];
                    float found_key = keys[loc + 1];

                    if(found_key != prev_key)
                        fraction = (key - prev_key) / (found_key - prev_key);

/*
System.out.println("Prev key " + prev_key);
System.out.println("Next key " + found_key);
System.out.println("Reqd key " + key);
System.out.println("Fraction is " + fraction);
System.out.println("x " + p0 + " dist " + dist);
*/
                    if (fraction <= 0.5)
                        ret_val = p0;
                    else
                       ret_val = p1;
                    break;

                case STEP:
                    ret_val = keyValues[loc];
                    break;

                default:
                    ret_val = 0;
            }
        }

        return ret_val;
    }

    //---------------------------------------------------------------
    // Misc Internal methods
    //---------------------------------------------------------------

    /**
     * Resize the allocated space for the keyValues array if needed. Marked
     * as final in order to encourage the compiler to inline the code for
     * faster execution.
     */
    private final void realloc()
    {
        if(currentSize == allocatedSize)
        {
            int new_size = allocatedSize + ARRAY_INCREMENT;

            // Don't acutally allocate the space for the float[3] values as the
            // arraycopy will set these. Just make sure we allocate after that
            // the remaining new, empty, places.
            int[] new_values = new int[new_size];

            System.arraycopy(keyValues, 0, new_values, 0, allocatedSize);

            float[] new_keys = new float[new_size];

            System.arraycopy(keys, 0, new_keys, 0, allocatedSize);

            keys = new_keys;
            keyValues = new_values;

            allocatedSize = new_size;
        }
    }

    /**
     * Create a string representation of this interpolator's values
     *
     * @return A nicely formatted string representation
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer("<integer interpolator>\n");

        for(int i = 0; i < currentSize; i++)
        {
            buf.append(i);
            buf.append(" key: ");
            buf.append(keys[i]);
            buf.append(" value: ");
            buf.append("\n");
        }

        buf.append("</integer interpolator>");
        return buf.toString();
    }
}
