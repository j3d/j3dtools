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
 * Representation of a keyframe general lighting handling.
 * <p>
 *
 * A keyframe consists of
 * <pre>
 * LIGHT INFORMATION BLOCK 0xB005
 *     NODE_HDR 0xB010
 *     POS TRACK TAG 0xB020
 *     COLOR TRACK TAG 0xB025
 * </pre>
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class KeyframeLightBlock
{
    /** The spotlight ID this block belongs to. */
    public int nodeId;

    /** The track position info */
    public KeyframeTrackBlock positions;

    /** Track colour info */
    public KeyframeColorBlock colors;
}
