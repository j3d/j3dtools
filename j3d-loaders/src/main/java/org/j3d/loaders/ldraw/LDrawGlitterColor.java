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
 * Colour definition of a glittered item.
 * <p>
 * Colours are named by number in the Lego view of the world, however we can't
 * name stuff as a number here so it is prefixed by the colour type (core,
 * transparent etc)
 * <p>
 *
 * The definition of the file format can be found at:
 * <a href="http://www.ldraw.org/Article93.html">
 *  http://www.ldraw.org/Article93.html</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class LDrawGlitterColor extends LDrawColor
{
    public static final LDrawGlitterColor GLITTER_114 =
        new LDrawGlitterColor(114, ColorType.GLITTER, 0xDF6695, 0x9A2A66, 128, 0x923978, 0.17f, 0.2f, 1);
    public static final LDrawGlitterColor GLITTER_117 =
        new LDrawGlitterColor(117, ColorType.GLITTER, 0xFFFFFF, 0xC3C3C3, 128, 0xFFFFFF, 0.08f, 0.1f, 1);
    public static final LDrawGlitterColor GLITTER_129 =
        new LDrawGlitterColor(129, ColorType.GLITTER, 0x640061, 0x280025, 128, 0x923978, 0.3f, 0.4f, 1);

    /**
     * A fractional value for the percentage of glitter in the final. A value
     * between 0 and 1.
     */
    private final float fraction;

    /** The second fractional value. Not sure on what it means */
    private final float vfraction;

    /** The size of the glitter particles (not sure on units) */
    private final int particleSize;

    /** The colour of the glitter inserts */
    private final Color glitterColor;

    private LDrawGlitterColor(int idx,
                              ColorType ct,
                              int hexColour,
                              int hexComplement,
                              int a,
                              int hexGlitter,
                              float frac,
                              float vfrac,
                              int size)
    {
        super(idx, ct, hexColour, hexComplement, a);

        glitterColor = new Color((hexGlitter & 0xFF0000) >> 16,
                                 (hexGlitter & 0x00FF00) >> 8,
                                 (hexGlitter & 0x0000FF) >> 0);

        fraction = frac;
        vfraction = vfrac;
        particleSize = size;
    }

    /**
     * Get the colour of the glitter particles that are embedded in the main
     * object colour.
     *
     * @return a non-null colour representation
     */
    public Color getGlitterColour()
    {
        return glitterColor;
    }

    /**
     * Get the size of the glitter particles embedded in the plastic.
     *
     * @return a non-negative number for the size
     */
    public int getParticleSize()
    {
        return particleSize;
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

    /**
     * Get the fractional percentage of glitter. The fraction is a value
     * between 0 (no glitter at all) and 1 (only glittler particles).
     *
     * @return a percentage value between 0 and 1
     */
    public float getVFraction()
    {
        return vfraction;
    }
}
