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

import javax.vecmath.Matrix4f;

// Local imports
// None

/**
 * Common base representation of a H-Anim Humanoid object.
 * <p>
 *
 * The Humanoid object is defined by
 * <a href="http://h-anim.org/Specifications/H-Anim1.1/">6.2 Humanoid</a>.
 * <p>
 *
 * Derived classes must set a collection of renderer-specific values. The most
 * critical is the output object.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class HAnimHumanoid extends HAnimObject
    implements HAnimObjectParent
{
    /** Message for the array size not being long enough */
    private static final String MIN_ARRAY_SIZE_MSG =
        "The source array is either null or not long enough";

    /** Message for trying to set a wrong child type */
    private static final String INVALID_CHILD_TYPE_MSG =
        "Invalid child type supplied. Must be one of Joint or Site";

    /** The identity matrix to pass through to the other nodes */
    private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();

    /** The current bboxCenter of the segment */
    protected float[] bboxCenter;

    /** The current bboxSize of the segment */
    protected float[] bboxSize;

    /** The current center of the site */
    private float[] center;

    /** The current center of the site */
    private float[] rotation;

    /** The current center of the site */
    private float[] scale;

    /** The current center of the site */
    private float[] scaleOrientation;

    /** The current center of the site */
    private float[] translation;

    /** The current coordinates of the segment */
    protected float[] skinCoords;

    /** The number of items in the coordinate list (raw number, not * 3) */
    protected int numSkinCoords;

    /** The current coordinates of the segment */
    protected float[] skinNormals;

    /** The number of items in the coordinate list (raw number, not * 3) */
    protected int numSkinNormals;

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

    /** The current collection of viewpoints nodes registered to this object. */
    protected Object[] viewpoints;

    /** The number of valid viewpoints of this object */
    protected int numViewpoints;

    /** The version info associated with the humanoid */
    private String version;

    /** The collection of information strings */
    private String[] info;

    /** The current collection of site nodes registered to this object. */
    private HAnimSite[] sites;

    /** The number of valid sites of this object */
    private int numSites;

    /** The current collection of joints nodes registered to this object. */
    private HAnimJoint[] joints;

    /** The number of valid joints of this object */
    private int numJoints;

    /** The current collection of the root joint and any attached sites. */
    protected HAnimObject[] skeleton;

    /** The number of valid viewpoints of this object */
    protected int numSkeleton;

    /** The current collection of segments . */
    private HAnimSegment[] segments;

    /** The number of valid segments of this object */
    private int numSegments;

    /** Local matrix value that combines all the fields together */
    protected Matrix4f localMatrix;

    /** This is the root joint for the skeleton */
    protected HAnimJoint rootJoint;

    /** Flag to indicate if anything has changed this frame */
    protected boolean hasChildUpdates;

    /**
     * Flag to indicate the root matrix values have changed, thus needing
     * to regenerate the entire skeleton and vertices.
     */
    protected boolean matrixChanged;

    /**
     * Collection of all the other children than the root that need to be
     * updated.
     */
    private ArrayList updatedChildren;

    /**
     * Flag indicating that the skeleton (rootJoint) has had one or more
     * of it's children change and needs to be recalculated.
     */
    protected boolean skeletonChanged;


    /**
     * Create a new, default instance of the site.
     */
    public HAnimHumanoid()
    {
        IDENTITY_MATRIX.setIdentity();
        center = new float[3];
        rotation = new float[4];
        scale = new float[] { 1, 1, 1 };
        scaleOrientation = new float[4];
        translation = new float[3];
        bboxCenter = new float[3];
        bboxSize = new float[3];

        rotation[2] = 1;
        scaleOrientation[2] = 1;

        localMatrix = new Matrix4f();
        localMatrix.setIdentity();

        updatedChildren = new ArrayList();

        hasChildUpdates = false;
        matrixChanged = false;
        skeletonChanged = false;
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
        if(child == rootJoint)
            skeletonChanged = true;
        else
            updatedChildren.add(child);

        hasChildUpdates = true;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

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
        if(val == null || val.length < 3)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG);

        translation[0] = val[0];
        translation[1] = val[1];
        translation[2] = val[2];
        matrixChanged = true;
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
     * 4 units long, and the scaleOrientation is taken from the 1st three values.
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
     * 4 units long, and the rotation is taken from the 1st three values.
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
     * Get the currently set version string. If none is set, null is returned.
     *
     * @return The current string instance or null
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Set the new version string for this humanoid. A null value will remove
     * it.
     *
     * @param ver The new string to use
     */
    public void setVersion(String ver)
    {
        version = ver;
    }


    /**
     * Get the number of elements in the info string array.
     *
     * @return The number of values kept in the info list
     */
    public int numInfo()
    {
        return (info == null) ? 0 : info.length;
    }


    /**
     * Get the current collection of info strings. If none are set, the array is
     * unchanged.
     *
     * @param val An array of at least length numInfo() to copy the values to
     */
    public void getInfo(String[] vals)
    {
        System.arraycopy(info, 0, vals, 0, numInfo());
    }

    /**
     * Set the new list of info strings. A numValid of zero will remove all the
     * currently set values.
     *
     * @param infoStrings The collection of strings to use
     * @param numValid The number kids to copy from the given array
     */
    public void setInfo(String[] infoStrings, int numValid)
    {
        // we'll just resize every time as this isn't something that is likely
        // to be changing very often, if at all.
        if((info == null) || (info.length != numValid))
            info = new String[numValid];

        System.arraycopy(infoStrings, 0, info, 0, numValid);
    }

    /**
     * Get the number of elements in skinCoord
     *
     * @return The number of elements kept in the skinCoordinates list
     */
    public int numSkinCoord()
    {
        return numSkinCoords / 3;
    }

    /**
     * Get the current value of the skinCoord.
     *
     * @param val An array of at least length of numSkinCoord() * 3 to copy
     *   the values to
     */
    public void getSkinCoord(float[] val)
    {
        System.arraycopy(skinCoords, 0, val, 0, numSkinCoords);
    }

    /**
     * Set a new value for the skinCoord of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * a multiple of 3 units long.
     *
     * @param val The new skinCoord value to use
     * @param numElements The number of 3d-vectors in the array
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setSkinCoord(float[] val, int numElements)
    {
        if(val == null || val.length < numElements * 3)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG);

        if((skinCoords == null) || (skinCoords.length < numElements * 3))
            skinCoords = new float[numElements * 3];

        System.arraycopy(val, 0, skinCoords, 0, numElements * 3);
        numSkinCoords = numElements * 3;

        if(rootJoint != null)
            rootJoint.updateSources(skinCoords,
                                    numElements,
                                    skinNormals,
                                    numSkinNormals / 3,
                                    outputCoords,
                                    outputNormals);
    }

    /**
     * Get the number of elements in skinNormal
     *
     * @return The number of elements kept in the skinNormalinates list
     */
    public int numSkinNormal()
    {
        return numSkinNormals / 3;
    }

    /**
     * Get the current value of the skinNormal.
     *
     * @param val An array of at least length of numSkinNormal() * 3 to copy
     *   the values to
     */
    public void getSkinNormal(float[] val)
    {
        System.arraycopy(skinNormals, 0, val, 0, numSkinNormals);
    }

    /**
     * Set a new value for the skinNormal of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * a multiple of 3 units long.
     *
     * @param val The new skinNormal value to use
     * @param numElements The number of 3d-vectors in the array
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setSkinNormal(float[] val, int numElements)
    {
        if(val == null || val.length < numElements * 3)
            throw new IllegalArgumentException(MIN_ARRAY_SIZE_MSG);

        if((skinNormals == null) || (skinNormals.length < numElements * 3))
            skinNormals = new float[numElements * 3];

        System.arraycopy(val, 0, skinNormals, 0, numElements * 3);
        numSkinNormals = numElements * 3;

        if(rootJoint != null)
            rootJoint.updateSources(skinCoords,
                                    numSkinCoords / 3,
                                    skinNormals,
                                    numElements,
                                    outputCoords,
                                    outputNormals);
    }

    /**
     * Get the number of currently valid viewpoints.
     *
     * @return The number of values kept in the viewpoint list
     */
    public int numViewpoints()
    {
        return numViewpoints;
    }

    /**
     * Get the current collection of viewpoints. If none are set, the array is
     * unchanged.
     *
     * @param val An array of at least length numViewpoints() to copy the values to
     */
    public void getViewpoints(Object[] vals)
    {
        System.arraycopy(viewpoints, 0, vals, 0, numViewpoints);
    }

    /**
     * Replace the existing viewpoints with the new set of viewpoints.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     */
    public void setViewpoints(Object[] kids, int numValid)
    {
        if(viewpoints == null || viewpoints.length < numValid)
            viewpoints = new Object[numValid];

        if(numValid != 0)
            System.arraycopy(kids, 0, viewpoints, 0, numValid);

        for(int i = numValid; i < numViewpoints; i++)
            viewpoints[i] = null;

        numViewpoints = numValid;
    }

    /**
     * Get the number of currently valid joints.
     *
     * @return The number of values kept in the joint list
     */
    public int numJoints()
    {
        return numJoints;
    }

    /**
     * Get the current collection of joints. If none are set, the array is
     * unchanged.
     *
     * @param val An array of at least length numJoints() to copy the values to
     */
    public void getJoints(HAnimJoint[] vals)
    {
        System.arraycopy(joints, 0, vals, 0, numJoints);
    }

    /**
     * Replace the existing joints with the new set of joints.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     */
    public void setJoints(HAnimJoint[] kids, int numValid)
    {
        if(joints == null || joints.length < numValid)
            joints = new HAnimJoint[numValid];

        if(numValid != 0)
            System.arraycopy(kids, 0, joints, 0, numValid);

        for(int i = numValid; i < numJoints; i++)
            joints[i] = null;

        numJoints = numValid;
    }

    /**
     * Get the number of currently valid skeleton.
     *
     * @return The number of values kept in the joint list
     */
    public int numSkeleton()
    {
        return numSkeleton;
    }

    /**
     * Get the current collection of skeleton. If none are set, the array is
     * unchanged.
     *
     * @param val An array of at least length numSkeleton() to copy the values to
     */
    public void getSkeleton(HAnimObject[] vals)
    {
        System.arraycopy(skeleton, 0, vals, 0, numSkeleton);
    }

    /**
     * Replace the existing skeleton with the new set of skeleton. The skeleton
     * can only consist of a single Joint and multiple Site objects. Any other
     * HAnim object types shall issue an exception.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     * @throws IllegalArgumentException One of the provided s
     */
    public void setSkeleton(HAnimObject[] kids, int numValid)
    {
        if(skeleton == null || skeleton.length < numValid)
            skeleton = new HAnimObject[numValid];

        rootJoint = null;

        for(int i = 0; i < numValid; i++)
        {
            if(!(kids[i] instanceof HAnimJoint ||
                 kids[i] instanceof HAnimSite))
                throw new IllegalArgumentException(INVALID_CHILD_TYPE_MSG);

            skeleton[i] = kids[i];

            if(kids[i] instanceof HAnimJoint)
            {
                if(rootJoint == null)
                    rootJoint = (HAnimJoint)kids[i];

                ((HAnimJoint)kids[i]).setParent(this,
                                                skinCoords,
                                                numSkinCoords / 3,
                                                skinNormals,
                                                numSkinNormals / 3,
                                                outputCoords,
                                                outputNormals);
            }
            else
            {
                ((HAnimSite)kids[i]).setParent(this);
            }
        }

        for(int i = numValid; i < numSkeleton; i++)
            skeleton[i] = null;

        numSkeleton = numValid;
    }

    /**
     * Get the number of currently valid sites.
     *
     * @return The number of values kept in the joint list
     */
    public int numSites()
    {
        return numSites;
    }

    /**
     * Get the current collection of sites. If none are set, the array is
     * unchanged.
     *
     * @param val An array of at least length numSites() to copy the values to
     */
    public void getSites(HAnimObject[] vals)
    {
        System.arraycopy(sites, 0, vals, 0, numSites);
    }

    /**
     * Replace the existing sites with the new set of sites. The sites
     * can only consist of a single Joint and multiple Site objects. Any other
     * HAnim object types shall issue an exception.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     */
    public void setSites(HAnimSite[] kids, int numValid)
    {
        if(sites == null || sites.length < numValid)
            sites = new HAnimSite[numValid];

        if(numValid != 0)
            System.arraycopy(kids, 0, sites, 0, numValid);

        for(int i = numValid; i < numSites; i++)
            sites[i] = null;

        numSites = numValid;
    }

    /**
     * Get the number of currently valid segments.
     *
     * @return The number of values kept in the joint list
     */
    public int numSegments()
    {
        return numSegments;
    }

    /**
     * Get the current collection of segments. If none are set, the array is
     * unchanged.
     *
     * @param val An array of at least length numSegments() to copy the values to
     */
    public void getSegments(HAnimObject[] vals)
    {
        System.arraycopy(segments, 0, vals, 0, numSegments);
    }

    /**
     * Replace the existing segments with the new set of segments. The segments
     * can only consist of a single Joint and multiple Segment objects. Any other
     * HAnim object types shall issue an exception.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     */
    public void setSegments(HAnimSegment[] kids, int numValid)
    {
        if(segments == null || segments.length < numValid)
            segments = new HAnimSegment[numValid];

        if(numValid != 0)
            System.arraycopy(kids, 0, segments, 0, numValid);

        for(int i = numValid; i < numSegments; i++)
            segments[i] = null;

        numSegments = numValid;
    }

    /**
     * All the skeletal changes are in for this frame, so update the matrix
     * values now. If nothing has changed, don't bother doing any calculations
     * and return immediately.
     */
    public void updateSkeleton()
    {
        if(!hasChildUpdates && !matrixChanged)
            return;

        // Recalculate the root matrix first only if it needs to.
        if(matrixChanged)
        {
            updateMatrix(center,
                         rotation,
                         scale,
                         scaleOrientation,
                         translation,
                         localMatrix);

            if(rootJoint != null)
            {
                rootJoint.updateSkeleton(IDENTITY_MATRIX, false);
                skeletonChanged = false;
            }
        }

        if(rootJoint != null && skeletonChanged)
        {
            rootJoint.updateSkeleton(IDENTITY_MATRIX, false);
            skeletonChanged = false;
        }

        // all the site updates:
        int num_updates = updatedChildren.size();
        for(int i = 0; i < num_updates; i++)
        {
            HAnimSite site = (HAnimSite)updatedChildren.get(i);
            site.updateLocation(localMatrix, matrixChanged);
        }

        updatedChildren.clear();
        hasChildUpdates = false;
        matrixChanged = false;
    }
}
