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
 * A single set of rotation data from the keyframe tracking.
 * <p>
 *
 * Rotations are described as an axis-angle setup.
 * <p>
 *
 * The data represented is
 * <pre>
 *     struct {
 *        short framenum
 *        int unknown
 *        float rotation in radians
 *        float axis_x
 *        float axis_y
 *        float axis_z
 *     }
 * </pre>
 *
 * For this implementation, the unknown values are ignored and not stored.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class RotationData extends TrackData
{
    /** The rotation value in radians */
    public float rotation;

    /** The x axis value */
    public float xAxis;

    /** The y axis value */
    public float yAxis;

    /** The z axis value */
    public float zAxis;
}
