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
import java.util.ArrayList;

// Local imports
// None

/**
 * Representation of a H-Anim Segment object.
 * <p>
 *
 * The segment object is defined by
 * <a href="http://h-anim.org/Specifications/H-Anim1.1/">6.5 Segment</a>.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class HAnimSegment extends HAnimObject
{
    /** Message for the array size not being long enough */
    private static final String MIN_ARRAY_SIZE_MSG =
        "The source array is either null or not long enough";

    /** The current bboxCenter of the segment */
    protected float[] bboxCenter;

    /** The current bboxSize of the segment */
    protected float[] bboxSize;

    /** The current bboxCenter of the segment */
    private float[] centerOfMass;

    /** The moments of inertia segment (length 9)*/
    private float[] momentsOfInertia;

    /** The number of valid values in momentsOfInertia */
    private int numMoments;

    /** The current coordinates of the segment */
    private float[] coords;

    /** The number of items in the coordinate list (raw number, not * 3) */
    private int numCoords;

    /** The current collection of children nodes registered to this object. */
    protected Object[] children;

    /** The number of valid children of this object */
    protected int numChildren;

    /** The current collection of children nodes registered to this object. */
    private HAnimDisplacer[] displacers;

    /** The number of valid children of this object */
    private int numDisplacers;

    /** The mass of this segment. Defaults to -1 */
    private float mass;

    /**
     * Create a new, default instance of the segment.
     */
    public HAnimSegment()
    {
        bboxCenter = new float[3];
        bboxSize = new float[3];
        momentsOfInertia = new float[9];
        centerOfMass = new float[3];
        mass = -1;
    }

    /**
     * Get the current value of the mass of the segment. A value of -1 indicates
     * that no mass is available.
     *
     * @return A positive value or -1.
     */
    public float getMass()
    {
        return mass;
    }

    /**
     * Set a new value for the mass of this segment. If no mass value is to be
     * used, then a value of -1 should be set (the default).
     *
     * @param val The new mass value to use
     */
    public void setMass(float val)
    {
        mass = val;
    }

    /**
     * Get the current value of the bboxCenter.
     *
     * @param val An array of at least length 3 to copy the value to
     */
    public void getBboxCenter(float[] val)
    {
        val[0] = bboxCenter[0];
        val[1] = bboxCenter[1];
        val[2] = bboxCenter[2];
    }

    /**
     * Set a new value for the bboxCenter of this segment. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the bboxCenter is taken from the 1st three values.
     *
     * @param val The new bboxCenter value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setBboxCenter(float[] val)
    {
        if(val == null || val.length < 3)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG);

        bboxCenter[0] = val[0];
        bboxCenter[1] = val[1];
        bboxCenter[2] = val[2];
    }

    /**
     * Get the current value of the bboxSize.
     *
     * @param val An array of at least length 3 to copy the value to
     */
    public void getBboxSize(float[] val)
    {
        val[0] = bboxSize[0];
        val[1] = bboxSize[1];
        val[2] = bboxSize[2];
    }

    /**
     * Set a new value for the bboxSize of this segment. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the bboxSize is taken from the 1st three values.
     * <p>
     *
     * If the three values are all -1, then this will disable the use of the
     * explicit bounds.
     *
     * @param val The new bboxSize value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setBboxSize(float[] val)
    {
        if(val == null || val.length < 3)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG);

        bboxSize[0] = val[0];
        bboxSize[1] = val[1];
        bboxSize[2] = val[2];
    }

    /**
     * Get the current value of the centerOfMass.
     *
     * @param val An array of at least length 3 to copy the value to
     */
    public void getCenterOfMass(float[] val)
    {
        val[0] = centerOfMass[0];
        val[1] = centerOfMass[1];
        val[2] = centerOfMass[2];
    }

    /**
     * Set a new value for the centerOfMass of this segment. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the centerOfMass is taken from the 1st three values.
     *
     * @param val The new centerOfMass value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setCenterOfMass(float[] val)
    {
        if(val == null || val.length < 3)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG);

        centerOfMass[0] = val[0];
        centerOfMass[1] = val[1];
        centerOfMass[2] = val[2];
    }

    public int numMomentsOfInertia()
    {
        return numMoments;
    }

    /**
     * Get the current value of the momentsOfInertia.
     *
     * @param val An array of at least numMomentsOfInteria() in length
     */
    public void getMomentsOfInertia(float[] val)
    {
        System.arraycopy(momentsOfInertia, 0, val, 0, numMoments);
    }

    /**
     * Set a new value for the momentsOfInertia of this segment. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the momentsOfInertia is taken from the 1st three values.
     *
     * @param val The new momentsOfInertia value to use
     * @param numValid The number of valid values to copy from the array
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setMomentsOfInertia(float[] val, int numValid)
    {
        if(val == null)
        {
            numMoments = 0;
        }
        else
        {
            if(momentsOfInertia.length < numValid)
                momentsOfInertia = new float[numValid];

            System.arraycopy(val, 0, momentsOfInertia, 0, numValid);
            numMoments = numValid;
        }
    }

    /**
     * Get the number of elements in coord
     *
     * @return The number of elements kept in the coordinates list
     */
    public int numCoord()
    {
        return numCoords / 3;
    }

    /**
     * Get the current value of the coord.
     *
     * @param val An array of at least length of numCoord() * 3 to copy
     *   the values to
     */
    public void getCoord(float[] val)
    {
        System.arraycopy(coords, 0, val, 0, numCoords);
    }

    /**
     * Set a new value for the coord of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * a multiple of 3 units long.
     *
     * @param val The new coord value to use
     * @param numElements The number of 3d-vectors in the array
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setCoord(float[] val, int numElements)
    {
        if(val == null || val.length < numElements * 3)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG);

        if((coords == null) || (coords.length < numElements * 3))
            coords = new float[numElements * 3];

        System.arraycopy(val, 0, coords, 0, numElements * 3);
        numCoords = numElements * 3;
    }

    /**
     * Get the number of currently valid children.
     *
     * @return The number of values kept in the info list
     */
    public int numChildren()
    {
        return numChildren;
    }

    /**
     * Get the current collection of children. If none are set, the array is
     * unchanged.
     *
     * @param vals An array of at least length numChildren() to copy the values to
     */
    public void getChildren(Object[] vals)
    {
        System.arraycopy(children, 0, vals, 0, numChildren);
    }

    /**
     * Replace the existing children with the new set of children.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     */
    public void setChildren(Object[] kids, int numValid)
    {
        if(children == null || children.length < numValid)
            children = new Object[numValid];

        if(numValid != 0)
            System.arraycopy(kids, 0, children, 0, numValid);

        for(int i = numValid; i < numChildren; i++)
            children[i] = null;

        numChildren = numValid;
    }

    /**
     * Add a child node to the existing collection. Duplicates and null values
     * are allowed.
     *
     * @param kid The new child instance to add
     */
    public void addChild(Object kid)
    {
        if(children == null || children.length == numChildren)
            children = new Object[numChildren + 4];

        children[numChildren++] = kid;
    }

    /**
     * Remove a child node from the existing collection. If there are
     * duplicates, only the first instance is removed. Only reference
     * comparisons are used.
     *
     * @param kid The child instance to remove
     */
    public void removeChild(Object kid)
    {
        for(int i = 0; i < numChildren; i++)
        {
            if(children[i] == kid)
            {
                System.arraycopy(children, i + 1, children, i, numChildren - i - 1);
                break;
            }
        }
    }

    /**
     * Replace the existing displacers with the new set of displacers.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     */
    public void setDisplacers(HAnimDisplacer[] kids, int numValid)
    {
        if(displacers == null || displacers.length < numValid)
            displacers = new HAnimDisplacer[numValid];

        if(numValid != 0)
            System.arraycopy(kids, 0, displacers, 0, numValid);

        for(int i = numValid; i < numDisplacers; i++)
            displacers[i] = null;

        numDisplacers = numValid;
    }
}
