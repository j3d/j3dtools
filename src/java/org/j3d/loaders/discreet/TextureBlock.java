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
 * Representation of a single texture and its paramaters needed for rendering.
 * <p>
 *
 * Textures are not inlined in the file, so this object only contains
 * information needed to read an external file and render it.
 * <p>
 *
 * The texture subchunk consists of the following information
 * <pre>
 *     MAPPING FILENAME 0xA300
 *     MAPPING PARAMETERS 0xA351
 * </pre>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class TextureBlock
{
    /**
     * The name of the file containing the raw texture file. Name may be
     * absolute or relative.
     */
    public String filename;

    /** A strength (percentage - [0,1]) for the texture map */
    public float strength;

    /**
     * The map tiling type (Not for repeat, clamp etc). Control bit flags,
     * where bit 0 (0x1) activates decaling, bit 1 (0x2) activates mirroring,
     * bit 3 (0x8) activates negation, bit 4 (0x10) deactivates tiling,
     * bit 5 (0x20) activates summed area sampling, bit 6 (0x40) activates
     * alpha sourcing, bit 7 (0x80) activates tinting, bit 8 (0x100) ignores
     * alpha, and bit 9 (0x200) activates RGB tinting. Bits 7, 8, and 9 are
     * only used with MAT_TEXMAP, MAT_TEX2MAP, and MAT_SPECMAP chunks. Bit 6,
     * when used with a MAT_TEXMAP, MAT_TEX2MAP, or MAT_SPECMAP chunk must be
     * accompanied with a tinting bit, either 7 or 9. Remaining bits are for
     * internal use only.
     */
    public int tiling;

    // EH? WTF does that translate to?
    /** The amount of blurring of the texture */
    public float blurring;

    /**
     * Bump map percentage filter to apply. Not quite sure exacly what this
     * does, but probably a scaling factor, like Strength.
     */
    public float bumpPercentage;

    /** The scale in the U direction */
    public float uScale;

    /** The scale in the V direction */
    public float vScale;

    /** The translation in the u direction */
    public float uOffset;

    /** The translation in the v direction */
    public float vOffset;

    /** The amount of rotation of the map (degrees or radians?) */
    public float angle;

    /** blend colour 1. Null if not set. */
    public float[] blendColor1;

    /** blend colour 2. Null if not set */
    public float[] blendColor2;

    /** Individual red component blend colour value */
    public float[] redBlends;

    /** Individual green component blend colour value */
    public float[] greenBlends;

    /** Individual blue component blend colour value */
    public float[] blueBlends;
}
