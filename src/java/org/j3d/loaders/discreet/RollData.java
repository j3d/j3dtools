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
 * A single set of camera roll data from the keyframe tracking.
 * <p>
 *
 * Rolls are described as a single angle
 * <p>
 *
 * The data represented is
 * <pre>
 *     struct {
 *        short framenum
 *        int unknown
 *        float camera_roll
 *     }
 * </pre>
 *
 * For this implementation, the unknown values are ignored and not stored.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class RollData extends TrackData
{
    /** The amount of roll to include value */
    public float roll;
}
