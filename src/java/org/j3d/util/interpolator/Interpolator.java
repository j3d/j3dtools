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
// none

// Application specific imports
// none

/**
 * An interpolator that works with positional coordinates.
 * <P>
 *
 * The interpolation routine is just a simple linear interpolation between
 * each of the points. The interpolator may take arbitrarily spaced keyframes
 * and compute correct values.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public abstract class Interpolator
{
    /** The interpolator should act as a linear interpolator between keys */
    public static final int LINEAR = 1;

    /** The interpolator should act as a step interpolator between keys */
    public static final int STEP = 2;

    /** The default number of items in the interpolator */
    protected static final int DEFAULT_SIZE = 20;

    /** The number of items to increment the array with */
    protected static final int ARRAY_INCREMENT = 5;

    /** The current size of the array data */
    protected int allocatedSize;

    /** Current total number of items in the array */
    protected int currentSize;

    /** The keys as a single array for fast searching */
    protected float[] keys;

    /** The type of interpolation routine to use */
    protected final int interpolationType;

    /**
     * Create a new interpolator instance with the default size for the number
     * of key values.
     */
    protected Interpolator()
    {
        this(DEFAULT_SIZE, LINEAR);
    }

    /**
     * Create an interpolator with the given basic size.
     *
     * @param size The starting number of items in interpolator
     */
    protected Interpolator(int size)
    {
        this(size, LINEAR);
    }

    /**
     * Create a interpolator with the given basic size and interpolation
     * type.
     *
     * @param size The starting number of items in interpolator
     * @param type The type of interpolation routine to do
     */
    protected Interpolator(int size, int type)
    {
        interpolationType = type;

        keys = new float[size];
    }

    /**
     * Reset the interpolator to be empty so that new key values are replacing
     * the old ones.
     */
    public void clear()
    {
        currentSize = 0;
    }

    //---------------------------------------------------------------
    // Misc Internal methods
    //---------------------------------------------------------------

    /**
     * Find the key in the array. Performs a fast binary search of the values
     * to locate the right index.  Returns the index i such that
     * key[i]<key<=key[i+1].  If the key is less than or equal to all
     * keys, returns -1.
     * The binary search is O(log n).
     *
     * @param key The key to search for
     * @return The index i such that key[i]<key<=key[i+1].
     */
    protected int findKeyIndex(float key)
    {
        // some special case stuff - check the extents of the array to avoid
        // the binary search
        if((currentSize == 0) || (key <= keys[0]))
            return -1;
        else if(key == keys[currentSize - 1])
            return currentSize - 1;
        else if(key > keys[currentSize - 1])
        // REVISIT - this return value is an exception from the general
        // pattern.  I think currentSize-1 would make more sense here.
        // I wont change it since it appears to be working.
        // [GC 21-Oct-2002]
            return currentSize;

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
