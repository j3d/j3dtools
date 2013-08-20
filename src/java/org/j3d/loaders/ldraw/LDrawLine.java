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
public class LDrawLine extends LDrawRenderable
{
    /**
     * Construct the line information from the two points and colour.
     *
     * @param col The colour to render in. Most not be null
     */
    LDrawLine(LDrawColor col, double[] start, double[] end)
    {
        super(col, start, end);
    }

    //------------------------------------------------------------------------
    // Methods defined by Object
    //------------------------------------------------------------------------

    @Override
    public String toString()
    {
        StringBuilder bldr = new StringBuilder("LDraw Triangle ");
        bldr.append("Colour ID ");
        bldr.append(getColor());
        bldr.append(" Inverted? ");
        bldr.append(isInvertedWinding() ? 'Y' : 'N');
        bldr.append(" from ( ");
        bldr.append(start[0]);
        bldr.append(",");
        bldr.append(start[1]);
        bldr.append(") to (");
        bldr.append(end[0]);
        bldr.append(",");
        bldr.append(end[1]);
        bldr.append(")");

        return bldr.toString();
    }
}
