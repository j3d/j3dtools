/*****************************************************************************
 *                      J3D.org Copyright (c) 2000
 *                           Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.util.interpolator;

// Standard imports
// None

// Application specific imports
// none

/**
 * An interpolator that works with sets of normals.
 * <P>
 *
 * The interpolation routine is a constant-angle interpolation between
 * each of the normals in the pairs of normal sets.
 * The interpolator may take arbitrarily spaced keyframes
 * and compute correct values. 
 * Based on the CoordinateInterpolator.java by Justin Couch.
 * <P>
 *
 * The interpolation implementation is based on the following:
 *   0 <= f <= 1,
 *   N1, N2 are normalized input normals,
 *   A is angle between N1 and N2 == acos(N1xN2),
 *   N3 = (sin((1-f)*A)*N1 + sin(f*A)*N2
 *   Normalized result is N3/(|N3|)
 *
 * @author Guy Carpenter
 * @version $Revision: 1.3 $
 */
public class NormalInterpolator extends Interpolator
{

    /** Reference to the shared float array return value for key values */
    private float sharedVector[];

    /** The key values indexed as [index][x0, y0, z0, x1, y1, z1, ...] */
    private float keyValues[][];

    /** The index of the normal set for which we currently hold cached angles
        or -1 if the cache is invalid.  Marks the lower of the pair of
        normal sets used to compute angles. */
    private int angleCacheIndex;

    /** Number of normals, and thus also the number of angles in angles[].
        Also equal to the floor of valueLength/3. */
    private int normalCount;
    
    /** Angles between each normal pair, cached between calls */
    private float angles[];

    /** The number of floats in the shortest member of the value array */
    private int valueLength;

    /**
     * Create a new linear interpolator instance with the default size for the
     * number of key values.
     */
    public NormalInterpolator()
    {
        this(DEFAULT_SIZE, LINEAR);
    }

    /**
     * Create an linear interpolator with the given basic size.
     *
     * @param size The starting number of items in interpolator
     */
    public NormalInterpolator(int size)
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
    public NormalInterpolator(int size, int type)
    {
        super(size, type);

        keys = new float[size];
        keyValues = new float[size][];
        valueLength = -1;
        angleCacheIndex = -1;  // invalidate angle cache
    }

    /**
     * Add a key frame set of values at the given key point. This will insert
     * the values at the correct position within the array for the given key.
     * If two keys have the same value, the new key is inserted after the old
     * one.
     *
     * @param key The value of the key to use
     * @param normals The normals at this key
     */
    public void addKeyFrame(float key, float normals[])
    {
        int loc = findKeyIndex(key); 

        // loc is now the largest key less than the new key.
        // adjust loc up to first key greater than new key.
        if(loc < 0)
            loc = 0;
        while (loc<currentSize && keys[loc]<=key) 
            loc++; 

        angleCacheIndex = -1;  // invalidate angle cache

        realloc();

        if(normals == null)
            throw new IllegalArgumentException("Normal array is null");

        int len = normals.length;

        if(len < 3 || len % 3 != 0)
            throw new IllegalArgumentException("Normals length not x 3");

        float normals1[] = new float[len];

        System.arraycopy(normals, 0, normals1, 0, len);

        /* set the value length to the shortest set of normals found so far */
        if(valueLength > len || valueLength < 0)
            valueLength = len;

        if(loc >= currentSize)
        {
            loc = currentSize;  // append
        }
        else
        {
            int k = currentSize - loc;
            System.arraycopy(keyValues, loc, keyValues, loc + 1, k);
            System.arraycopy(keys, loc, keys, loc + 1, k);

        }
        keyValues[loc] = normals1;
        keys[loc] = key;
        currentSize++;
    }

    /**
     * Compute the angle between each of the normal pairs in the
     * sets keyValues[index],keyValues[index+1].
     * Also sets angleCacheIndex and normalCount;
     */

