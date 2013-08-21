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
import java.awt.Color;

// Local parser
// None

/**
 * Colour definition of a speckled item.
 * <p>
 * The definition of the file format can be found at:
 * <a href="http://www.ldraw.org/Article93.html">
 *  http://www.ldraw.org/Article93.html</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class LDrawSpeckleColor extends LDrawColor
{
    public static final LDrawSpeckleColor SPECKLE_75 =
        new LDrawSpeckleColor(75, ColorType.SPECKLE, 0x000000, 0x595959, 0, 0x595959, 0.4f, 1, 3);
    public static final LDrawSpeckleColor SPECKLE_76 =
        new LDrawSpeckleColor(76, ColorType.SPECKLE, 0x635F61, 0x595959, 0, 0xAE7A59, 0.4f, 1, 3);
    public static final LDrawSpeckleColor SPECKLE_132 =
        new LDrawSpeckleColor(132, ColorType.SPECKLE, 0x000000, 0x595959, 0, 0xAE7A59, 0.4f, 1, 3);
    public static final LDrawSpeckleColor SPECKLE_133 =
        new LDrawSpeckleColor(133, ColorType.SPECKLE, 0x000000, 0xDBAC34, 0, 0x595959, 0.4f, 1, 3);

    /**
     * A fractional value for the percentage of glitter in the final. A value
     * between 0 and 1.
     */
    private final float fraction;

    /** The minimum size of the glitter particles (not sure on units) */
    private final int minParticleSize;

    /** The maximum size of the glitter particles (not sure on units) */
    private final int maxParticleSize;

    /** The colour of the glitter inserts */
    private final Color speckleColor;

    private LDrawSpeckleColor(int idx,
                              ColorType ct,
                              int hexColour,
                              int hexComplement,
                              int a,
                              int hexSpeckle,
                              float frac,
                              int min,
                              int max)
    {
        super(idx, ct, hexColour, hexComplement, a);

        speckleColor = new Color((hexSpeckle & 0xFF0000) >> 16,
                                 (hexSpeckle & 0x00FF00) >> 8,
                                 (hexSpeckle & 0x0000FF) >> 0);

        fraction = frac;
        minParticleSize = min;
        maxParticleSize = max;
    }

    /**
     * Get the colour of the glitter particles that are embedded in the main
     * object colour.
     *
     * @return a non-null colour representation
     */
    public Color getSpeckleColour()
    {
        return speckleColor;
    }

    /**
     * Get the size of the glitter particles embedded in the plastic.
     *
     * @return a non-negative number for the size
     */
    public int getMinParticleSize()
    {
        return minParticleSize;
    }

    /**
     * Get the size of the glitter particles embedded in the plastic.
     *
     * @return a non-negative number for the size
     */
    public int getMaxParticleSize()
    {
        return maxParticleSize;
    }

    /**
     * Get the fractional percentage of glitter. The fraction is a value
     * between 0 (no glitter at all) and 1 (only glittler particles).
     *
     * @return a percentage value between 0 and 1
     */
    public float getFraction()
    {
        return fraction;
    }
}
