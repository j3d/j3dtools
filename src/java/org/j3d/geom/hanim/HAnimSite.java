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
import javax.vecmath.Matrix4f;

// Local imports
// None

/**
 * Representation of a H-Anim Site object.
 * <p>
 *
 * The site object is defined by
 * <a href="http://h-anim.org/Specifications/H-Anim1.1/">6.5 Site</a>.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class HAnimSite extends HAnimObject
{
    /** Message for the array size not being long enough */
    private static final String MIN_ARRAY_SIZE_MSG =
        "The source array is either null or not long enough";

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

    /** The current collection of children nodes registered to this object. */
    protected Object[] children;

    /** The number of valid children of this object */
    protected int numChildren;

    /** Local matrix value that combines all the fields together */
    protected Matrix4f localMatrix;

    /** Matrix that contains the global transformation down to this site */
    protected Matrix4f globalMatrix;

    /** The parent of this joint */
    private HAnimObjectParent parent;

    /**
     * Flag to say that we're already sent an update request and there's no
     * need to send another.
     */
    private boolean updateSent;

    /**
     * Flag to indicate the root matrix values have changed, thus needing
     * to regenerate the entire skeleton and vertices.
     */
    protected boolean matrixChanged;

    /**
     * Create a new, default instance of the site.
     */
    public HAnimSite()
    {
        center = new float[3];
        rotation = new float[4];
        scale = new float[] { 1, 1, 1 };
        scaleOrientation = new float[4];
        translation = new float[3];

        rotation[2] = 1;
        scaleOrientation[2] = 1;

        localMatrix = new Matrix4f();
        globalMatrix = new Matrix4f();

        matrixChanged = false;
        updateSent = false;
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
     * Set a new value for the center of this site. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the center is taken from the 1st three values.
     *
     * @param val The new center value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setCenter(float[] val)
    {
        if(val == null || val.length < 3)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG);

        center[0] = val[0];
        center[1] = val[1];
        center[2] = val[2];

        matrixChanged = true;

        sendUpdateMsg();
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
     * Set a new value for the scale of this site. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the scale is taken from the 1st three values.
     *
     * @param val The new scale value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setScale(float[] val)
    {
        if(val == null || val.length < 3)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG);

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
     * Set a new value for the translation of this site. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the translation is taken from the 1st three values.
     *
     * @param val The new translation value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setTranslation(float[] val)
    {
        if(val == null || val.length < 4)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG);

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
     * Set a new value for the scaleOrientation of this site. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the scaleOrientation is taken from the 1st three values.
     *
     * @param val The new scaleOrientation value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setScaleOrientation(float[] val)
    {
        if(val == null || val.length < 4)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG);

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
     * Set a new value for the rotation of this site. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the rotation is taken from the 1st three values.
     *
     * @param val The new rotation value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setRotation(float[] val)
    {
        if(val == null || val.length < 4)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG);

        rotation[0] = val[0];
        rotation[1] = val[1];
        rotation[2] = val[2];
        rotation[3] = val[3];

        matrixChanged = true;
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
     * @param val An array of at least length numChildren() to copy the values to
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
            children = new Object[numChildren];

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
     * Set the parent of this node to the given reference. Any previous
     * reference is removed.
     *
     * @param parent The new parent instance to use
     */
    protected void setParent(HAnimObjectParent parent)
    {
        this.parent = parent;
    }

    /**
     * All the skeletal changes are in for this frame, so update the matrix
     * values now. This should not be callable by the general public. Derived
     * classes may override this method, but should call it as well to ensure
     * the internal matrices are correctly updated.
     *
     * @param parentTransform The transformation into global coordinates of
     *   the parent of this joint
     * @param parentChanged Flag to indicate that the parent transformation
     *   matrix has changed or is still the same as last call
     */
    protected void updateLocation(Matrix4f parentTransform,
                                  boolean parentChanged)
    {
        if(matrixChanged)
        {
            updateMatrix(center,
                         rotation,
                         scale,
                         scaleOrientation,
                         translation,
                         localMatrix);
        }

        if(parentChanged || matrixChanged)
            globalMatrix.mul(parentTransform, localMatrix);

        updateSent = false;
        matrixChanged = false;
    }

    /**
     * Send an update message to the parent, if one has not already been sent.
     */
    private void sendUpdateMsg()
    {
        if(!updateSent && parent != null)
        {
            parent.childUpdateRequired(this);
            updateSent = true;
        }
    }
}