    private void computeAngles(int index)
    {
        int i;

        // if cache is valid, do nothing.
        if (angleCacheIndex != index) {
            angleCacheIndex = index;

            normalCount = valueLength/3; // discrete vectors

            // allocate array of angles
            if (angles == null || angles.length != normalCount)
            {
                angles = new float[normalCount];
            }

            int vi=0;
            for (i=0;i<normalCount;i++)
            {
                // dot product: cos(angle) = ax*bx+ay*by+az*bz
                float dot = 
                    keyValues[index][vi+0] * keyValues[index+1][vi+0] +
                    keyValues[index][vi+1] * keyValues[index+1][vi+1] +
                    keyValues[index][vi+2] * keyValues[index+1][vi+2];
                angles[i] = (float)Math.acos(dot);
            }
        }
    }

    /**
     * Get the interpolated normal set for the given key value.  If the
     * key lies outside the range of the values defined, it will be clamped to
     * the end point value. For speed reasons, this will return a reusable
     * float array. Do not modify the values or keep a reference to this as
     * it will change values between calls.
     *
     * @param key The key value to get the position for
     * @return An array of the normal values at that position.  All normals are flattened into a single array of [x, y, z] values.  The length of the array will be 3 times the number of normals returned.
     */
    public float[] floatValue(float key)
    {
        if(sharedVector == null || sharedVector.length != valueLength)
            sharedVector = new float[valueLength];

        int loc = findKeyIndex(key);

        if(loc < 0)
            System.arraycopy(keyValues[0], 0, sharedVector, 0, valueLength);
        else if(loc >= currentSize - 1)
            System.arraycopy(keyValues[currentSize - 1], 0, sharedVector, 0, valueLength);
        else
        {
            switch(interpolationType)
            {
                case LINEAR:
                    float fraction = 0;
                    float prev_key = keys[loc];
                    float next_key = keys[loc + 1];

                    if(next_key != prev_key)
                        fraction = (key - prev_key) / (next_key - prev_key);

                    computeAngles(loc);

                    int i;
                    int vi = 0;  /* index into sets of x,y,z */
                    for (i = 0; i < normalCount; i++)
                    {
                        // scalara and scalarb are the contributions
                        // from normal[i] and normal[i+1] respectively.
                        float scalara = 
                            (float)Math.sin((1.0f-fraction)*angles[i]);
                        float scalarb = 
                            (float)Math.sin(fraction*angles[i]);

                        float x = 
                            keyValues[loc+0][vi+0]*scalara +
                            keyValues[loc+1][vi+0]*scalarb;
                        float y = 
                            keyValues[loc+0][vi+1]*scalara +
                            keyValues[loc+1][vi+1]*scalarb;
                        float z = 
                            keyValues[loc+0][vi+2]*scalara +
                            keyValues[loc+1][vi+2]*scalarb;

                        float length = (float)Math.sqrt(x*x+y*y+z*z);

                        if(length == 0)
                        {
                            // if length is 0, normals are 180 degrees
                            // apart, we cannot normalize.  Instead we
                            // snap to the closest of the end points.
                            // Note - x3d spec says:
                            // "The results are undefined if P and Q
                            // are diagonally opposite."
                            int src = (fraction<0.5f) ? loc : loc+1;
                            System.arraycopy(keyValues[src],vi,
                                             sharedVector,vi,
                                             3);
                        }
                        else
                        {
                            // store normalized vector
                            sharedVector[vi+0] = x / length;
                            sharedVector[vi+1] = y / length;
                            sharedVector[vi+2] = z / length;
                        }
                        vi += 3;
                    }
                    break;

                case STEP:
                    System.arraycopy(keyValues[loc], 0, sharedVector, 0, valueLength);
                    break;
            }
        }

        return sharedVector;
    }

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

            float[] new_keys = new float[new_size];

            System.arraycopy(keys, 0, new_keys, 0, allocatedSize);

            keys = new_keys;
            keyValues = new_values;
            allocatedSize = new_size;
        }
    }

    public String toString()
    {
        StringBuilder buf = new StringBuilder("<Normal interpolator>\n");
        buf.append("First normal for each key\n");
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
            buf.append("\n");
        }

        buf.append("</Normal interpolator>");
        return buf.toString();
    }
}
