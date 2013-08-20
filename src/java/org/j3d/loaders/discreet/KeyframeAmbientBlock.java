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
 * Representation of a collection of a single camera block in a keyframe.
 * <p>
 *
 * A keyframe camera block consists of
 * <pre>
 * AMBIENT NODE TAG 0xB001
 *     NODE ID 0xB030
 *     NODE HEADER 0xB010
 *     COLOR TRACK 0xB020
 * </pre>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class KeyframeAmbientBlock extends KeyframeTag
{
    /** The track position info */
    public KeyframeColorBlock colors;
}
