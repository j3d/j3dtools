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
 * Representation of a quad part.
 * <p>
 * Quads are assumed to be in CCW order.
 *
 * The definition of the file format can be found at:
 * <a href="http://www.ldraw.org/Article93.html">
 *  http://www.ldraw.org/Article93.html</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class LDrawQuad extends LDrawRenderable
{
    /** First mid point in the quad */
    private double[] p1;

    /** Second mid point in the quad */
    private double[] p2;


    /**
     * Construct the line information from the two points and colour.
     *
     * @param col The colour to render in. Most not be null
     */
    LDrawQuad(LDrawColor col, double[] start, double[] middle1, double[] middle2, double[] end)
    {
        super(col, start, end);

        p1 = middle1;
        p2 = middle2;
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
        bldr.append(p1[0]);
        bldr.append(",");
        bldr.append(p1[1]);
        bldr.append(") to (");
        bldr.append(p2[0]);
        bldr.append(",");
        bldr.append(p2[1]);
        bldr.append(") to (");
        bldr.append(end[0]);
        bldr.append(",");
        bldr.append(end[1]);
        bldr.append(")");

        return bldr.toString();
    }

    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------

    /**
     * Get the coordinates of the second point (X, Y, Z)
     *
     * @return a reference to the internal position value
     */
    public double[] getMiddlePoint1()
    {
        return p1;
    }

    /**
     * Get the coordinates of the second point (X, Y, Z)
     *
     * @return a reference to the internal position value
     */
    public double[] getMiddlePoint2()
    {
        return p2;
    }
}
