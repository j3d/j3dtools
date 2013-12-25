/*****************************************************************************
 *                      J3D.org Copyright (c) 2000
 *                           Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

// External imports
// none

// Local imports
// none

/**
 * Heavily customised interpolator for determining texture coordinates to
 * apply to a given particle.
 * <P>
 *
 * Public definition to allow renderer-specific particle system implementations
 * to request the interpolation, but not allowed to be created or set up
 * outside of this package.
 *
 * @author Justin Couch
 * @version $Revision: 2.0 $
 */
public class TexCoordInterpolator
{

    /** The key values indexed as [index * (s, t, r, angle)] */
    private float[] keyValues;

    /** The number of tex coords per particle. */
    private final int texCoordSize;

    /** Current total number of items in the array */
    protected int currentSize;

    /** The keys as a single array for fast searching */
    protected float[] keys;

    /**
     * Create a new linear interpolator instance with the default size for the
     * number of key values.
     */
    TexCoordInterpolator(int texCoordSize)
    {
        this.texCoordSize = texCoordSize;
    }

    /**
     * Set the keys and texture coordinates to use. Makes an internal copy of
     * these days.
     *
     * @param times The list of time keys to use
     * @param numEntries The number of keys/keyValue pairs
     * @param texCoords The raw texture coordinates
     */
    void setupInterpolants(float[] times, int numEntries, float[] texCoords)
    {
        if((keys == null) || (keys.length < numEntries))
        {
            keys = new float[numEntries];
            keyValues = new float[numEntries * texCoordSize * 2];
        }

        System.arraycopy(times, 0, keys, 0, numEntries);
        System.arraycopy(texCoords, 0, keyValues, 0, numEntries * texCoordSize * 2);
        currentSize = numEntries;
    }

    /**
     * Go find the appropriate set of texture coordinates now.
     *
     * @param key The key value to get the position for
     * @param outputCoords An array of the values at that position [x, y, z]
     */
    public void interpolate(float key, int offset, float[] outputCoords)
    {
        int loc = findKeyIndex(key);

        switch(texCoordSize)
        {
            case 2:
                outputCoords[offset] = keyValues[loc * 4];
                outputCoords[offset + 1] = keyValues[loc * 4 + 1];
                outputCoords[offset + 2] = keyValues[loc * 4 + 2];
                outputCoords[offset + 3] = keyValues[loc * 4 + 3];
                break;

            case 3:
                outputCoords[offset] = keyValues[loc * 6];
                outputCoords[offset + 1] = keyValues[loc * 6 + 1];
                outputCoords[offset + 2] = keyValues[loc * 6 + 2];
                outputCoords[offset + 3] = keyValues[loc * 6 + 3];
                outputCoords[offset + 4] = keyValues[loc * 6 + 4];
                outputCoords[offset + 5] = keyValues[loc * 6 + 5];
                break;

            case 4:
                outputCoords[offset] = keyValues[loc * 8];
                outputCoords[offset + 1] = keyValues[loc * 8 + 1];
                outputCoords[offset + 2] = keyValues[loc * 8 + 2];
                outputCoords[offset + 3] = keyValues[loc * 8 + 3];
                outputCoords[offset + 4] = keyValues[loc * 8 + 4];
                outputCoords[offset + 5] = keyValues[loc * 8 + 5];
                outputCoords[offset + 6] = keyValues[loc * 8 + 6];
                outputCoords[offset + 7] = keyValues[loc * 8 + 7];
                break;
        }
    }

    //---------------------------------------------------------------
    // Misc Internal methods
    //---------------------------------------------------------------

    /**
     * Find the key in the array. Performs a fast binary search of the values
     * to locate the right index.  Returns the index i such that
     * key[i]<key<=key[i+1].  If the key is less than or equal to all
     * keys, returns 0.
     * The binary search is O(log n).
     *
     * @param key The key to search for
     * @return The index i such that key[i]<key<=key[i+1].
     */
    private final int findKeyIndex(float key)
    {
        // some special case stuff - check the extents of the array to avoid
        // the binary search
        if((currentSize == 0) || (key <= keys[0]))
            return 0;
        else if(key >= keys[currentSize - 1])
            return currentSize - 1;

        int start = 0;
        int end = currentSize - 1;
        int mid = currentSize >> 1;  // identical to (start + end + 1) >> 1

        // Non-recursive binary search.
        // Searches for the largest i such that keys[i]<key.
        // Differs a little from a classical binary search
        // in that we cannot discard the middle value from
        // the search when key>keys[mid] (because keys[mid] may
        // turn out to be the best solution, and we cannot
        // terminate when key==keys[mid] (because there may be
        // more than one i with keys[i]==key, and we must find the
        // first one.
        // Round up when computing the new mid value to avoid
        // a possible infinite loop with start==mid<end.

        while(start < end)
        {
            float test = keys[mid];

            if(test >= key)
                end = mid - 1;
            else
                start = mid;     // note we don't exclude mid from range

            // We recompute mid at the end so that
            // it is correct when loop terminates.
            // Note that we round up.  This is required to avoid
            // getting stuck with mid==start.
            mid = (start + end + 1) >> 1;
        }

        return mid;
    }
}
