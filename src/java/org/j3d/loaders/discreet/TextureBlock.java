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
 * @version $Revision: 1.1 $
 */
public class TextureBlock
{
    /**
     * The name of the file containing the raw texture file. Name may be
     * absolute or relative.
     */
    public String filename;

    /** The map tiling (repeat, clamp etc) */
    public int tiling;

    // EH? WTF does that translate to?
    /** The amount of blurring of the texture */
    public float blurring;

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
}
