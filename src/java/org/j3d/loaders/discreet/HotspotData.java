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
 * A single set of spotlight hotspot angle data from the keyframe tracking.
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
 *        float hotspot_ang
 *     }
 * </pre>
 *
 * For this implementation, the unknown values are ignored and not stored.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class HotspotData extends TrackData
{
    /** The angle for the hotspot something or other. */
    public float angle;
}
