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
 * Representation of a collection of keyframes for animating the object.
 * <p>
 *
 * A keyframe consists of
 * <pre>
 * KEYFRAMER CHUNK 0xB000
 *     NODE_HDR 0xB010
 *     PIVOT 0xB002
 *     POSITION TRACK TAG 0xB020
 *     ROTATION TRACK TAG 0xB021
 *     SCALE TRACK TAG 0xB022
 *     MORPH SMOOTH 0xB015
 * </pre>
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class KeyframeMeshBlock
{
    /** The name of the object this references */
    public String name;

    /** The position in the heirarchy */
    public int heirarchyPosition;

    /** Location of the pivot position for this mesh in local coords */
    public float[] pivot;

    /** Position track information */
    public KeyframePositionBlock positions;

    /** Rotation track information */
    public KeyframeRotationBlock rotations;

    /** Scale track information */
    public KeyframeScaleBlock scales;

    /** Smoothing factor in radians when morphing. In radians. */
    public float morphSmoothingAngle;
}
