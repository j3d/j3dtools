/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.hanim;

// External imports
// None

// Local imports
// None

/**
 * Representation of a H-Anim Displacer object.
 * <p>
 *
 * The joint object is defined by
 * <a href="http://h-anim.org/Specifications/H-Anim1.1/">6.5 Displacer</a>.
 *
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class HAnimDisplacer extends HAnimObject
{
    /** Message for the array size not being long enough */
    private static final String MIN_ARRAY_SIZE_MSG =
        "The source array is either null or not long enough";

    /** The current coordinates of the segment */
    private int[] coordIndex;

    /** The number of items in the coordinate list (raw number, not * 3) */
    private int numCoordIndex;

    /** The current displacements of the displacer */
    private float[] displacements;

    /** The number of items in the displacement list */
    private int numDisplacements;

    /** The uniform weight to apply to all these displacements */
    private float weight;

    /**
     * Construct a default instance of the displacer.
     */
    public HAnimDisplacer()
    {
        weight = 1;
    }

    /**
     * Get the current value of the weight to be applied to the displacements.
     *
     * @param return A non-negative value
     */
    public float getWeight()
    {
        return weight;
    }

    /**
     * Set a new value for the weight for the displacements. If no weight value
     * is to be used, then a value of 1 should be set (the default).
     *
     * @param val The new weight value to use
     */
    public void setWeight(float val)
    {
        weight = val;
    }

    /**
     * Get the number of elements in the skinDisplacements field, and by
     * association skinCoordWeight (if any weights have been set).
     *
     * @return The number of value kept in the skin coordinate index
     */
    public int numDisplacements()
    {
        return numDisplacements;
    }

    /**
     * Get the current value of the skinDisplacements. If no weights are set, the
     * array is left unchanged.
     *
     * @param val An array of at least length of numCoord() to copy
     *   the values to
     */
    public void getDisplacements(float[] val)
    {
        System.arraycopy(displacements, 0, val, 0, numDisplacements);
    }

    /**
     * Set a new value for the skinDisplacements of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * as long as the numValid field value.
     *
     * @param val The new skinDisplacements value to use
     * @param numValid The number of valid values to read from the index list
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setDisplacements(float[] val, int numValid)
    {
        if(val == null || val.length < numValid)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG);

        System.arraycopy(val, 0, displacements, 0, numValid);
        numDisplacements = numValid;
    }

    /**
     * Get the number of elements in the skinCoordIndex field, and by
     * association skinCoordWeight (if any weights have been set).
     *
     * @return The number of value kept in the skin coordinate index
     */
    public int numCoordIndex()
    {
        return numCoordIndex;
    }

    /**
     * Get the current value of the skinCoordIndex. If no weights are set, the
     * array is left unchanged.
     *
     * @param val An array of at least length of numCoord() to copy
     *   the values to
     */
    public void getCoordIndex(float[] val)
    {
        System.arraycopy(coordIndex, 0, val, 0, numCoordIndex);
    }

    /**
     * Set a new value for the skinCoordIndex of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * as long as the numValid field value.
     *
     * @param val The new skinCoordIndex value to use
     * @param numValid The number of valid values to read from the index list
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setCoordIndex(int[] val, int numValid)
    {
        if(val == null || val.length < numValid)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG);

        if((coordIndex == null) || (coordIndex.length < numValid))
        {
            coordIndex = new int[numValid];

            // resize the displacements list while we're at it just to avoid
            // issues with a user hitting two different list sizes.
            float[] tmp = new float[numValid];

            if(displacements != null)
                System.arraycopy(displacements, 0, tmp, 0, numCoordIndex);

            displacements = tmp;
        }

        System.arraycopy(val, 0, coordIndex, 0, numValid);
        numCoordIndex = numValid;
    }
}
