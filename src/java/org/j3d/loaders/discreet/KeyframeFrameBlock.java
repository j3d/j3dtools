/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.discreet;

// External imports
// None

// Local imports
// None

/**
 * Representation of a collection of a single frame block in a keyframe.
 * <p>
 *
 * A keyframe block consists of
 * <pre>
 * FRAMES (START AND END) 0xB008
 *     OBJECT NAME 0xB010
 *     OBJECT PIVOT POINT 0xB013
 *     INSTANCE NAME
 *     BOUNDBOX  0xB014
 *     POSITION TRACK 0xB020
 *     ROTATION TRACK 0xB021
 *     SCALE TRACK 0xB022
 *     HIERARCHY POSITION 0xB030
 * </pre>
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public class KeyframeFrameBlock extends KeyframeTag
{
    /** The pivot point coordinate for this block */
    public float[] pivotPoint;

    /** The name of the object instance */
    public String instanceName;

    /** Bounding box minimum positions of the mesh */
    public float[] minBounds;

    /** Bounding box maximum positions of the mesh */
    public float[] maxBounds;

    /** The track position info */
    public KeyframePositionBlock positions;

    /** The track rotation info */
    public KeyframeRotationBlock rotations;

    /** The track scale info */
    public KeyframeScaleBlock scales;

    /** The track morph info */
    public KeyframeMorphBlock morphs;

    /** Smoothing factor in radians when morphing. In degrees [0 - 180]. */
    public float morphSmoothingAngle;

    /**
     * Construct a new instance of this frame.
     */
    public KeyframeFrameBlock()
    {
        minBounds = new float[3];
        maxBounds = new float[3];
        pivotPoint = new float[3];
    }
}
