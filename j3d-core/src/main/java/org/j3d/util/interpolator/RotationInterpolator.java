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
// None

// Application specific imports
import org.j3d.maths.vector.AxisAngle4d;
import org.j3d.maths.vector.Quat4d;

/**
 * An interpolator that works with positional coordinates.
 * <P>
 *
 * Two different interpolation schemes are provided - a simple linear system
 * and quaternion based. The interpolator may take arbitrarily
 * spaced keyframes and compute correct values.
 * <P>
 * The simple interpolation routine is just a linear interpolation between
 * each component of each of the points.
 * <p>
 *
 * For quaternion based interpolation, the code uses the algorithm presented by
 * Graphics Gems III, Page 96.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class RotationInterpolator extends Interpolator
{
    /** Flag to nominate that the interpolation routine should be quaternions */
    public static final int QUATERNION = 3;

    /** Reference to the shared quaternion return value for key values */
    private Quat4d sharedPoint;

    /** Reference to the shared float array return value for key values */
    private float[] sharedVector;

    /** The key values indexed as [index][x, y, z, r] */
    private float[][] keyValues;

    // Working vars for quaternion based interpolation
    private AxisAngle4d angle1, angle2;
    private Quat4d quat1, quat2;

    /**
     * Create a new linear interpolator instance with the default size for the
     * number of key values.
     */
    public RotationInterpolator()
    {
        this(DEFAULT_SIZE, LINEAR);
    }

    /**
     * Create an linear interpolator with the given basic size.
     *
     * @param size The starting number of items in interpolator
     */
    public RotationInterpolator(int size)
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
    public RotationInterpolator(int size, int type)
    {
        super(size, type);

        keys = new float[size];
        keyValues = new float[size][4];

        sharedPoint = new Quat4d();
        sharedVector = new float[4];

        if(type == QUATERNION)
        {
            angle1 = new AxisAngle4d();
            angle2 = new AxisAngle4d();
            quat1 = new Quat4d();
            quat2 = new Quat4d();
        }
    }

    /**
     * Add a key frame set of values at the given key point. This will insert
     * the values at the correct position within the array for the given key.
     * If two keys have the same value, the new key is inserted before the old
     * one.
     *
     * @param key The value of the key to use
     * @param x The x coordinate of the orientation at this key
     * @param y The y coordinate of the orientation at this key
     * @param z The z coordinate of the orientation at this key
     * @param w The angle coordinate (angle) of the orientation at this key
     */
    public void addKeyFrame(float key, float x, float y, float z, float w)
    {
        int loc = findKeyIndex(key);

        // loc is now the largest key less than the new key.
        // adjust loc up to the first key greater than the new key.
        if(loc < 0)
            loc = 0;
        while (loc<currentSize && keys[loc]<=key) 
            loc++; 

        realloc();

        float[] new_val;

        if(loc >= currentSize)
        {
            // append to the end
            new_val = keyValues[currentSize];
        }
        else
        {
            // insert. Shuffle everything up one spot
            int num_moving = currentSize - loc;

            System.arraycopy(keyValues, loc, keyValues, loc + 1, num_moving);
            System.arraycopy(keys, loc, keys, loc + 1, num_moving);

            new_val = new float[4];
            keyValues[loc] = new_val;
        }

        new_val[0] = x;
        new_val[1] = y;
        new_val[2] = z;
        new_val[3] = w;

        keys[loc] = key;
        currentSize++;
    }

    /**
     * Add a key frame set of values at the given key point. This will insert
     * the values at the correct position within the array for the given key.
     * If two keys have the same value, the new key is inserted before the old
     * one.
     *
     * @param key The value of the key to use
     * @param pt The point data to take information from
     */
    public void addKeyFrame(float key, AxisAngle4d pt)
    {
        if(pt == null)
            throw new IllegalArgumentException("Angle must not be null");

        addKeyFrame(key, (float)pt.x, (float)pt.y, (float)pt.z, (float)pt.angle);
    }

    /**
     * Get the interpolated value of the point at the given key value. If the
     * key lies outside the range of the values defined, it will be clamped to
     * the end point value. For speed reasons, this will return a reusable
     * point instance. Do not modify the values or keep a reference to this as
     * it will change values between calls.
     *
     * @param key The key value to get the position for
     * @return A point representation of the value at that position
     */
    public Quat4d pointValue(float key)
    {
        int loc = findKeyIndex(key);

        if(loc < 0)
           sharedPoint.set(keyValues[0][0], keyValues[0][1], keyValues[0][2], keyValues[0][2]);
        else if(loc >= currentSize)
        {
           sharedPoint.set(keyValues[currentSize - 1][0],
                           keyValues[currentSize - 1][1],
                           keyValues[currentSize - 1][2],
                           keyValues[currentSize - 1][3]);
        }
        else
        {
            switch(interpolationType)
            {
                case LINEAR:
                    float[] p1 = keyValues[loc + 1];
                    float[] p0 = keyValues[loc];

                    float x_dist = p1[0] - p0[0];
                    float y_dist = p1[1] - p0[1];
                    float z_dist = p1[2] - p0[2];
                    float w_dist = p1[3] - p0[3];

                    float fraction = 0;

                    // just in case we get two keys the same
                    float prev_key = keys[loc];
                    float found_key = keys[loc + 1];

                    if(found_key != prev_key)
                        fraction = (key - prev_key) / (found_key - prev_key);

                    sharedPoint.x = p0[0] + fraction * x_dist;
                    sharedPoint.y = p0[1] + fraction * y_dist;
                    sharedPoint.z = p0[2] + fraction * z_dist;
                    sharedPoint.angle = p0[3] + fraction * w_dist;
                    break;

                case STEP:
                    float[] pnt = keyValues[loc];

                    sharedPoint.x = pnt[0];
                    sharedPoint.y = pnt[1];
                    sharedPoint.z = pnt[2];
                    sharedPoint.angle = pnt[3];
                    break;

                case QUATERNION:
                    p1 = keyValues[loc + 1];
                    p0 = keyValues[loc];

                    angle1.set(p0[0], p0[1], p0[2], p0[3]);
                    angle2.set(p1[0], p1[1], p1[2], p1[3]);

                    quat1.set(angle1);
                    quat2.set(angle2);

                    double cos_omega = quat1.x * quat2.x +
                                       quat1.y * quat2.y +
                                       quat1.z * quat2.z +
                                       quat1.angle * quat2.angle;

                    // If opposite hemispheres then negate quat1
                    if (cos_omega < 0.0)
                        quat1.negate();

                    fraction = 0;

                    // just in case we get two keys the same
                    prev_key = keys[loc];
                    found_key = keys[loc + 1];

                    if(found_key != prev_key)
                        fraction = (key - prev_key) / (found_key - prev_key);

                    quat1.interpolate(quat1, quat2, fraction);
                    quat1.get(angle1);

                    sharedVector[0] = (float)angle1.x;
                    sharedVector[1] = (float)angle1.y;
                    sharedVector[2] = (float)angle1.z;
                    sharedVector[3] = (float)angle1.angle;

                    sharedPoint.set(sharedVector[0], sharedVector[1], sharedVector[2], sharedVector[3]);
            }
        }

        return sharedPoint;
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
    public float[] floatValue(float key)
    {
        int loc = findKeyIndex(key);

        if(loc < 0)
        {
           sharedVector[0] = keyValues[0][0];
           sharedVector[1] = keyValues[0][1];
           sharedVector[2] = keyValues[0][2];
           sharedVector[3] = keyValues[0][3];
        }
        else if(loc >= (currentSize - 1))
        {
           sharedVector[0] = keyValues[currentSize - 1][0];
           sharedVector[1] = keyValues[currentSize - 1][1];
           sharedVector[2] = keyValues[currentSize - 1][2];
           sharedVector[3] = keyValues[currentSize - 1][3];
        }
        else
        {
            switch(interpolationType)
            {
                case LINEAR:
                    float[] p1 = keyValues[loc + 1];
                    float[] p0 = keyValues[loc];

                    float x_dist = p1[0] - p0[0];
                    float y_dist = p1[1] - p0[1];
                    float z_dist = p1[2] - p0[2];
                    float w_dist = p1[3] - p0[3];
                    float fraction = 0;

                    // just in case we get two keys the same
                    float prev_key = keys[loc];
                    float found_key = keys[loc + 1];

                    if(found_key != prev_key)
                        fraction = (key - prev_key) / (found_key - prev_key);

                    sharedVector[0] = p0[0] + fraction * x_dist;
                    sharedVector[1] = p0[1] + fraction * y_dist;
                    sharedVector[2] = p0[2] + fraction * z_dist;
                    sharedVector[3] = p0[3] + fraction * w_dist;
                    break;

                case STEP:
                    float[] pnt = keyValues[loc];
                    sharedVector[0] = pnt[0];
                    sharedVector[1] = pnt[1];
                    sharedVector[2] = pnt[2];
                    sharedVector[3] = pnt[3];
                    break;

                case QUATERNION:
                    p1 = keyValues[loc + 1];
                    p0 = keyValues[loc];

                    angle1.set(p0[0], p0[1], p0[2], p0[3]);
                    angle2.set(p1[0], p1[1], p1[2], p1[3]);

                    quat1.set(angle1);
                    quat2.set(angle2);

                    double cos_omega = quat1.x * quat2.x +
                                       quat1.y * quat2.y +
                                       quat1.z * quat2.z +
                                       quat1.angle * quat2.angle;

                    // If opposite hemispheres then negate quat1
                    if (cos_omega < 0.0)
                        quat1.negate();

                    fraction = 0;

                    // just in case we get two keys the same
                    prev_key = keys[loc];
                    found_key = keys[loc + 1];

                    if(found_key != prev_key)
                        fraction = (key - prev_key) / (found_key - prev_key);

                    quat1.interpolate(quat1, quat2, fraction);
                    quat1.get(angle1);

                    sharedVector[0] = (float)angle1.x;
                    sharedVector[1] = (float)angle1.y;
                    sharedVector[2] = (float)angle1.z;
                    sharedVector[3] = (float)angle1.angle;
            }
        }

        return sharedVector;
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
            float[][] new_values = new float[new_size][];

            System.arraycopy(keyValues, 0, new_values, 0, allocatedSize);

            for(int i = allocatedSize; i < new_size; i++)
                new_values[i] = new float[4];

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
        StringBuilder buf = new StringBuilder("<rotation interpolator>\n");

        for(int i = 0; i < currentSize; i++)
        {
            buf.append(i);
            buf.append(" key: ");
            buf.append(keys[i]);
            buf.append(" x: ");
            buf.append(keyValues[i][0]);
            buf.append(" y: ");
            buf.append(keyValues[i][1]);
            buf.append(" z: ");
            buf.append(keyValues[i][2]);
            buf.append(" angle: ");
            buf.append(keyValues[i][3]);
            buf.append("\n");
        }

        buf.append("</rotation interpolator>");
        return buf.toString();
    }
}
