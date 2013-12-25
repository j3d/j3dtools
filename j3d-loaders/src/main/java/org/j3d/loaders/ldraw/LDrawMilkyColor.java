/*****************************************************************************
 *                            (c) j3d.org 2002-2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.ldraw;

// External imports
// None

// Local parser
// None

/**
 * Representation of a milky colour type.
 * <p>
 *
 * The definition of the file format can be found at:
 * <a href="http://www.ldraw.org/Article93.html">
 *  http://www.ldraw.org/Article93.html</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class LDrawMilkyColor extends LDrawColor
{
    public static final LDrawMilkyColor MILKY_21 =
        new LDrawMilkyColor(21, ColorType.MILKY, 0xE0FFB0, 0xA4C374, 224, 15);
    public static final LDrawMilkyColor MILKY_79 =
        new LDrawMilkyColor(79, ColorType.MILKY, 0xFFFFFF, 0xC3C3C3, 250, 0);
    public static final LDrawMilkyColor MILKY_294 =
        new LDrawMilkyColor(294, ColorType.MILKY, 0xBDC6AD, 0x818A71, 250, 15);

    /** The Luminance to apply to the main colour */
    private final int luminance;

    private LDrawMilkyColor(int idx,
                            ColorType ct,
                            int hexColour,
                            int hexComplement,
                            int a,
                            int lum)
    {
        super(idx, ct, hexColour, hexComplement, a);

        luminance = lum;
    }

    /**
     * Get the value of the luminance. This will be a value greater than or equal to zero.
     *
     * @return a number >= zero
     */
    public int getLuminance()
    {
        return luminance;
    }
}
