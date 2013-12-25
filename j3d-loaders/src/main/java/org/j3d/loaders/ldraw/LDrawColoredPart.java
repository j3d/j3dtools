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
 * Base representation of a part from the file that is not the header.
 * <p>
 *
 * The definition of the file format can be found at:
 * <a href="http://www.ldraw.org/Article93.html">
 *  http://www.ldraw.org/Article93.html</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public abstract class LDrawColoredPart extends LDrawFilePart
{
    /** The colour that this part is to be rendered in */
    private LDrawColor partColor;

    /** If BFC is enabled, and the InvertNext flag has been set, this is true */
    private boolean invertWinding;

    /**
     * Construct the base part that is rendered in the specific colour.
     *
     * @param col The colour to render in. Most not be null
     */
    protected LDrawColoredPart(LDrawColor col)
    {
        assert col != null : "Null part colour not allowed";

        partColor = col;
        invertWinding = false;
    }

    /**
     * Get the colour that the part is rendered in.
     */
    public LDrawColor getColor()
    {
        return partColor;
    }

    /**
     * Set the inverted winding flag for this object.
     */
    void setInvertedWinding(boolean state)
    {
        invertWinding = state;
    }

    /**
     * Get whether this part has been flagged as to be inverted winding when
     * rendered.
     *
     * @return true When inversion is to be applied
     */
    public boolean isInvertedWinding()
    {
        return invertWinding;
    }

}
