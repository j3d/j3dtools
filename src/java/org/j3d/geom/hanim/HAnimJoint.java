/*****************************************************************************
 *                        Yumtech, Inc Copyright (c) 2004-
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.hanim;

// External imports
import java.util.ArrayList;

import javax.vecmath.Matrix4f;

// Local imports
import org.j3d.util.ErrorReporter;

/**
 * Representation of a H-Anim Joint object.
 * <p>
 *
 * The joint object is defined by
 * <a href="http://h-anim.org/Specifications/H-Anim1.1/">6.5 Joint</a>.
 * <p>
 *
 * Although the HAnim spec does not define bounding box fields, VRML/X3D
 * do, and we have an internal Grouping node to handling this joint, so
 * we've added fields and support for it anyway.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class HAnimJoint extends HAnimObject
    implements HAnimObjectParent
{
    /** Message for the array size not being long enough */
    private static final String MIN_ARRAY_SIZE_MSG =
        "The source array is either null or not long enough for HAnimJoint::";

    /** Message for trying to set a wrong child type */
    private static final String INVALID_CHILD_TYPE_MSG =
        "Invalid child type supplied. Must be one of Joint, Segment or Site";

    /** The current bboxCenter of the joint */
    protected float[] bboxCenter;

    /** The current bboxSize of the joint */
    protected float[] bboxSize;

    /** The current center of the joint */
    private float[] center;

    /** The current rotation angles of the joint */
    private float[] rotation;

    /** The current scale of the joint */
    private float[] scale;

    /** The current orientation of the scale of the joint */
    private float[] scaleOrientation;

    /** The current translation of the joint */
    private float[] translation;

    /** The current orientation for the limits of the joint */
    private float[] limitOrientation;

    /** The current lower rotation limit of the joint */
    private float[] lowerLimit;

    /** The number of items in the lower limit (raw number, not * 3) */
    private int numLower;

    /** The current upper rotation limit of the joint */
    private float[] upperLimit;

    /** The number of items in the upper limit (raw number, not * 3) */
    private int numUpper;

    /** The current stiffness values of the joint */
    private float[] stiffness;

    /** The number of items in stiffness (raw number, not * 3) */
    private int numStiffness;

    /** The skin coordinate index values into the global coordinate list */
    protected int[] skinCoordIndex;

    /** The skin coordinate weight values. If set, one for each index */
    protected float[] skinCoordWeight;

    /** The number of items in skinCoordIndex and skinCoordWeight fields */
    protected int numSkinCoord;

    /** The current collection of children nodes registered to this object. */
    protected HAnimObject[] children;

    /** The number of valid children of this object */
    protected int numChildren;

    /** The current collection of children nodes registered to this object. */
    protected HAnimDisplacer[] displacers;

    /** The number of valid children of this object */
    protected int numDisplacers;

    /** Local matrix value that combines all the fields together */
    protected Matrix4f localMatrix;

    /** Matrix that contains the global transformation down to this site */
    protected Matrix4f globalMatrix;

    /** The parent of this joint */
    protected HAnimObjectParent parent;

    /**
     * Flag to indicate the root matrix values have changed, thus needing
     * to regenerate the entire skeleton and vertices.
     */
    protected boolean matrixChanged;

    /**
     * Flag to say that we're already sent an update request and there's no
     * need to send another.
     */
    protected boolean updateSent;

    /** Reference to the array that holds the base coordinate values */
    protected float[] sourceCoords;

    /** The current number of source skin coordinates */
    protected int numSourceCoords;

    /** Reference to the array that holds the base normal values */
    protected float[] sourceNormals;

    /** The current number of source skin normals */
    protected int numSourceNormals;

    /**
     * Reference to the object that we place the modified coordinates in.
     * May be either a float[] or NIO FloatBuffer, depending on the end
     * user implementation.
     */
    protected Object outputCoords;

    /**
     * Reference to the object that we place the modified normals in.
     * May be either a float[] or NIO FloatBuffer, depending on the end
     * user implementation.
     */
    protected Object outputNormals;

    /**
     * Index of this joint into the global array of values. A value of -1
     * means that there is not the ability to know at this stage.
     */
    protected int objectIndex;

    /**
     * Create a new, default instance of the joint.
     */
    public HAnimJoint()
    {
        bboxCenter = new float[3];
        bboxSize = new float[3];
        center = new float[3];
        rotation = new float[4];
        scale = new float[] { 1, 1, 1 };
        scaleOrientation = new float[4];
        translation = new float[3];
        limitOrientation = new float[4];

        localMatrix = new Matrix4f();
        localMatrix.setIdentity();
        globalMatrix = new Matrix4f();

        rotation[2] = 1;
        scaleOrientation[2] = 1;
        limitOrientation[2] = 1;

        matrixChanged = false;
        updateSent = false;
    }

    //----------------------------------------------------------
    // Methods defined by HAnimObjectParent
    //----------------------------------------------------------

    /**
     * Notification that the child has changed and will need to recalculate
     * it's vertex positions. A change could be in the transformation matrix,
     * coordinate weights or referenced coordinates (or elsewhere).
     *
     * @param child Reference to the child that has changed
     */
    public void childUpdateRequired(HAnimObject child)
    {
        if(!updateSent)
        {
            if(parent != null)
                parent.childUpdateRequired(this);

            updateSent = true;
        }
    }

    /**
     * Get the object's index into the greater list of things. For example, a
     * joint needs to be able to update it's matrix in a large array of
     * matrices and it needs to update the same matrix every time.
     *
     * @return The index of the object into global lists
     */
    public int requestNextObjectIndex()
    {
        return (parent != null) ? parent.requestNextObjectIndex() : -1;
    }

    //----------------------------------------------------------
    // Methods defined by HAnimObject
    //----------------------------------------------------------

    /**
     * Register an error reporter with the object so that any errors generated
     * by the object can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter)
    {
        super.setErrorReporter(reporter);

        for(int i = 0; i < numChildren; i++)
            children[i].setErrorReporter(errorReporter);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set a new value for the center of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the center is taken from the 1st three values.
     *
     * @param val The new center value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setCenter(float[] val)
    {
        if(val == null || val.length < 3)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG +
                                               "center");

        center[0] = val[0];
        center[1] = val[1];
        center[2] = val[2];

        matrixChanged = true;

        sendUpdateMsg();
    }

    /**
     * Get the current value of the center.
     *
     * @param val An array of at least length 3 to copy the value to
     */
    public void getCenter(float[] val)
    {
        val[0] = center[0];
        val[1] = center[1];
        val[2] = center[2];
    }

    /**
     * Get the current value of the scale.
     *
     * @param val An array of at least length 3 to copy the value to
     */
    public void getScale(float[] val)
    {
        val[0] = scale[0];
        val[1] = scale[1];
        val[2] = scale[2];
    }

    /**
     * Set a new value for the scale of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the scale is taken from the 1st three values.
     *
     * @param val The new scale value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setScale(float[] val)
    {
        if(val == null || val.length < 3)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG +
                                               "scale");

        scale[0] = val[0];
        scale[1] = val[1];
        scale[2] = val[2];

        matrixChanged = true;

        sendUpdateMsg();
    }

    /**
     * Get the current value of the translation.
     *
     * @param val An array of at least length 3 to copy the value to
     */
    public void getTranslation(float[] val)
    {
        val[0] = translation[0];
        val[1] = translation[1];
        val[2] = translation[2];
    }

    /**
     * Set a new value for the translation of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the translation is taken from the 1st three values.
     *
     * @param val The new translation value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setTranslation(float[] val)
    {
        if(val == null || val.length < 3)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG +
                                               "translation");

        translation[0] = val[0];
        translation[1] = val[1];
        translation[2] = val[2];

        matrixChanged = true;

        sendUpdateMsg();
    }

    /**
     * Get the current value of the scaleOrientation.
     *
     * @param val An array of at least length 4 to copy the value to
     */
    public void getScaleOrientation(float[] val)
    {
        val[0] = scaleOrientation[0];
        val[1] = scaleOrientation[1];
        val[2] = scaleOrientation[2];
        val[3] = scaleOrientation[3];
    }

    /**
     * Set a new value for the scaleOrientation of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the scaleOrientation is taken from the 1st three values.
     *
     * @param val The new scaleOrientation value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setScaleOrientation(float[] val)
    {
        if(val == null || val.length < 4)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG +
                                               "scaleOrientation");

        scaleOrientation[0] = val[0];
        scaleOrientation[1] = val[1];
        scaleOrientation[2] = val[2];
        scaleOrientation[3] = val[3];

        matrixChanged = true;

        sendUpdateMsg();
    }

    /**
     * Get the current value of the rotation.
     *
     * @param val An array of at least length 4 to copy the value to
     */
    public void getRotation(float[] val)
    {
        val[0] = rotation[0];
        val[1] = rotation[1];
        val[2] = rotation[2];
        val[3] = rotation[3];
    }

    /**
     * Set a new value for the rotation of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 4 units long, and the rotation is taken from the 1st three values.
     *
     * @param val The new rotation value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setRotation(float[] val)
    {
        if(val == null || val.length < 4)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG +
                                               "rotation");

        rotation[0] = val[0];
        rotation[1] = val[1];
        rotation[2] = val[2];
        rotation[3] = val[3];

        matrixChanged = true;

        sendUpdateMsg();
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
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG +
                                               "bboxCenter");

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
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG +
                                               "bboxSize");

        bboxSize[0] = val[0];
        bboxSize[1] = val[1];
        bboxSize[2] = val[2];
    }

    /**
     * Get the current value of the limitOrientation.
     *
     * @param val An array of at least length 3 to copy the value to
     */
    public void getLimitOrientation(float[] val)
    {
        val[0] = limitOrientation[0];
        val[1] = limitOrientation[1];
        val[2] = limitOrientation[2];
        val[3] = limitOrientation[3];
    }

    /**
     * Set a new value for the limitOrientation of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 4 units long, and the limitOrientation is taken from the 1st three values.
     *
     * @param val The new limitOrientation value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setLimitOrientation(float[] val)
    {
        if(val == null || val.length < 4)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG +
                                               "limitOrientation");

        limitOrientation[0] = val[0];
        limitOrientation[1] = val[1];
        limitOrientation[2] = val[2];
        limitOrientation[3] = val[3];
    }

    /**
     * Get the number of elements in lowerLimit
     *
     * @return The number of elements kept in this lower limit
     */
    public int numLowerLimit()
    {
        return numLower / 3;
    }

    /**
     * Get the current value of the lowerLimit.
     *
     * @param val An array of at least length of numLowerLimit() * 3 to copy
     *   the values to
     */
    public void getLowerLimit(float[] val)
    {
        System.arraycopy(lowerLimit, 0, val, 0, numLower);
    }

    /**
     * Set a new value for the lowerLimit of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the lowerLimit is taken from the 1st three values.
     *
     * @param val The new lowerLimit value to use
     * @param numElements The number of 3d-vectors in the array
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setLowerLimit(float[] val, int numElements)
    {
        if(val == null || val.length < numElements * 3)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG +
                                               "lowerLimit");

        if((lowerLimit == null) || (lowerLimit.length < numElements * 3))
            lowerLimit = new float[numElements * 3];

        System.arraycopy(val, 0, lowerLimit, 0, numElements * 3);
        numLower = numElements * 3;
    }

    /**
     * Get the number of elements in upperLimit
     *
     * @return The number of elements kept in this upper limit
     */
    public int numUpperLimit()
    {
        return numUpper / 3;
    }

    /**
     * Get the current value of the upperLimit.
     *
     * @param val An array of at least length of numUpperLimit() * 3 to copy
     *   the values to
     */
    public void getUpperLimit(float[] val)
    {
        System.arraycopy(upperLimit, 0, val, 0, numUpper);
    }

    /**
     * Set a new value for the upperLimit of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the upperLimit is taken from the 1st three values.
     *
     * @param val The new upperLimit value to use
     * @param numElements The number of 3d-vectors in the array
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setUpperLimit(float[] val, int numElements)
    {
        if(val == null || val.length < numElements * 3)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG +
                                               "upperLimit");

        if((upperLimit == null) || (upperLimit.length < numElements * 3))
            upperLimit = new float[numElements * 3];

        System.arraycopy(val, 0, upperLimit, 0, numElements * 3);
        numUpper = numElements * 3;
    }

    /**
     * Get the number of elements in stiffness
     *
     * @return The number of elements kept in this stiffness list
     */
    public int numStiffness()
    {
        return numStiffness;
    }

    /**
     * Get the current value of the stiffness.
     *
     * @param val An array of at least length of numStiffness() * 3 to copy
     *   the values to
     */
    public void getStiffness(float[] val)
    {
        System.arraycopy(stiffness, 0, val, 0, numStiffness);
    }

    /**
     * Set a new value for the stiffness of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the stiffness is taken from the 1st three values.
     *
     * @param val The new stiffness value to use
     * @param numElements The number of values in the array
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setStiffness(float[] val, int numElements)
    {
        if(val == null || val.length < numElements)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG +
                                               "stiffness");

        if((stiffness == null) || (stiffness.length < numElements))
            stiffness = new float[numElements];

        System.arraycopy(val, 0, stiffness, 0, numElements);
        numStiffness = numElements;
    }

    /**
     * Get the number of elements in the skinCoordIndex field, and by
     * association skinCoordWeight (if any weights have been set).
     *
     * @return The number of value kept in the skin coordinate index
     */
    public int numSkinCoord()
    {
        return numSkinCoord;
    }

    /**
     * Get the current value of the skinCoordIndex. If no weights are set, the
     * array is left unchanged.
     *
     * @param val An array of at least length of numSkinCoord() to copy
     *   the values to
     */
    public void getSkinCoordIndex(float[] val)
    {
        System.arraycopy(skinCoordIndex, 0, val, 0, numSkinCoord);
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
    public void setSkinCoordIndex(int[] val, int numValid)
    {
        if(val == null || val.length < numValid)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG +
                                               "skinCoordIndex");

        if((skinCoordIndex == null) || (skinCoordIndex.length < numValid))
        {
            skinCoordIndex = new int[numValid];

            // resize the weight list while we're at it just to avoid issues
            // with a user hitting two different list sizes.
            float[] tmp = new float[numValid];

            if(skinCoordWeight != null)
                System.arraycopy(skinCoordWeight, 0, tmp, 0, numSkinCoord);

            skinCoordWeight = tmp;
        }

        System.arraycopy(val, 0, skinCoordIndex, 0, numValid);
        numSkinCoord = numValid;

        sendUpdateMsg();
    }

    /**
     * Get the current value of the skinCoordWeight. If no weights are set, the
     * array is left unchanged.
     *
     * @param val An array of at least length of numSkinCoord() to copy
     *   the values to
     */
    public void getSkinCoordWeight(float[] val)
    {
        System.arraycopy(skinCoordWeight, 0, val, 0, numSkinCoord);
    }

    /**
     * Set a new value for the skinCoordWeight of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * as long as the currently set skinCoordIndex values length.
     *
     * @param val The new skinCoordWeight value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setSkinCoordWeight(float[] val)
    {
        if(val == null || val.length < numSkinCoord)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG +
                                               "skinCoordWeight");

        // No need to resize here as it should have already been done by
        // setSkinCoordIndex.

        System.arraycopy(val, 0, skinCoordWeight, 0, numSkinCoord);
        numSkinCoord = numSkinCoord;

        sendUpdateMsg();
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
    public void getChildren(HAnimObject[] vals)
    {
        System.arraycopy(children, 0, vals, 0, numChildren);
    }

    /**
     * Replace the existing children with the new set of children.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     * @throws IllegalArgumentException The child is not one of a Joint,
     *    Segment or Site
     */
    public void setChildren(HAnimObject[] kids, int numValid)
    {
        if(children == null || children.length < numValid)
            children = new HAnimObject[numValid];

        for(int i = 0; i < numValid; i++)
        {
            if(!(kids[i] instanceof HAnimJoint ||
                 kids[i] instanceof HAnimSegment ||
                 kids[i] instanceof HAnimSite))
                throw new IllegalArgumentException(INVALID_CHILD_TYPE_MSG);

            children[i] = kids[i];

            if(kids[i] instanceof HAnimJoint)
                ((HAnimJoint)kids[i]).setParent(this,
                                                sourceCoords,
                                                numSourceCoords,
                                                sourceNormals,
                                                numSourceNormals,
                                                outputCoords,
                                                outputNormals);
            else if(kids[i] instanceof HAnimSite)
                ((HAnimSite)kids[i]).setParent(this);
        }

        for(int i = numValid; i < numChildren; i++)
            children[i] = null;

        numChildren = numValid;

        sendUpdateMsg();
    }

    /**
     * Add a child node to the existing collection. Duplicates and null values
     * are allowed.
     *
     * @param kid The new child instance to add
     * @throws IllegalArgumentException The child is not one of a Joint,
     *    Segment or Site
     */
    public void addChild(HAnimObject kid)
    {
        if(!(kid instanceof HAnimJoint ||
             kid instanceof HAnimSegment ||
             kid instanceof HAnimSite))
            throw new IllegalArgumentException(INVALID_CHILD_TYPE_MSG);

        if(children == null || children.length == numChildren)
        {
            HAnimObject[] tmp = new HAnimObject[numChildren + 4];

            if(numDisplacers != 0)
                System.arraycopy(children, 0, tmp, 0, numChildren);

            children = tmp;
        }

        children[numChildren++] = kid;
        kid.setErrorReporter(errorReporter);

        if(kid instanceof HAnimJoint)
            ((HAnimJoint)kid).setParent(this,
                                        sourceCoords,
                                        numSourceCoords,
                                        sourceNormals,
                                        numSourceNormals,
                                        outputCoords,
                                        outputNormals);
        else if(kid instanceof HAnimSite)
            ((HAnimSite)kid).setParent(this);

        sendUpdateMsg();
    }

    /**
     * Remove a child node from the existing collection. If there are
     * duplicates, only the first instance is removed. Only reference
     * comparisons are used.
     *
     * @param kid The child instance to remove
     * @throws IllegalArgumentException The child is not one of a Joint,
     *    Segment or Site
     */
    public void removeChild(HAnimObject kid)
    {
        for(int i = 0; i < numChildren; i++)
        {
            if(children[i] == kid)
            {
                System.arraycopy(children, i + 1, children, i, numChildren - i - 1);
                break;
            }
        }

        numChildren--;
    }

    /**
     * Get the number of currently valid children.
     *
     * @return The number of values kept in the info list
     */
    public int numDisplacers()
    {
        return numDisplacers;
    }

    /**
     * Get the current collection of children. If none are set, the array is
     * unchanged.
     *
     * @param vals An array of at least length numChildren() to copy the values to
     */
    public void getDisplacers(HAnimDisplacer[] vals)
    {
        System.arraycopy(displacers, 0, vals, 0, numDisplacers);
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

        sendUpdateMsg();
    }

    /**
     * Add a child node to the existing collection. Duplicates and null values
     * are allowed.
     *
     * @param kid The new child instance to add
     */
    public void addDisplacer(HAnimDisplacer kid)
    {
        if(displacers == null || displacers.length == numDisplacers)
        {
            HAnimDisplacer[] tmp = new HAnimDisplacer[numDisplacers + 4];

            if(numDisplacers != 0)
                System.arraycopy(displacers, 0, tmp, 0, numDisplacers);

            displacers = tmp;
        }

        displacers[numDisplacers++] = kid;

        sendUpdateMsg();
    }

    /**
     * Remove a child node from the existing collection. If there are
     * duplicates, only the first instance is removed. Only reference
     * comparisons are used.
     *
     * @param kid The child instance to remove
     */
    public void removeDisplacer(HAnimDisplacer kid)
    {
        for(int i = 0; i < numDisplacers; i++)
        {
            if(displacers[i] == kid)
            {
                System.arraycopy(displacers,
                                 i + 1,
                                 displacers,
                                 i,
                                 numDisplacers - i - 1);

                sendUpdateMsg();
                break;
            }
        }
    }

    /**
     * Set the parent of this node to the given reference. Any previous
     * reference is removed.
     *
     * @param parent The new parent instance to use
     * @param srcCoords The array for the original, unmodified coordinates
     * @param numCoords Number of valid coordinate values
     * @param srcNormals The array for the original, unmodified normals
     * @param numNormals Number of valid normal values
     * @param destCoords The array/buffer for the transformed coordinates
     * @param destNormals The array/buffer for the transformed normals
     */
    protected void setParent(HAnimObjectParent parent,
                             float[] srcCoords,
                             int numCoords,
                             float[] srcNormals,
                             int numNormals,
                             Object destCoords,
                             Object destNormals)
    {
        this.parent = parent;

        sourceCoords = srcCoords;
        sourceNormals = srcNormals;
        outputCoords = destCoords;
        outputNormals = destNormals;

        numSourceCoords = numCoords;
        numSourceNormals = numNormals;

        // Fetch an object ID for ourselves
        objectIndex = parent.requestNextObjectIndex();

        for(int i = 0; i < numChildren; i++)
        {
            if(children[i] instanceof HAnimJoint)
                ((HAnimJoint)children[i]).setParent(this,
                                                    srcCoords,
                                                    numCoords,
                                                    srcNormals,
                                                    numNormals,
                                                    destCoords,
                                                    destNormals);
            else if(children[i] instanceof HAnimSite)
                ((HAnimSite)children[i]).setParent(this);
        }

    }

    /**
     * The global coordinate and/or normal values were changed by the user,
     * so go through and update the local references.
     *
     * @param srcCoords The array for the original, unmodified coordinates
     * @param numCoords Number of valid coordinate values
     * @param srcNormals The array for the original, unmodified normals
     * @param numNormals Number of valid normal values
     * @param destCoords The array/buffer for the transformed coordinates
     * @param destNormals The array/buffer for the transformed normals
     */
    protected void updateSources(float[] srcCoords,
                                 int numCoords,
                                 float[] srcNormals,
                                 int numNormals,
                                 Object destCoords,
                                 Object destNormals)
    {
        sourceCoords = srcCoords;
        sourceNormals = srcNormals;
        outputCoords = destCoords;
        outputNormals = destNormals;

        numSourceCoords = numCoords;
        numSourceNormals = numNormals;

        for(int i = 0; i < numChildren; i++)
        {
            if(children[i] instanceof HAnimJoint)
                ((HAnimJoint)children[i]).updateSources(srcCoords,
                                                        numCoords,
                                                        srcNormals,
                                                        numNormals,
                                                        destCoords,
                                                        destNormals);
        }
    }

    /**
     * All the skeletal changes are in for this frame, so update the matrix
     * values now.
     *
     * @param parentTransform The transformation into global coordinates of
     *   the parent of this joint
     * @param parentChanged Flag to indicate that the parent transformation
     *   matrix has changed or is still the same as last call
     */
    protected void updateSkeleton(Matrix4f parentTransform,
                                  boolean parentChanged)
    {
        boolean has_changed = parentChanged || matrixChanged;

        if(matrixChanged)
        {
            updateMatrix(center,
                         rotation,
                         scale,
                         scaleOrientation,
                         translation,
                         localMatrix);

            matrixChanged = false;
        }

        if(has_changed)
            globalMatrix.mul(parentTransform, localMatrix);

        for(int i = 0; i < numChildren; i++)
        {
            if(children[i] instanceof HAnimJoint)
                ((HAnimJoint)children[i]).updateSkeleton(globalMatrix,
                                                         has_changed);
            else if(children[i] instanceof HAnimSite)
                ((HAnimSite)children[i]).updateLocation(globalMatrix,
                                                        has_changed);
        }

        updateSent = false;
    }

    /**
     * Send an update message to the parent, if one has not already been sent.
     */
    protected void sendUpdateMsg()
    {
        if(!updateSent && parent != null)
        {
            parent.childUpdateRequired(this);
            updateSent = true;
        }
    }
}
