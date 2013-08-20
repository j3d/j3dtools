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
 * Representation of a keyframe's collection of position information.
 * <p>
 *
 * A keyframe consists of
 * <pre>
 * POSITION TRACK TAG 0xB021
 *     short flags
 *     short unknown[4]
 *     long keys;
 *     struct {
 *        short framenum
 *        int unknown
 *        float pos_x
 *        float pos_y
 *        float pos_z
 *     } position[keys]
 * </pre>
 *
 * For this implementation, the unknown values are ignored and not stored.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class KeyframePositionBlock
{
    /** The flags associated with this rotation. */
    public int flags;

    /** The number of valid keys to use */
    public int numKeys;

    /** The sets of keyframe/value pairs */
    public PositionData[] positions;
}
