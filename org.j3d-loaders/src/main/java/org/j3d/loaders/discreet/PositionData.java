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
 * A single set of position data from the keyframe tracking.
 * <p>
 *
 * Positions are described as a location in 3-space.
 * <p>
 *
 * The data represented is
 * <pre>
 *     struct {
 *        int framenum
 *        short splineInfo
 *        float x
 *        float y
 *        float z
 *     }
 * </pre>
 *
 * For this implementation, the unknown values are ignored and not stored.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class PositionData extends TrackData
{
    /** The x axis value */
    public float x;

    /** The y axis value */
    public float y;

    /** The z axis value */
    public float z;
}
