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
 * A single set of scale data from the keyframe tracking.
 * <p>
 *
 * Scales are independent on each axis and may include negative values.
 * <p>
 *
 * The data represented is
 * <pre>
 *     struct {
 *        short framenum
 *        int unknown
 *        float scale_x
 *        float scale_y
 *        float scale_z
 *     }
 * </pre>
 *
 * For this implementation, the unknown values are ignored and not stored.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class ScaleData extends TrackData
{
    /** The x scale value */
    public float xScale;

    /** The y scale value */
    public float yScale;

    /** The z scale value */
    public float zScale;
}
