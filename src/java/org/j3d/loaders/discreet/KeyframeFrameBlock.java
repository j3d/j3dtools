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
 * Representation of a collection of a single frame block in a keyframe.
 * <p>
 *
 * A keyframe block consists of
 * <pre>
 * FRAMES (START AND END) 0xB008
 *     OBJECT NAME 0xB010
 *     OBJECT PIVOT POINT 0xB013
 *     POSITION TRACK 0xB020
 *     ROTATION TRACK 0xB021
 *     SCALE TRACK 0xB022
 *     HIERARCHY POSITION 0xB030
 * </pre>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class KeyframeFrameBlock
{
    /** An identifier for the node */
    public int nodeId;

    /** Node header data, if specified */
    public NodeHeaderData nodeHeader;

    /** The pivot point coordinate for this block */
    public float[] pivotPoint;

    /** The track position info */
    public KeyframePositionBlock[] positions;

    /** Number of valid positions to use */
    public int numPositions;

    /** The track rotation info */
    public KeyframeRotationBlock[] rotations;

    /** Number of valid rotations to use */
    public int numRotations;

    /** The track scale info */
    public KeyframeScaleBlock[] scales;

    /** Number of valid scales to use */
    public int numScales;

    /** The track field of view info */
    public KeyframeFOVBlock[] fovs;

    /** Number of valid field of views to use */
    public int numFOVs;

    /** The track camera roll info */
    public KeyframeRollBlock[] rolls;

    /** Number of valid rolls to use */
    public int numRolls;

    /** The track color info */
    public KeyframeColorBlock[] colors;

    /** Number of valid colors to use */
    public int numColors;

    /** The track morph info */
    public KeyframeMorphBlock[] morphs;

    /** Number of valid morphs to use */
    public int numMorphs;

    /** The track spotlight hotspot info */
    public KeyframeHotspotBlock[] hotspots;

    /** Number of valid hotspots to use */
    public int numHotspots;

    /** The track rolloff info */
    public KeyframeFalloffBlock[] falloffs;

    /** Number of valid falloffs to use */
    public int numFalloffs;
}
