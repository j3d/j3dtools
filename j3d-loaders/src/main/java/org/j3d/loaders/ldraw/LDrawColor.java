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
import java.util.Map;
import java.util.HashMap;


// Local parser
import org.j3d.util.I18nManager;

/**
 * Base definition of all colour types available to LDraw files.
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
public abstract class LDrawColor
{
    /** Global map from index to any colour type, inc all derived types */
    private static final Map<Integer, LDrawColor> indexToColorMap;

    /**
     * Enumerated list of the types of material that a colour can be
     * rendered in.
     */
    public enum ColorType
    {
        CORE,
        TRANSPARENT,
        PEARL,
        MILKY,
        CHROME,
        METALLIC,
        SPECKLE,
        RUBBER,
        GLITTER,
        INTERNAL,
    };

    /** 3 component primary colour */
    private final Color colour;

    /** 3 component complement colour */
    private final Color complement;

    /** The transparency to apply to the main colour */
    private final int alpha;

    /** The name of the colour in the currently set locale from i18nManager */
    private final String name;

    /**
     * Definition of the type of colour material this is. This is also infered
     * from the index of the colour from the list, but it's easier to have it
     * spelt out here.
     */
    private final ColorType type;

    /** The underlying colour index from the LDraw definition */
    private final int index;

    /**
     * Static constructor to initialse the global lookup map.
     */
    static
    {
        indexToColorMap = new HashMap<>();
    }

    protected LDrawColor(int idx,
                         ColorType ct,
                         int hexColour,
                         int hexComplement,
                         int a)
    {
        index = idx;
        type = ct;
        colour = new Color((hexColour & 0xFF0000) >> 16,
                           (hexColour & 0x00FF00) >> 8,
                           (hexColour & 0x0000FF) >> 0);

        complement = new Color((hexComplement & 0xFF0000) >> 16,
                               (hexComplement & 0x00FF00) >> 8,
                               (hexComplement & 0x0000FF) >> 0);

        alpha = a;

        I18nManager mgr = I18nManager.getManager();
        name = mgr.getString("org.j3d.loaders.ldraw.LDrawStdColor." + idx);

        indexToColorMap.put(idx, this);
    }

    /**
     * Get the LDRaw index that this colour class represents.
     *
     * A number greater than 0 and one of the defined index colour values
     */
    public int getIndex()
    {
        return index;
    }

    public ColorType getColorType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    /**
     * Return the alpha component of the colour to be applied. The value is
     * between 0 and 255 where 0 is completely opaque and 255 is completely
     * transparent.
     *
     * @return An alpha value that represents how transparent the object is
     */
    public int getAlpha()
    {
        return alpha;
    }

    public Color getColor()
    {
        return colour;
    }

    public Color getComplementColor()
    {
        return complement;
    }

    /**
     * Look up a matching colour definition based on an index. The index is the
     * LDraw index, not the Lego official index value.
     *
     * @param idx The LDraw index to get the colour for
     * @return The matching colour for that index. If there isn't one, return null
     */
    public static LDrawColor getColorForIndex(int idx)
    {
        return indexToColorMap.get(idx);
    }
}
