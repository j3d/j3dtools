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
 * Representation of a keyframe spotlight handling.
 * <p>
 *
 * A keyframe consists of
 * <pre>
 * SPOT LIGHT INFORMATION BLOCK 0xB007
 *     NODE_HDR 0xB010
 *     POSITION TRACK TAG 0xB020
 *     HOTSPOT TRACK TAG 0xB027
 *     FALLOFF TRACK TAG 0xB028
 *     ROLL TRACK TAG 0xB024
 *     COLOR TRACK TAG 0xB025
 * </pre>
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class KeyframeSpotlightBlock extends KeyframeTag
{
    /** The track position info */
    public KeyframePositionBlock positions;

    /** Spotlight hotspot track info */
    public KeyframeHotspotBlock hotspots;

    /** Spotlight falloff track info */
    public KeyframeFalloffBlock falloffs;

    /** Spotlight cone roll track info */
    public KeyframeRollBlock rolloffs;

    /** Colour track info */
    public KeyframeColorBlock colors;
}
